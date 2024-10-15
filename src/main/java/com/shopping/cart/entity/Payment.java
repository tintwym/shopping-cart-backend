package com.shopping.cart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
	
    // 0 - unpaid, 1 - paid, 2 - cancelled
    private int paymentStatus;
    private BigDecimal amount;
    private String stripeSessionId;
    @ManyToOne
    private User user;
    @OneToOne
    private Order order;
    // Default constructor is required by JPA
    public Payment() {}

    // Parameterized constructor
    public Payment(int paymentStatus, BigDecimal amount, User user) {
        this.paymentStatus = paymentStatus;
        this.amount = amount;
    }
}
