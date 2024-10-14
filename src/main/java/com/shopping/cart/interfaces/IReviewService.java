package com.shopping.cart.interfaces;

import com.shopping.cart.entity.Review;

import java.util.List;
import java.util.UUID;

public interface IReviewService {
    Review getReview(UUID id);
    List<Review> getAllReviews();

    void addReview();
    void deleteReview();
    void updateReview();
}
