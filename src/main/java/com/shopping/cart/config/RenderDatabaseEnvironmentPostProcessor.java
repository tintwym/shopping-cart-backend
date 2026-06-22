package com.shopping.cart.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails before Spring Boot opens a JDBC connection when Render is missing DATABASE_URL.
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isRender()) {
            return;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "DATABASE_URL is not set. On Render → Environment, add your Neon PostgreSQL URL "
                            + "(postgresql://user:pass@host/db?sslmode=require), then redeploy.");
        }
    }

    private static boolean isRender() {
        return isSet(System.getenv("RENDER")) || isSet(System.getenv("RENDER_SERVICE_ID"));
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
