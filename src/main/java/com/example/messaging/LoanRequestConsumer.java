package com.example.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LoanRequestConsumer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private LoanRequestConsumer() {}

    public static void start() {
        JMSContext ctx = Jms.connectionFactory().createContext();

        Queue queue = ctx.createQueue("LoanRequestQueue");
        JMSConsumer consumer = ctx.createConsumer(queue);

        consumer.setMessageListener(msg -> {
            try {
                if (!(msg instanceof TextMessage tm)) return;

                String json = tm.getText();
                @SuppressWarnings("unchecked")
                Map<String, Object> doc = MAPPER.readValue(json, Map.class);

                String requestId = (String) doc.get("requestId");
                Number amountN = (Number) doc.get("amount");
                double amount = amountN != null ? amountN.doubleValue() : 0.0;

                @SuppressWarnings("unchecked")
                Map<String, Object> stats = (Map<String, Object>) doc.get("stats");

                double balance = 0.0;
                if (stats != null && stats.get("balance") instanceof Number b) {
                    balance = b.doubleValue();
                }

                // Règle de décision simple (à adapter)
                boolean approved = balance >= (amount * 0.2); // ex: au moins 20% en "capacité"
                String reason = approved ? "APPROVED_BY_RULE" : "INSUFFICIENT_BALANCE_RULE";

                Map<String, Object> reply = new LinkedHashMap<>();
                reply.put("type", "LOAN_DECISION");
                reply.put("requestId", requestId);
                reply.put("timestamp", Instant.now().toString());
                reply.put("approved", approved);
                reply.put("reason", reason);
                reply.put("balance", balance);
                reply.put("amount", amount);

                Destination replyTo = msg.getJMSReplyTo();
                if (replyTo != null) {
                    String replyJson = MAPPER.writeValueAsString(reply);
                    ctx.createProducer()
                            .setJMSCorrelationID(requestId)
                            .send(replyTo, replyJson);
                }

                System.out.println("📥 LOAN_REQUEST handled, requestId=" + requestId + ", approved=" + approved);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        System.out.println("✅ Consumer listening on LoanRequestQueue");
    }
}
