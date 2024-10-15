package com.shopping.cart.controller.api;

import com.shopping.cart.service.CartService;
import com.shopping.cart.service.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CheckoutApiController {
    private final CheckoutService checkoutService;

    @Autowired
    public CheckoutApiController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public void checkout(@RequestHeader("Authorization") String token) {
        checkoutService.initiateCheckout(token);
    }
}
