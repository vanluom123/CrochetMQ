package com.feng.messagequeue.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class RabbitmqConsumer {
    private final ObjectMapper objectMapper;
    private final Channel channel;

    public RabbitmqConsumer(ObjectMapper objectMapper, Channel channel) {
        this.objectMapper = objectMapper;
        this.channel = channel;
    }

    public <T> T consume(String queueName, Class<T> classType) {
        BlockingQueue<T> queue = new LinkedBlockingQueue<>();
        try {
            channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    T object = objectMapper.readValue(message, classType);
                    queue.put(object);
                    log.info("Received message: {}", message);
                } catch (Exception e) {
                    log.error("Error processing message: {}", e.getMessage());
                    throw new RuntimeException("Error processing message", e);
                }
            }, consumerTag -> {});
        } catch (IOException e) {
            log.error("Error consuming message: {}", e.getMessage());
        }
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error waiting for message", e);
        }
    }
}
