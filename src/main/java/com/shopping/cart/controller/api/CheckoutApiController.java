package com.shopping.cart.controller.api;

import com.shopping.cart.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutApiController {
    private final CheckoutService checkoutService;

    @Autowired
    public CheckoutApiController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestHeader("Authorization") String token) {
        try {
            String sessionId = checkoutService.checkout(token);
            // Return the sessionId as part of a JSON object
            return ResponseEntity.ok().body(Map.of("sessionId", sessionId)); // Send the sessionId back to the client
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating checkout session: " + e.getMessage());
        }
    }

    @PostMapping("/complete-order")
    public ResponseEntity<?> completeOrder(@RequestHeader("Authorization") String token) {
        try {
            checkoutService.completeOrder(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error completing order: " + e.getMessage());
        }
    }
}
