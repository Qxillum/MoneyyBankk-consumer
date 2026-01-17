package com.example.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.exceptions.IllegalArgumentException ;
import jakarta.jms.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LoanRequestConsumer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final InvalidMessageProducer invalidMessageProducer;

    public LoanRequestConsumer() {
        this.invalidMessageProducer = new InvalidMessageProducer();
    }

    public void start() {
        JMSContext ctx = Jms.connectionFactory().createContext(JMSContext.CLIENT_ACKNOWLEDGE);

        Queue queue = ctx.createQueue("LoanRequestQueue");
        JMSConsumer consumer = ctx.createConsumer(queue);

        consumer.setMessageListener(msg -> {
            String sourceQueue = "LoanRequestQueue";
            String json = "";
            try {
                if (!(msg instanceof TextMessage tm)) {
                    this.invalidMessageProducer.sendInvalidMessage(sourceQueue, String.valueOf(msg),
                            "  Not a Text-Message");
                    msg.acknowledge();
                    return;
                }
                json = tm.getText();
                JsonNode node = MAPPER.readTree(json);

                validateLoanRequestPayload(node);

                String requestId = node.hasNonNull("requestId") ? node.get("requestId").asText() : null;
                Number amountN = node.hasNonNull("amount") ? node.get("amount").asDouble() : null;
                double amount = amountN != null ? amountN.doubleValue() : 0.0;
                JsonNode statsNode = node.get("stats");

                if (statsNode == null || !statsNode.isObject()) {
                    throw new IllegalArgumentException("stats must be a JSON object");
                }

                if (!statsNode.hasNonNull("balance") || !statsNode.get("balance").isNumber()) {
                    throw new IllegalArgumentException("stats.balance missing or not numeric");
                }

                double balance = statsNode.hasNonNull("balance")
                        ? statsNode.get("balance").asDouble()
                        : 0.0;
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
                            .send(replyTo, replyJson);
                } else {
                    this.invalidMessageProducer.sendInvalidMessage(
                            sourceQueue,
                            json,
                            "Missing JMSReplyTo destination");
                }

                System.out.println("📥 LOAN_REQUEST handled, requestId=" + requestId + ", approved=" + approved);
                msg.acknowledge();

            } catch (Exception e) {
                String reason = e.getClass().getSimpleName() + ": " + e.getMessage();
                this.invalidMessageProducer.sendInvalidMessage(sourceQueue, json, reason);
                try {
                    msg.acknowledge();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
        });

        System.out.println("Consumer listening on LoanRequestQueue");
    }

    private static void validateLoanRequestPayload(JsonNode node) throws IllegalArgumentException {
        if (node == null)
            throw new IllegalArgumentException("Body is null");
        if (!node.hasNonNull("requestId"))
            throw new IllegalArgumentException("Missing field: requestId");
        if (!node.hasNonNull("timestamp"))
            throw new IllegalArgumentException("Missing field: timestamp");
        if (!node.hasNonNull("email"))
            throw new IllegalArgumentException("Missing field: email");
        if (node.get("requestId").asInt(-1) <= 0)
            throw new IllegalArgumentException("Invalid requestId");

        String email = node.get("email").asText();
        if (email.isBlank() || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email");

        if (!node.hasNonNull("amount") || !node.get("amount").isNumber())
            throw new IllegalArgumentException("Missing/invalid field: amount");
        if (node.get("amount").asDouble() <= 0)
            throw new IllegalArgumentException("Invalid amount");

    }
}
