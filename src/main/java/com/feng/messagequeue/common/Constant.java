package com.feng.messagequeue.common;

public class Constant {
    // RabbitMQ
    // Queue
    public static final String QUEUE_NAME = "crochetQueue";
    public static final String DEAD_LETTER_QUEUE_NAME = "deadLetterQueue";
    // Exchange
    public static final String EXCHANGE_NAME = "crochetExchange";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "deadLetterExchange";
    // Routing key
    public static final String ROUTING_KEY = "crochet";
    public static final String DEAD_LETTER_ROUTING_KEY = "deadLetter";
    // Kafka
    public static final String GROUP_ID = "little-crochet-group";
    public static final String RETRY_TOPIC = "little-crochet-retry";
    public static final String DEAD_LETTER_TOPIC = "little-crochet-dlt";
    public static final String AUTO_COMMIT_INTERVAL_MS = "100";
    public static final String EARLIEST = "earliest";
    public static final int RETRY_NUMBER = 3;
    public static final int RETRY_BACKOFF_MS = 1000;
    public static final String ACKS = "all";
    public static final int NUM_CONCURRENCY = 3;
    public static final int RETRY_MAX_ATTEMPT = 5;

    private Constant() {
    }
}
