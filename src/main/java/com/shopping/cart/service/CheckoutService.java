package com.shopping.cart.service;

import com.shopping.cart.entity.Cart;
import com.shopping.cart.entity.CartItem;
import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.ICheckoutService;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.ProductRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService implements ICheckoutService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final CheckoutFulfillmentService checkoutFulfillmentService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Autowired
    public CheckoutService(
            CartRepository cartRepository,
            ProductRepository productRepository,
            UserService userService,
            CheckoutFulfillmentService checkoutFulfillmentService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.checkoutFulfillmentService = checkoutFulfillmentService;
    }

    @Override
    public String checkout(String token) {
        User user = userService.getUserFromToken(token);

        Cart cart = cartRepository.findByUser(user);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Cannot proceed with checkout.");
        }

        List<SessionCreateParams.LineItem> stripeLineItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            Product product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("Product not found: " + cartItem.getProduct().getId()));
            if (product.isDeleted()) {
                throw new IllegalStateException("Removed product still in cart: " + product.getName());
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for " + product.getName()
                        + " (requested " + cartItem.getQuantity() + ", available " + product.getStock() + ")");
            }

            SessionCreateParams.LineItem stripeLineItem;
            if (product.getStripePriceId() != null && !product.getStripePriceId().isBlank()) {
                stripeLineItem = SessionCreateParams.LineItem.builder()
                        .setPrice(product.getStripePriceId())
                        .setQuantity((long) cartItem.getQuantity())
                        .build();
            } else {
                stripeLineItem = SessionCreateParams.LineItem.builder()
                        .setPriceData(
                                SessionCreateParams.LineItem.PriceData.builder()
                                        .setCurrency("sgd")
                                        .setUnitAmount(product.getPrice().longValue() * 100)
                                        .setProductData(
                                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                        .setName(product.getName())
                                                        .putMetadata("internal_product_id", product.getId().toString())
                                                        .build()
                                        )
                                        .build()
                        )
                        .setQuantity((long) cartItem.getQuantity())
                        .build();
            }
            stripeLineItems.add(stripeLineItem);
        }

        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;

        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addAllLineItem(stripeLineItems)
                    .setClientReferenceId(user.getId().toString())
                    .putMetadata("user_id", user.getId().toString())
                    .setSuccessUrl(base + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(base + "/cart")
                    .build();

            Session session = Session.create(params);
            return session.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }

    /**
     * Called from the browser after Stripe redirects back. Verifies the session belongs to the
     * signed-in user, then fulfills idempotently (same logic as the Stripe webhook).
     */
    @Override
    public void confirmCheckoutSession(String token, String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalStateException("sessionId is required");
        }
        User user = userService.getUserFromToken(token);
        try {
            com.stripe.model.checkout.Session session = checkoutFulfillmentService.retrievePaidSession(sessionId);
            String metaUser = session.getMetadata() != null ? session.getMetadata().get("user_id") : null;
            if (metaUser == null || !user.getId().toString().equals(metaUser)) {
                throw new IllegalStateException("This checkout session does not belong to the current user");
            }
            checkoutFulfillmentService.fulfillAfterRetrieve(sessionId, session);
        } catch (StripeException e) {
            throw new RuntimeException("Unable to confirm checkout with Stripe", e);
        }
    }
}
