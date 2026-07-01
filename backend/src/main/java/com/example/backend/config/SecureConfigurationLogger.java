package com.example.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SecureConfigurationLogger implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecureConfigurationLogger.class);

    private final Environment environment;

    public SecureConfigurationLogger(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        LOGGER.info("DB_URL configured: {}", isConfigured("spring.datasource.url"));
        LOGGER.info("BREVO_SMTP_KEY configured: {}", isConfigured("spring.mail.password"));
        LOGGER.info("JWT_SECRET configured: {}", isConfigured("jwt.secret"));
    }

    private boolean isConfigured(String propertyName) {
        String value = environment.getProperty(propertyName);
        return value != null && !value.isBlank();
    }
}
