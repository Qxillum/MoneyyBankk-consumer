package com.example.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        packages("com.example.resources",
                "com.example.filters",
                "com.example.interceptors",
                "com.example.errors");
        register(JacksonFeature.class);
        register(OpenApiResource.class);
        property("jersey.config.beanValidation.enableOutputValidationErrorEntity.server", true);
    }
}
