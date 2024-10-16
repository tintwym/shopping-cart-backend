package com.shopping.cart.filter;

import com.shopping.cart.utility.JwtUtility;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtility jwtUtility;

    public JwtAuthenticationFilter(JwtUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String jwt = null;
        String username = null;

        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);  // Extract the token by removing "Bearer " prefix
            try {
                username = jwtUtility.extractUsername(jwt);  // Extract the username from the token
            } catch (Exception e) {
                logger.error("Error extracting username from JWT: " + e.getMessage());
            }
        }

        // Validate the token
        if (username != null && jwtUtility.isTokenValid(jwt, username)) {
            // You can set user info in the request here if needed
            request.setAttribute("username", username);

            // Check if token needs refreshing
            if (jwtUtility.shouldRefreshToken(jwt)) {
                // Generate a new token and add it to the response headers
                String newToken = jwtUtility.generateToken(username);
                response.setHeader("Authorization", "Bearer " + newToken);
            }
        } else {
            // If token is invalid, return a 401 Unauthorized response
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;  // Stop further filter chain
        }

        filterChain.doFilter(request, response);
    }
}
