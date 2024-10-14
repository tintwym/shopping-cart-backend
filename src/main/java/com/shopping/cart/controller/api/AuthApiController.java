package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.LoginUserRequest;
import com.shopping.cart.dto.request.RegisterUserRequest;
import com.shopping.cart.dto.response.AuthResponse;
import com.shopping.cart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {
    private final UserService userService;

    @Autowired
    public AuthApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest registerUserRequest) {
        // Call the registerUser method from the userService
        AuthResponse authResponse = userService.registerUser(registerUserRequest);

        // Return a ResponseEntity with the AuthResponse object
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginUserRequest loginUserRequest) {
        // Call the loginUser method from the userService
        AuthResponse authResponse = userService.loginUser(loginUserRequest);

        if (authResponse == null) {
            // Return a ResponseEntity with a 401 Unauthorized status code
            return ResponseEntity.status(401).build();
        }

        // Return a ResponseEntity with the AuthResponse object
        return ResponseEntity.ok(authResponse);
    }
}
