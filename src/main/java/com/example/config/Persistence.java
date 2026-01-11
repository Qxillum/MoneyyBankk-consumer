package com.example.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class Persistence {

    private static final EntityManagerFactory factory =
            jakarta.persistence.Persistence.createEntityManagerFactory("postgres");

    public static EntityManager getEntityManager() {
        return factory.createEntityManager();
    }
}
