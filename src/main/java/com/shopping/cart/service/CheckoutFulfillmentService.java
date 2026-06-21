package com.shopping.cart.service;

import com.shopping.cart.entity.*;
import com.shopping.cart.repository.*;
import com.stripe.exception.StripeException;
import com.stripe.model.LineItem;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionRetrieveParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Turns a paid Stripe Checkout Session into an order (idempotent), adjusts stock,
 * records payment, and clears the cart. Used by the Stripe webhook and by the
 * post-checkout sync endpoint.
 */
@Service
public class CheckoutFulfillmentService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutFulfillmentService.class);

    private final TransactionTemplate transactionTemplate;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    public CheckoutFulfillmentService(
            PlatformTransactionManager transactionManager,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            PaymentRepository paymentRepository,
            ProductRepository productRepository,
            CartRepository cartRepository,
            UserRepository userRepository) {
        this.transactionTemplate = new TransactionTemplate(Objects.requireNonNull(transactionManager));
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    public Session retrievePaidSession(String sessionId) throws StripeException {
        SessionRetrieveParams params = SessionRetrieveParams.builder()
                .addExpand("line_items.data.price.product")
                .build();
        Session session = Session.retrieve(sessionId, params, null);
        if (!"paid".equals(session.getPaymentStatus())) {
            throw new IllegalStateException("Checkout session is not paid yet: " + session.getPaymentStatus());
        }
        return session;
    }

    /**
     * Idempotent fulfillment for a Checkout Session. Stripe I/O happens outside the DB transaction;
     * synchronized prevents double-fulfillment on concurrent webhook + browser sync on one node.
     */
    public void fulfillBySessionId(String sessionId) throws StripeException {
        Session session = retrievePaidSession(sessionId);
        fulfillAfterRetrieve(sessionId, session);
    }

    /**
     * Verifies the paid session belongs to {@code user}, then fulfills idempotently.
     */
    public void confirmSessionForUser(String sessionId, User user) throws StripeException {
        Session session = retrievePaidSession(sessionId);
        Map<String, String> metadata = session.getMetadata();
        String ownerId = metadata != null ? metadata.get("user_id") : null;
        if (ownerId == null || !user.getId().toString().equals(ownerId)) {
            throw new IllegalStateException("This checkout session does not belong to the current user");
        }
        fulfillAfterRetrieve(sessionId, session);
    }

    public void fulfillAfterRetrieve(String sessionId, Session session) {
        if (!"paid".equals(session.getPaymentStatus())) {
            throw new IllegalStateException("Checkout session is not paid yet: " + session.getPaymentStatus());
        }
        synchronized (sessionId.intern()) {
            transactionTemplate.executeWithoutResult(status -> {
                if (orderRepository.findByStripeCheckoutSessionId(sessionId).isPresent()) {
                    return;
                }
                if (session.getMetadata() == null || session.getMetadata().get("user_id") == null) {
                    throw new IllegalStateException("Checkout session missing user_id metadata");
                }
                UUID userId = UUID.fromString(session.getMetadata().get("user_id"));
                User user = userRepository.findById(Objects.requireNonNull(userId))
                        .orElseThrow(() -> new IllegalStateException("User not found for checkout session"));

                if (session.getLineItems() == null || session.getLineItems().getData() == null
                        || session.getLineItems().getData().isEmpty()) {
                    throw new IllegalStateException("Checkout session has no line items");
                }

                long sumLineCents = session.getLineItems().getData().stream()
                        .mapToLong(li -> li.getAmountTotal() != null ? li.getAmountTotal() : 0L)
                        .sum();
                if (session.getAmountTotal() != null && sumLineCents != session.getAmountTotal()) {
                    throw new IllegalStateException("Line item totals do not match session amount");
                }

                Map<UUID, Integer> qtyByProduct = new LinkedHashMap<>();
                Map<UUID, BigDecimal> unitPriceByProduct = new LinkedHashMap<>();
                for (LineItem lineItem : session.getLineItems().getData()) {
                    Product product = resolveProduct(lineItem);
                    if (product.isDeleted()) {
                        throw new IllegalStateException("Product is no longer available: " + product.getId());
                    }
                    int qty = lineItem.getQuantity() != null ? lineItem.getQuantity().intValue() : 0;
                    if (qty <= 0) {
                        throw new IllegalStateException("Invalid quantity for line item");
                    }
                    qtyByProduct.merge(product.getId(), qty, (a, b) -> a + b);
                    BigDecimal unit = product.getPrice();
                    unitPriceByProduct.putIfAbsent(product.getId(), unit);
                }

                for (Map.Entry<UUID, Integer> e : qtyByProduct.entrySet()) {
                    Product fresh = productRepository.findById(Objects.requireNonNull(e.getKey()))
                            .orElseThrow(() -> new IllegalStateException("Product missing: " + e.getKey()));
                    if (fresh.isDeleted()) {
                        throw new IllegalStateException("Product is no longer available: " + fresh.getId());
                    }
                    if (fresh.getStock() < e.getValue()) {
                        log.error("Insufficient stock after payment for product {} (need {}, have {}). Manual refund may be required. session={}",
                                fresh.getId(), e.getValue(), fresh.getStock(), sessionId);
                        throw new IllegalStateException("Insufficient stock for product: " + fresh.getName());
                    }
                }

                BigDecimal orderTotal = BigDecimal.valueOf(session.getAmountTotal()).movePointLeft(2);

                Order order = new Order();
                order.setUser(user);
                order.setTotalPrice(orderTotal);
                order.setStatus("COMPLETED");
                order.setStripeCheckoutSessionId(sessionId);
                order = orderRepository.save(order);

                for (Map.Entry<UUID, Integer> e : qtyByProduct.entrySet()) {
                    Product fresh = productRepository.findById(Objects.requireNonNull(e.getKey())).orElseThrow();
                    int qty = e.getValue();
                    fresh.setStock(fresh.getStock() - qty);
                    productRepository.save(fresh);

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(fresh);
                    orderItem.setQuantity(qty);
                    orderItem.setPrice(fresh.getPrice().multiply(BigDecimal.valueOf(qty)));
                    orderItemRepository.save(orderItem);
                }

                Payment payment = new Payment();
                payment.setPaymentStatus(1);
                payment.setAmount(orderTotal);
                payment.setUser(user);
                payment.setOrder(order);
                if (session.getPaymentIntent() != null) {
                    payment.setStripePaymentIntentId(session.getPaymentIntent());
                }
                paymentRepository.save(payment);

                Cart cart = cartRepository.findByUser(user);
                if (cart != null) {
                    cartRepository.delete(cart);
                }
            });
        }
    }

    private Product resolveProduct(LineItem lineItem) {
        Price price = lineItem.getPrice();
        if (price == null) {
            throw new IllegalStateException("Line item missing price");
        }
        if (price.getId() != null && !price.getId().isBlank()) {
            Optional<Product> byPrice = productRepository.findByStripePriceId(price.getId());
            if (byPrice.isPresent()) {
                return byPrice.get();
            }
        }
        if (price.getProduct() != null && !price.getProduct().isBlank()) {
            Optional<Product> byStripeProduct = productRepository.findByStripeProductId(price.getProduct());
            if (byStripeProduct.isPresent()) {
                return byStripeProduct.get();
            }
        }
        com.stripe.model.Product stripeProduct = price.getProductObject();
        if (stripeProduct != null && stripeProduct.getMetadata() != null) {
            String internalId = stripeProduct.getMetadata().get("internal_product_id");
            if (internalId != null && !internalId.isBlank()) {
                return productRepository.findById(Objects.requireNonNull(UUID.fromString(internalId)))
                        .orElseThrow(() -> new IllegalStateException("Unknown internal_product_id: " + internalId));
            }
        }
        throw new IllegalStateException("Could not map Stripe line item to catalog product");
    }
}
