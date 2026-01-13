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

        JMSContext ctx = factory.createContext();

        Queue queue = ctx.createQueue("UserCreatedQueue");
        JMSConsumer consumer = ctx.createConsumer(queue);

        consumer.setMessageListener(msg -> {
            String payload = "";
            String correlationId = "<none>";
            try {
                if (!(msg instanceof TextMessage tm)) {
                    System.out.println("📥 Non-text JMS message: " + msg);
                    return;
                }

                String json = tm.getText();
                JsonNode node = MAPPER.readTree(json);

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

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        System.out.println("✅ Consumer listening on UserCreatedQueue");
    }

    private static void validatePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("Payload is empty");
        }

        // Exemple de règles: adapte à ton projet
        boolean ok = payload.startsWith("UserCreated:") ||
                payload.startsWith("Informations statistiques:");

        if (!ok) {
            throw new IllegalArgumentException("Unknown message format/prefix");
        }
    }
}
