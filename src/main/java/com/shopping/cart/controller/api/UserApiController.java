package com.shopping.cart.controller.api;

import com.shopping.cart.entity.User;
import com.shopping.cart.service.UserService;
import com.shopping.cart.utility.JwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserApiController {
    private final UserService userService;

    @Autowired
    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody String usernameJson) {
        // Check if the token is present
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token is missing or invalid");
        }

        // Verify the token
        boolean isValid = userService.verifyToken(token, usernameJson);

        // Return a 200 OK response if the token is valid, otherwise return a 401 Unauthorized response
        return isValid ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/token")
    public ResponseEntity<User> getUserFromToken(@RequestHeader(value = "Authorization", required = false) String token) {
        // Check if the token is present
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token is missing or invalid");
        }

        // Get the user from the JWT token
        User user = userService.getUserFromToken(token);

        // Return the user and a 200 OK response
        return ResponseEntity.ok(user);
    }

    @GetMapping("show/{id}")
    public ResponseEntity<?> show(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
