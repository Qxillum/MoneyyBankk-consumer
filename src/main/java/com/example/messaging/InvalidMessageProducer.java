package com.example.messaging;

import jakarta.jms.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InvalidMessageProducer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void sendInvalidMessage(String sourceQueue, String originalPayload, String errorReason) {
        ConnectionFactory factory = Jms.connectionFactory();

        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("sourceQueue", sourceQueue);
        doc.put("receivedAt", Instant.now().toString()); // ISO-8601 UTC
        doc.put("errorReason", errorReason);
        doc.put("originalPayload", originalPayload);

        try (JMSContext ctx = factory.createContext(JMSContext.AUTO_ACKNOWLEDGE)) {
            String json = MAPPER.writeValueAsString(doc);
         

            Queue invalidQueue = ctx.createQueue("InvalidMessageQueue");
            ctx.createProducer().send(invalidQueue, json);

            System.out.println("Invalid message sent to InvalidMessageQueue: " + json);
        }
        catch (JsonProcessingException e) {
                e.printStackTrace();
            }
    }
}
