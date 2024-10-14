package com.shopping.cart.repository;

import com.shopping.cart.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    // Custom query to fetch only products where isDeleted is false
    List<Product> findByIsDeletedFalse();
}
