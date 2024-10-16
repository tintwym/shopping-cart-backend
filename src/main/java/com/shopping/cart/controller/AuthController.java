package com.shopping.cart.controller;

import com.shopping.cart.dto.request.LoginAdminRequest;
import com.shopping.cart.dto.request.RegisterAdminRequest;
import com.shopping.cart.entity.User;
import com.shopping.cart.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class AuthController {
    private final UserService userService;
    private final HttpSession session;

    @Autowired
    public AuthController(UserService userService, HttpSession session) {
        this.userService = userService;
        this.session = session;
    }

    @GetMapping({"/", "/auth/login"})
    public String login() {
        return "auth/login";
    }

    @PostMapping("/auth/login")
    public String loginAdmin(@ModelAttribute LoginAdminRequest loginAdminRequest) {
        boolean loginSuccess = userService.loginAdmin(loginAdminRequest);

        if (loginSuccess) {
            // Get the user object by username
            User user = userService.getUserByUsername(loginAdminRequest.getUsername());

            // Add the username to the session
            session.setAttribute("username", user.getUsername());

            // Redirect to the dashboard
            return "redirect:/dashboard";
        }

        return "redirect:/auth/login";
    }

    @GetMapping("/auth/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String registerAdmin(@ModelAttribute RegisterAdminRequest registerAdminRequest) {
        boolean registerSuccess = userService.registerAdmin(registerAdminRequest);

        if (registerSuccess) {
            // Get the user object by username
            User user = userService.getUserByUsername(registerAdminRequest.getUsername());

            // Add the username to the session
            session.setAttribute("username", user.getUsername());

            // Redirect to the dashboard
            return "redirect:/dashboard";
        }

        return "redirect:/auth/register";
    }

    @GetMapping("/logout")
    public String logout() {
        session.removeAttribute("username");
        return "redirect:/auth/login";
    }
}
