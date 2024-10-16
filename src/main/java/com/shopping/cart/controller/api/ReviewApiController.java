package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.AddReviewRequest;
import com.shopping.cart.entity.Review;
import com.shopping.cart.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewApiController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // Get all reviews for a product
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getAllReviewsForProduct(@PathVariable UUID productId) {
        List<Review> reviews = reviewService.getAllReviewsForProduct(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/show")
    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/store")
    public ResponseEntity<?> addReview(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody AddReviewRequest addReviewRequest) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body("Authorization token is missing.");
        }

        // Call the service method to add a review
        reviewService.addReview(token, addReviewRequest);
        return ResponseEntity.ok("Review added successfully");
    }
}
