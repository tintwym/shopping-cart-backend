package com.shopping.cart.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Column(unique = true)
    private String name;

    private String description;
    private BigDecimal price;
    private int stock;
    private boolean isDeleted = false;

    // One-to-Many relationship with ProductImage
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;

    // Default constructor is required by JPA
    public Product() {}

    // Parameterized constructor
    public Product(String name, String description, BigDecimal price, int stock, boolean isDeleted, List<ProductImage> images) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.isDeleted = isDeleted;
        this.images = images;
    }
}
