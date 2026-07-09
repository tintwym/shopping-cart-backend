package com.shopping.cart.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Fails before Spring Boot opens a JDBC connection when Cloud Run is missing DATABASE_URL.
 */
public class ProductionDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isCloudRun()) {
            return;
        }
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (databaseUrl == null || databaseUrl.isBlank()) {
            throw new IllegalStateException(
                    "DATABASE_URL is not set. In Google Cloud Run → service → Variables, "
                            + "add your Neon PostgreSQL URL "
                            + "(postgresql://user:pass@host/db?sslmode=require), then redeploy.");
        }
    }

    private static boolean isCloudRun() {
        return isSet(System.getenv("K_SERVICE"));
    }

    private static boolean isSet(String value) {
        return value != null && !value.isBlank();
    }
}
