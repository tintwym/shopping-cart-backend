package com.shopping.cart.service;

import com.shopping.cart.dto.request.AddReviewRequest;
import com.shopping.cart.entity.OrderItem;
import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.Review;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IReviewService;
import com.shopping.cart.repository.OrderItemRepository;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.ReviewRepository;
import com.shopping.cart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService implements IReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserService userService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, ProductRepository productRepository, UserRepository userRepository, OrderItemRepository orderItemRepository, UserService userService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderItemRepository = orderItemRepository;
        this.userService = userService;
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
    public List<Review> getAllReviewsForProduct(UUID productId) {
        return reviewRepository.findByProductId(productId);
    }

    @Override
    public void addReview(String token, AddReviewRequest addReviewRequest) {
        // Get the user ID from the token
        User authUser = userService.getUserFromToken(token);

        // Find the product, user, and order item by their IDs
        Product product = productRepository.findById(addReviewRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepository.findById(authUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        OrderItem orderItem = orderItemRepository.findById(addReviewRequest.getOrderItemId())
                .orElseThrow(() -> new RuntimeException("Order item not found"));

        // Create and save the new review
        Review review = new Review(
                addReviewRequest.getComment(),
                addReviewRequest.getRating(),
                product,
                user,
                orderItem
        );

        reviewRepository.save(review);
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
