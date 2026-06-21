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
            var session = checkoutService.checkout(token);
            return ResponseEntity.ok().body(Map.of(
                    "sessionId", session.getSessionId(),
                    "checkoutUrl", session.getCheckoutUrl()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating checkout session: " + e.getMessage());
        }
    }

    @PostMapping("/checkout/confirm")
    public ResponseEntity<?> confirmCheckout(@RequestHeader("Authorization") String token,
                                             @RequestParam("sessionId") String sessionId) {
        try {
            checkoutService.confirmCheckoutSession(token, sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error confirming checkout: " + e.getMessage());
        }
    }
}
