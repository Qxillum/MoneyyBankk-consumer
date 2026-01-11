package com.example.messaging;

import jakarta.jms.*;

public final class UserCreatedConsumer {

    private UserCreatedConsumer() {}

    public static void start() {
        ConnectionFactory factory = Jms.connectionFactory();

        // On ne ferme pas le context : le consumer doit rester actif tant que l'app tourne
        JMSContext ctx = factory.createContext();
        Queue queue = ctx.createQueue("UserCreatedQueue");
        JMSConsumer consumer = ctx.createConsumer(queue);

        consumer.setMessageListener(msg -> {
            try {
                if (msg instanceof TextMessage tm) {
                    System.out.println("📥 Consumer received: " + tm.getText());
                } else {
                    System.out.println("📥 Consumer received message: " + msg);
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        System.out.println("✅ Consumer listening on UserCreatedQueue");
    }
}
