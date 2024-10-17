package com.shopping.cart.controller.api;

import com.shopping.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
public class CartApiController {
    private final CartService cartService;

    @Autowired
    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartCount(@RequestHeader("Authorization") String token) {
        int count = cartService.getCartItemCount(token);
        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);

        // Add Cache-Control header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-store, must-revalidate");

        return ResponseEntity.ok().headers(headers).body(response);
    }

    @GetMapping("/show")
    public ResponseEntity<?> getCartByUser(@RequestHeader(value = "Authorization", required = false) String token) {
        // Add Cache-Control header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-store, must-revalidate");

        return ResponseEntity.ok().headers(headers).body(cartService.getCartByUser(token));
    }

    @PostMapping("/store")
    public ResponseEntity<?> addProductToCart(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam UUID productId, @RequestParam int quantity) {
        cartService.addProductToCart(token, productId, quantity);

        // Add Cache-Control header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-store, must-revalidate");

        return ResponseEntity.ok().headers(headers).build();
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProductInCart(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam UUID productId, @RequestParam int quantity) {
        cartService.updateProductInCart(token, productId, quantity);

        // Add Cache-Control header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-store, must-revalidate");

        return ResponseEntity.ok().headers(headers).build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteProductFromCart(@RequestHeader(value = "Authorization", required = false) String token, @RequestParam UUID productId) {
        cartService.deleteProductFromCart(token, productId);

        // Add Cache-Control header
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-store, must-revalidate");

        return ResponseEntity.ok().headers(headers).build();
    }
}