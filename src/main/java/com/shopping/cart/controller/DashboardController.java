package com.shopping.cart.controller;

import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.User;
import com.shopping.cart.service.ProductService;
import com.shopping.cart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {
    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public DashboardController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Retrieve the username from the session
        String username = (String) session.getAttribute("username");

        // Check if the username exists in the session
        if (username != null) {
            // Find the user by the username
            User user = userService.getUserByUsername(username);

            // Add the user object to the model
            model.addAttribute("user", user);
        }

        // Retrieve and add products to the model
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Return the dashboard view
        return "dashboard";
    }

}
