package com.shopping.cart.service;

import java.util.UUID;

import com.shopping.cart.entity.Payment;
import com.shopping.cart.interfaces.IPaymentService;
import com.shopping.cart.repository.PaymentRepository;



public class PaymentService implements IPaymentService{
	private final PaymentRepository paymentRepository;
	
	public PaymentService(PaymentRepository paymentRepository ) {
		this.paymentRepository = paymentRepository;;
		
	}
	
	
	@Override
	public Payment findById(String uuid) {
		Payment payment = paymentRepository.findById(UUID.fromString(uuid)).orElse(null);
		return payment;
	}

	
	@Override
	public void deletePayment(Payment payment) {
		paymentRepository.delete(payment);
	}


	@Override
	public Payment createPayment(Payment payment) {
		paymentRepository.save(payment);
		return payment;
	}


	@Override
	public Payment updatePayment(Payment payment) {
		paymentRepository.save(payment);
		return payment;
	}

}
