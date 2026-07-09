package com.shopping.cart.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails before Spring Boot opens a JDBC connection when Railway is missing DATABASE_URL.
 */
public class ProductionDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isRailway()) {
            return;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "DATABASE_URL is not set. On Railway → Variables, add your Neon PostgreSQL URL "
                            + "(postgresql://user:pass@host/db?sslmode=require), then redeploy.");
        }
    }

    private static boolean isRailway() {
        return isSet(System.getenv("RAILWAY_ENVIRONMENT"))
                || isSet(System.getenv("RAILWAY_SERVICE_ID"));
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
