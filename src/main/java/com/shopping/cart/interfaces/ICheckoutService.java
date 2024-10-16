package com.shopping.cart.interfaces;

public interface ICheckoutService {
    String checkout(String token);
    void completeOrder(String token);
}
