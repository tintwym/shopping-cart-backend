package com.shopping.cart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopping.cart.dto.request.ChangePasswordRequest;
import com.shopping.cart.dto.request.LoginAdminRequest;
import com.shopping.cart.dto.request.LoginUserRequest;
import com.shopping.cart.dto.request.RegisterAdminRequest;
import com.shopping.cart.dto.request.RegisterUserRequest;
import com.shopping.cart.dto.response.AuthResponse;
import com.shopping.cart.entity.Role;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.IUserService;
import com.shopping.cart.mapper.UserMapper;
import com.shopping.cart.repository.RoleRepository;
import com.shopping.cart.repository.UserRepository;
import com.shopping.cart.utility.JwtUtility;
import com.shopping.cart.utility.PasswordHashingUtility;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.UUID;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtility jwtUtility;

    public UserService(JwtUtility jwtUtility, UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtility = jwtUtility;
    }

    @Override
    public boolean verifyToken(String token, String usernameJson) {
        // Remove the "Bearer " prefix from the token
        token = token.replace("Bearer ", "");

        try {
            // Convert the JSON string to extract the actual username
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(usernameJson);

            // Extract the "username" field from the JSON
            String username = jsonNode.get("username").asText();

            // Check if the token is valid using the extracted username
            return jwtUtility.isTokenValid(token, username);
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract username from JSON", e);
        }
    }

    @Override
    public User getUserFromToken(String token) {
        // Remove the "Bearer " prefix from the token
        token = token.replace("Bearer ", "");

        // Get the username from the token
        String username = jwtUtility.extractUsername(token);

        // Find the user by username
        return userRepository.findByUsername(username);
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(Objects.requireNonNull(id)).orElse(null);
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public AuthResponse registerUser(RegisterUserRequest registerUserRequest) {
        // Hash the password before saving it to the database
        registerUserRequest.setPassword(PasswordHashingUtility.hashPassword(registerUserRequest.getPassword()));

        // Map the RegisterUserRequest to a User entity
        User user = UserMapper.fromRegisterUserRequest(registerUserRequest);

        // Fetch the role from the role service
        Role role = roleRepository.findByName("User");
        user.setRole(role);

        // Save the user to the database
        userRepository.save(user);

        // Generate JSON Web Token
        String token = jwtUtility.generateToken(user.getUsername());

        // Return the AuthResponse object with the token
        return new AuthResponse(token);
    }

    @Override
    public boolean registerAdmin(RegisterAdminRequest registerAdminRequest) {
        // Hash the password before saving it to the database
        registerAdminRequest.setPassword(PasswordHashingUtility.hashPassword(registerAdminRequest.getPassword()));

        // Map the RegisterUserRequest to a User entity
        User user = UserMapper.fromRegisterAdminRequest(registerAdminRequest);

        // Fetch the role from the role service
        Role role = roleRepository.findByName("Admin");
        user.setRole(role);

        // Save the user to the database
        userRepository.save(user);

        return true;
    }

    @Override
    public AuthResponse loginUser(LoginUserRequest loginUserRequest) {
        // Find the user by username
        User user = userRepository.findByUsername(loginUserRequest.getUsername());

        if (user == null || user.getRole() == null || !Objects.equals(user.getRole().getName(), "User")) {
            return null;
        }

        if (PasswordHashingUtility.verifyPassword(loginUserRequest.getPassword(), user.getPassword())) {
                // Generate JSON Web Token
                String token = jwtUtility.generateToken(user.getUsername());

                // Return the AuthResponse object with the token
                return new AuthResponse(token);
        }

        return null;
    }

    @Override
    public boolean loginAdmin(LoginAdminRequest loginAdminRequest) {
        // Find the user by username
        User user = userRepository.findByUsername(loginAdminRequest.getUsername());

        // Check if the user exists and the password is correct
        return user != null && user.getRole().getName().equals("Admin") && PasswordHashingUtility.verifyPassword(loginAdminRequest.getPassword(), user.getPassword());
    }

    @Override
    public void changePassword(String token, ChangePasswordRequest request) {
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()
                || request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current and new password are required");
        }
        if (request.getNewPassword().length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be at least 8 characters");
        }

        User user = getUserFromToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }
        if (!PasswordHashingUtility.verifyPassword(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.setPassword(PasswordHashingUtility.hashPassword(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public User requireAdmin(String authorizationHeader) {
        User user = getUserFromToken(normalizeBearer(authorizationHeader));
        if (!isAdmin(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
        return user;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getRole() != null
                && "Admin".equalsIgnoreCase(user.getRole().getName());
    }

    private static String normalizeBearer(String authorizationHeader) {
        if (authorizationHeader == null) {
            return "";
        }
        return authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
    }
}
