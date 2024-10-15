package com.shopping.cart.interfaces;

import com.shopping.cart.entity.Payment;

public interface IPaymentService {
	Payment findById(String uuid);
	
	void deletePayment(Payment payment);
	
	Payment createPayment (Payment payment);
	
	Payment updatePayment(Payment payment);
}
