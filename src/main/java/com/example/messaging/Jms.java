package com.example.messaging;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQJMSConnectionFactory;

public final class Jms {
    private Jms() {}

    public static ConnectionFactory connectionFactory() {
        return new ActiveMQJMSConnectionFactory("tcp://localhost:61616", "admin", "admin");
    }
}
