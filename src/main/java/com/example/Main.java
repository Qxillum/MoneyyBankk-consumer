package com.example;

import com.example.messaging.LoanRequestConsumer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import com.example.config.ApplicationConfig;
import java.net.URI;

public class Main {
    public static final String BASE_URI = "http://localhost:8081/api/";
    public static void main(String[] args) throws Exception {
        GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), new ApplicationConfig());
        System.out.println("Api server is starting on " + BASE_URI);
        com.example.messaging.UserCreatedConsumer.start();
        LoanRequestConsumer.start();
        Thread.currentThread().join();
    }
}


