package com.shopping.cart.service;

import com.shopping.cart.entity.*;
import com.shopping.cart.interfaces.ICheckoutService;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.OrderItemRepository;
import com.shopping.cart.repository.OrderRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService implements ICheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Autowired
    public CheckoutService(CartRepository cartRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserService userService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
    }

    @Override
    public String checkout(String token) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        // Fetch the user's cart
        Cart cart = cartRepository.findByUser(user);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Cannot proceed with checkout.");
        }

        // Create Stripe line items based on cart contents
        List<SessionCreateParams.LineItem> stripeLineItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            SessionCreateParams.LineItem stripeLineItem = SessionCreateParams.LineItem.builder()
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("sgd")
                                    .setUnitAmount(cartItem.getPrice().longValue() * 100)
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(cartItem.getProduct().getName())
                                                    .build()
                                    )
                                    .build()
                    )
                    .setQuantity((long) cartItem.getQuantity())
                    .build();
            stripeLineItems.add(stripeLineItem);
        }

        try {
            // Create the Stripe Checkout session
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .addAllLineItem(stripeLineItems)
                    .setSuccessUrl("http://localhost:5173/payment/success")
                    .setCancelUrl("http://localhost:5173/cart")
                    .build();

            Session session = Session.create(params);

            // Return the session ID so the frontend can redirect to Stripe
            return session.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }

    @Override
    public void completeOrder(String token) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        // Fetch the user's cart
        Cart cart = cartRepository.findByUser(user);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Cannot proceed with order completion.");
        }

        // Check if there is a pending order for this user
        if (orderRepository.existsByUserAndStatus(user, "PENDING")) {
            throw new IllegalStateException("There is already a pending order for this user.");
        }

        // Create a new Order and set the user and total price
        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(cart.getTotalPrice());
        order.setStatus("PENDING"); // Set the status to PENDING initially

        // Save the order
        order = orderRepository.save(order);

        // Move cart items to order items
        List<CartItem> cartItems = cart.getCartItems();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            // Save order item
            orderItemRepository.save(orderItem);
        }

        // Clear the cart after order is completed
        cartRepository.delete(cart);

        // Set order status to COMPLETED after everything is done
        order.setStatus("COMPLETED");
        orderRepository.save(order);
    }
}
