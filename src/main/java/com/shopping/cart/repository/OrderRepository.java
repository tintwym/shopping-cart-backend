package com.shopping.cart.repository;

import com.shopping.cart.entity.Order;
import com.shopping.cart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUser(User user);

    // Check if a pending order already exists for the user
    boolean existsByUserAndStatus(User user, String status);
}
