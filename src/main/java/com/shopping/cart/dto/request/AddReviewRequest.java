package com.shopping.cart.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddReviewRequest {
    private String comment;
    private int rating;
    private UUID productId;
    private UUID orderItemId;
}
