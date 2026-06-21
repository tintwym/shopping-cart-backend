package com.shopping.cart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CorsOrigins {

    private final List<String> origins;

    public CorsOrigins(@Value("${app.frontend.base-url:}") String frontendBaseUrl) {
        List<String> allowed = new ArrayList<>();
        allowed.add("http://localhost:8081");
        allowed.add("http://localhost:8082");
        if (frontendBaseUrl != null && !frontendBaseUrl.isBlank()) {
            String base = frontendBaseUrl.endsWith("/")
                    ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                    : frontendBaseUrl;
            if (!allowed.contains(base)) {
                allowed.add(base);
            }
        }
        this.origins = List.copyOf(allowed);
    }

    public String[] asArray() {
        return origins.toArray(String[]::new);
    }
}
