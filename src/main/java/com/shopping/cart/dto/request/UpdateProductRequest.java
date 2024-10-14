package com.shopping.cart.dto.request;

import com.shopping.cart.entity.ProductImage;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
}
