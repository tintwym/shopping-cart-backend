package com.shopping.cart.interfaces;

import java.util.List;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.shopping.cart.entity.CartItem;
import com.shopping.cart.entity.Product;

public interface IStripeService {
	Session createStripeSession(List<SessionCreateParams.LineItem> lineItems, String paymentId) throws StripeException;
	
	List <SessionCreateParams.LineItem> convertToStripeItem(List<CartItem> cartItems);
	
	boolean verifyStripeSession(String sessionId) throws StripeException;
	
	Product createStripeProduct(Product product) throws StripeException;
	
	Product updateStripeProduct(Product product) throws StripeException;


}

