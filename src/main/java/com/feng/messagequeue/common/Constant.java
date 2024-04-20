package com.feng.messagequeue.common;

public class Constant {
    public static final String GROUP_ID = "group-id";
    public static final String TOPIC_NAME = "little-crochet";
    public static final String RETRY_TOPIC = "little-crochet-retry";
    public static final String DEAD_LETTER_TOPIC = "little-crochet-dlt";
    public static final int PARTITION_NUMBER = 6;
    public static final int REPLICAS_NUMBER = 3;
    public static final String QUEUE_NAME = "crochet.queue";
    public static final String DEAD_LETTER_QUEUE_NAME = "deadLetterQueue";
    public static final String EXCHANGE_NAME = "crochet-direct-exchange";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "deadLetterExchange";
    public static final String DEAD_LETTER_ROUTING_KEY = "deadLetter";
    public static final String ROUTING_KEY = "crochet";
    private Constant() {
    }
}
