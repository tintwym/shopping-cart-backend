package com.shopping.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import com.shopping.cart.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment,UUID>{
	public Payment findPaymentByStripeSessionId(String sessionId);
	
	public List<Payment> findPaymentByUserId(Integer userId);
	
	public long deleteByUserIdAndPaymentStatus(Integer userId, int paymentStatus);
}
