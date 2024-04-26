package com.feng.messagequeue.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feng.messagequeue.common.Constant;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class RabbitmqConsumer {
    int retryCount = 0;

    final ObjectMapper objectMapper;

    final ConnectionFactory connectionFactory;

    public RabbitmqConsumer(ObjectMapper objectMapper, ConnectionFactory connectionFactory) {
        this.objectMapper = objectMapper;
        this.connectionFactory = connectionFactory;
    }

    public <T> T consume(String queueName, Class<T> classType) {
        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(false);
        BlockingQueue<T> queue = new LinkedBlockingQueue<>();
        try {
            channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                log.info("[{}] [Received] [{}]: {}", LocalDateTime.now(), queueName, message);
                T object = objectMapper.readValue(message, classType);
                try {
                    queue.put(object);
                } catch (InterruptedException e) {
                    log.error("Error putting message in queue: {}", e.getMessage());
                    throw new RuntimeException(e);
                }
                if (retryCount < Constant.RETRY_MAX_ATTEMPT) {
                    publishToRetryExchange(channel, message);
                    retryCount++;
                } else {
                    retryCount = 0;
                }
            }, consumerTag -> {
            });
        } catch (IOException e) {
            log.error("Error consuming message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        connection.close();
        try {
            return queue.take();
        } catch (InterruptedException e) {
            log.error("Error waiting for message: {}", e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error waiting for message", e);
        }
    }

    // Publish to RetryQueue on failure
    private void publishToRetryExchange(Channel channel, String message) throws IOException {
        log.info("[{}] [Retry{}] [Re-Publish]: {}", LocalDateTime.now(), retryCount, message);
        channel.basicPublish(Constant.DEAD_LETTER_EXCHANGE_NAME, Constant.DEAD_LETTER_ROUTING_KEY, null, message.getBytes());
    }
}
