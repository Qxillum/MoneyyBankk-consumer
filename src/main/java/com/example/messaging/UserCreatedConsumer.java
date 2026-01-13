package com.example.messaging;

import jakarta.jms.*;

public final class UserCreatedConsumer {

    private final InvalidMessageProducer invalidMessageProducer;

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
                correlationId = msg.getJMSCorrelationID();
                if (msg instanceof TextMessage tm) {
                    payload = tm.getText();
                } else {
                    throw new IllegalArgumentException(
                            "Expected TextMessage but got: " + msg.getClass().getSimpleName());
                }
                System.out.println("📥 Consumer received: " + payload);
                System.out.println("    Verif= 1" + correlationId);

                System.out.println(payload == null || payload.isBlank());
                validatePayload(payload);
                System.out.println("    Verif= 2" + correlationId);

            } catch (Exception e) {
                this.invalidMessageProducer.sendInvalidMessage("UserCreatedQueue", payload, e.getMessage());
                System.out.println("🚫 Invalid message -> InvalidMessageQueue. Reason: " + e.getMessage());
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
