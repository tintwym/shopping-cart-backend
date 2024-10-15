package com.shopping.cart.service;

import com.shopping.cart.entity.*;
import com.shopping.cart.interfaces.ICheckoutService;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.OrderItemRepository;
import com.shopping.cart.repository.OrderRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckoutService implements ICheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;
    private final StripeService stripeService;
    private final PaymentService paymentService;
    

    @Autowired
    public CheckoutService(CartRepository cartRepository, OrderRepository orderRepository, OrderItemRepository orderItemRepository, UserService userService, StripeService stripeService, PaymentService paymentService) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
		this.stripeService = stripeService;
		this.paymentService = paymentService;
    }

    public String initiateCheckout(String token) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        // Fetch the user's cart
        Cart cart = cartRepository.findByUser(user);
        if (cart == null || cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cart is empty. Cannot proceed with checkout.");
        }
        
        // Create Payment Object 
     	Payment payment = new Payment(0, cart.getTotalPrice(), user);
     	paymentService.createPayment(payment);
     	// Convert to Stripe Items
     		List<SessionCreateParams.LineItem> stripeLineItems = stripeService.convertToStripeItem(cart.getCartItems());
     		
     	// Create Stripe Session - add error handling
     	Session session = null;
     	try {
     		session = stripeService.createStripeSession(stripeLineItems, payment.getId().toString());
     		System.out.println("session is created");
     		session.getAmountTotal();
     		System.out.println(session.getUrl());
     	} catch (StripeException e) {
     		e.printStackTrace();
     	}
     		
     	// Save Payment into DB and remove any existing payments that are unpaid 
     	payment.setStripeSessionId(session.getId());
     	paymentService.updatePayment(payment);
     		
     	// Return the Session URL
     	return session.getUrl();
        
        

    }
    
    public void processCheckout(String token) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);
    	
        // Fetch the user's cart
        Cart cart = cartRepository.findByUser(user);
        
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
    
    public void cancelCheckout(String paymentId) {
		Payment payment = paymentService.findById(paymentId);
		
		paymentService.deletePayment(payment);
    }
}
