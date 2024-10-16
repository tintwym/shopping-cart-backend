package com.shopping.cart.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public class SessionTimeoutFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        // Exclude specific URLs like login and registration from being checked for session timeout
        String requestURI = httpRequest.getRequestURI();

        // Allow access to login and other public pages
        if (!requestURI.contains("/") && (!requestURI.contains("/auth/login") && (!requestURI.contains("/auth/register") && (session == null || session.getAttribute("username") == null)))) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login");
            return;  // Return immediately to stop the chain
        }

        chain.doFilter(request, response);  // Continue request chain if session is valid or on public pages
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
