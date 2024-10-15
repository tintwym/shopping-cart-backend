package com.shopping.cart.interfaces;

public interface ICheckoutService {
    String initiateCheckout(String token);
    void processCheckout(String paymentId);
    void cancelCheckout(String paymentId);
}
