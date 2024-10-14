package com.shopping.cart.service;

import com.shopping.cart.entity.Review;
import com.shopping.cart.interfaces.IReviewService;
import com.shopping.cart.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @Override
    public Review getReview(UUID id) {
        return reviewRepository.findById(id).orElse(null);
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public void addReview() {
        System.out.println("Add review");
    }

    @Override
    public void updateReview() {
        System.out.println("Update review");
    }

    @Override
    public void deleteReview() {
        System.out.println("Delete review");
    }
}
