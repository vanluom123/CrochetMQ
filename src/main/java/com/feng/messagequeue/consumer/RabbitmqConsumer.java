package com.feng.messagequeue.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class RabbitmqConsumer {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Channel channel;

    public <T> T consume(String queueName, Class<T> classType) {
        CompletableFuture<T> future = new CompletableFuture<>();
        try {
            channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                try {
                    T object = objectMapper.readValue(message, classType);
                    future.complete(object);
                    log.info("Received message: {}", message);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }, consumerTag -> {
            });
        } catch (IOException e) {
            future.completeExceptionally(e);
        }
        return future.join();
    }
}
