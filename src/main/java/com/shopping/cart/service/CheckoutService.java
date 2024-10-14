package com.shopping.cart.service;

import com.shopping.cart.entity.*;
import com.shopping.cart.interfaces.ICheckoutService;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.OrderItemRepository;
import com.shopping.cart.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService implements ICheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    @Autowired
    public CheckoutService(CartRepository cartRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserService userService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
    }

    public void checkout(String token) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        // Fetch the user's cart
        Cart cart = cartRepository.findByUser(user);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Cannot proceed with checkout.");
        }

        // Create a new Order and set the user and total price
        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(cart.getTotalPrice());

        // Save the order
        order = orderRepository.save(order);

        // Move cart items to order items
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());

            // Save order item
            orderItemRepository.save(orderItem);
        }

        // After processing, clear the cart
        cartRepository.delete(cart);
    }
}
