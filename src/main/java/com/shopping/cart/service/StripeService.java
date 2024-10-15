package com.shopping.cart.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.shopping.cart.entity.Cart;
import com.shopping.cart.entity.CartItem;
import com.shopping.cart.entity.Product;
import com.shopping.cart.interfaces.IStripeService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.checkout.Session;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;

import jakarta.annotation.PostConstruct;

@Service
public class StripeService implements IStripeService{

	@Value("${stripe.api.key}")
	private String stripeApiKey;
	
	@PostConstruct
	public void init() {
		Stripe.apiKey = stripeApiKey;
	}
	
	@Override
    public Session createStripeSession(List<SessionCreateParams.LineItem> lineItems, String paymentId) throws StripeException {
        System.out.println("inside the creation function");
		SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/checkout/success?paymentId=" + paymentId)  
                .setCancelUrl("http://localhost:8080/checkout/cancel?paymentId=" + paymentId)   
                .addAllLineItem(lineItems)
                .build();
		
        return Session.create(params);
    }
	
	@Override
    public List<SessionCreateParams.LineItem> convertToStripeItem(List<CartItem> cartItems) {
		List<SessionCreateParams.LineItem> stripeLineItems = cartItems.stream().map(item  ->
		SessionCreateParams.LineItem.builder()
		.setQuantity(item.getQuantity()*1L)
		.setPrice(item.getProduct().getStripeId())
		.build()
			)
		.collect(Collectors.toList());
		
		return stripeLineItems;
		
    }

	@Override
	public boolean verifyStripeSession(String sessionId) throws StripeException {
		// TODO Auto-generated method stub
		Session session =  Session.retrieve(sessionId);
		
		if(session.getPaymentStatus()=="paid") {
			return true;
		}
		
		
		return false;
	}

	@Override
	public Product createStripeProduct(Product product) throws StripeException {
		// TODO Auto-generated method stub
		long unitAmount = product.getPrice().multiply(BigDecimal.valueOf(100)).longValue();
		PriceCreateParams params =
		  PriceCreateParams.builder()
		    .setCurrency("sgd")
		    .setUnitAmount(unitAmount)
		    .setProductData(
		      PriceCreateParams.ProductData.builder().setName(product.getName()).build()
		    )
		    .build();
		Price price = Price.create(params);
		product.setStripeId(price.getId());
		
		return product;
	}

	@Override
	public Product updateStripeProduct(Product product) throws StripeException {
		// TODO Auto-generated method stub
		// Stripe does not allow to update the price of the price object. So create new one we can try to work
		//	with the product object in the future in order to fix the reference between stripe and our app
		long unitAmount = product.getPrice().multiply(BigDecimal.valueOf(100)).longValue();
		PriceCreateParams params =
				  PriceCreateParams.builder()
				    .setCurrency("sgd")
				    .setUnitAmount(unitAmount)
				    .setProductData(
				      PriceCreateParams.ProductData.builder().setName(product.getName()).build()
				    )
				    .build();
				Price price = Price.create(params);
				product.setStripeId(price.getId());
		return product;
	}

}
