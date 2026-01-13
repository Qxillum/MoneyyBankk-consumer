package com.example.messaging;
import jakarta.jms.*;

import java.time.Instant;


public class InvalidMessageProducer {
    public void sendInvalidMessage(String sourceQueue, String originalPayload, String errorReason) {
        ConnectionFactory factory = Jms.connectionFactory();

        // Petit "enveloppe" JSON (simple, sans Jackson) pour garder l'info exploitable
        String invalidPayload = "{"
                + "\"sourceQueue\":\"" + escape(sourceQueue) + "\","
                + "\"receivedAt\":\"" + Instant.now().toString() + "\","
                + "\"errorReason\":\"" + escape(errorReason) + "\","
                + "\"originalPayload\":\"" + escape(originalPayload) + "\""
                + "}";

        try (JMSContext ctx = factory.createContext()) {
            Queue invalidQueue = ctx.createQueue("InvalidMessageQueue");
            ctx.createProducer().send(invalidQueue, invalidPayload);

            System.out.println("🚫 Invalid message sent to InvalidMessageQueue: " + invalidPayload);
        }
    }

    // Evite de casser le JSON si le message contient des guillemets / backslashes.
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
