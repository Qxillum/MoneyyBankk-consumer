package com.example.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;

public final class UserCreatedConsumer {

    private final InvalidMessageProducer invalidMessageProducer;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public UserCreatedConsumer() {
        this.invalidMessageProducer = new InvalidMessageProducer();
    }

    public void start() {
        ConnectionFactory factory = Jms.connectionFactory();

        JMSContext ctx = factory.createContext(JMSContext.CLIENT_ACKNOWLEDGE);

        Queue queue = ctx.createQueue("UserCreatedQueue");
        JMSConsumer consumer = ctx.createConsumer(queue);

        consumer.setMessageListener(msg -> {
            String sourceQueue = "UserCreatedQueue";
            String payload = "";
            try {
                if (!(msg instanceof TextMessage tm)) {
                    throw new IllegalArgumentException(
                            "  Not a Text-Message");
                }

                payload = tm.getText();
                JsonNode node = MAPPER.readTree(payload);

                validateUserCreatedPayload(node);

                Integer userId = node.hasNonNull("userId") ? node.get("userId").asInt() : null;
                String timestamp = node.hasNonNull("timestamp") ? node.get("timestamp").asText() : null;
                String nom = node.hasNonNull("nom") ? node.get("nom").asText() : null;
                String prenom = node.hasNonNull("prenom") ? node.get("prenom").asText() : null;
                String email = node.hasNonNull("email") ? node.get("email").asText() : null;

                System.out.println("📥 USER_CREATED received -> userId=" + userId
                        + ", timestamp=" + timestamp
                        + ", nom=" + nom
                        + ", prenom=" + prenom
                        + ", email=" + email);
                msg.acknowledge();
            } catch (Exception e) {
                String reason = e.getClass().getSimpleName() + ": " + e.getMessage();
                this.invalidMessageProducer.sendInvalidMessage(sourceQueue, payload, reason);
                try {
                    msg.acknowledge();
                } catch (JMSException e1) {
                    e1.printStackTrace();
                }
            }
        });

        System.out.println("Consumer listening on UserCreatedQueue");
    }

    private static void validateUserCreatedPayload(JsonNode node) {
        if (node == null)
            throw new IllegalArgumentException("Body is null");
        if (!node.hasNonNull("userId"))
            throw new IllegalArgumentException("Missing field: userId");
        if (!node.hasNonNull("timestamp"))
            throw new IllegalArgumentException("Missing field: timestamp");
        if (!node.hasNonNull("email"))
            throw new IllegalArgumentException("Missing field: email");
        if (node.get("userId").asInt(-1) <= 0)
            throw new IllegalArgumentException("Invalid userId");

        String email = node.get("email").asText();
        if (email.isBlank() || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email");
    }
}
