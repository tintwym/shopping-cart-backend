package com.shopping.cart.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {
    private String comment;
    private int rating;

    @OneToOne // One review belongs to one product
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    // Default constructor is required by JPA
    public Review() {}

    // Parameterized constructor
    public Review(String comment, int rating) {
        this.comment = comment;
        this.rating = rating;
    }
}
