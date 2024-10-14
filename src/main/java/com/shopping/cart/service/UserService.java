package com.shopping.cart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.stereotype.Service;

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
        return userRepository.findById(id).orElse(null);
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
        Role role = roleRepository.findByName("Admin"); // Replace "Admin" with the appropriate role name if necessary
        user.setRole(role);

        // Save the user to the database
        userRepository.save(user);

        return true;
    }

    @Override
    public AuthResponse loginUser(LoginUserRequest loginUserRequest) {
        // Find the user by username
        User user = userRepository.findByUsername(loginUserRequest.getUsername());

        // Check if the user exists and the password is correct
        if (user != null && PasswordHashingUtility.verifyPassword(loginUserRequest.getPassword(), user.getPassword())) {
            // Generate JSON Web Token
            String token = jwtUtility.generateToken(user.getUsername());

            // Return the AuthResponse object with the token
            return new AuthResponse(token);
        }

        // Return null if the user does not exist or the password is incorrect
        return null;
    }

    @Override
    public boolean loginAdmin(LoginAdminRequest loginAdminRequest) {
        // Find the user by username
        User user = userRepository.findByUsername(loginAdminRequest.getUsername());

        // Check if the user exists and the password is correct
        return user != null && user.getRole().getName().equals("Admin") && PasswordHashingUtility.verifyPassword(loginAdminRequest.getPassword(), user.getPassword());
    }
}
