package com.feng.messagequeue.producer.rabbitmq;

import com.feng.messagequeue.common.Constant;
import com.feng.messagequeue.event.BaseEvent;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RabbitmqProducer {
    @Value("${rabbitmq.ttl:60000}")
    int ttl;

    final AmqpTemplate amqpTemplate;

    final ConnectionFactory connectionFactory;

    public RabbitmqProducer(AmqpTemplate amqpTemplate, ConnectionFactory connectionFactory) {
        this.amqpTemplate = amqpTemplate;
        this.connectionFactory = connectionFactory;
    }

    public void send(BaseEvent event) {
        log.info("Send msg: {}", event);
        amqpTemplate.convertAndSend(Constant.EXCHANGE_NAME, Constant.ROUTING_KEY, event);
    }

    public void send(String message, String exchange, String queueName, String routingKey) {
        Connection connection = connectionFactory.createConnection();
        Channel channel = connection.createChannel(false);
        try {
            // Create the WorkQueue
            channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT, true);
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, exchange, routingKey, null);

            // Create the RetryQueue
            Map<String, Object> arguments = new HashMap<>();
            arguments.put("x-dead-letter-exchange", exchange);
            arguments.put("x-dead-letter-routing-key", routingKey);
            arguments.put("x-message-ttl", ttl);
            channel.exchangeDeclare(Constant.DEAD_LETTER_EXCHANGE_NAME, BuiltinExchangeType.DIRECT, true);
            channel.queueDeclare(Constant.DEAD_LETTER_QUEUE_NAME, true, false, false, arguments);
            channel.queueBind(Constant.DEAD_LETTER_QUEUE_NAME, Constant.DEAD_LETTER_EXCHANGE_NAME, Constant.DEAD_LETTER_ROUTING_KEY, null);

            // Publish the message
            log.info("[{}] [Work] [Send]: {}", LocalDateTime.now(), message);
            channel.basicPublish(exchange, routingKey, null, message.getBytes());
        } catch (IOException e) {
            log.error("Error sending message: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        connection.close();
    }

    public void send(String message) {
        send(message, Constant.EXCHANGE_NAME, Constant.QUEUE_NAME, Constant.ROUTING_KEY);
    }
}
