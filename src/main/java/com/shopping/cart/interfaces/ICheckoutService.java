package com.shopping.cart.interfaces;

public interface ICheckoutService {
    String checkout(String token);

    /** Confirms a paid Stripe Checkout Session for the current user (idempotent). */
    void confirmCheckoutSession(String token, String sessionId);
}
