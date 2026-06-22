package com.shopping.cart.repository;

import com.shopping.cart.entity.Cart;
import com.shopping.cart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Cart findByUser(User user);

    @Query("SELECT DISTINCT c FROM Cart c "
            + "LEFT JOIN FETCH c.cartItems ci "
            + "LEFT JOIN FETCH ci.product p "
            + "LEFT JOIN FETCH p.images "
            + "WHERE c.user = :user")
    Cart findByUserWithItems(@Param("user") User user);
}
