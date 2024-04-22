package com.feng.messagequeue.producer.rabbitmq;

import com.feng.messagequeue.common.Constant;
import com.feng.messagequeue.event.BaseEvent;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RabbitmqProducer {
    private final AmqpTemplate amqpTemplate;
    private final Channel channel;

    public RabbitmqProducer(AmqpTemplate amqpTemplate, Channel channel) {
        this.amqpTemplate = amqpTemplate;
        this.channel = channel;
    }

    public void send(BaseEvent event) {
        amqpTemplate.convertAndSend(Constant.EXCHANGE_NAME, Constant.ROUTING_KEY, event);
        log.info("Send msg: {}", event);
    }

    public void send(String message,
                     String exchange,
                     String queueName,
                     String routingKey) throws IOException {
        channel.exchangeDeclare(exchange, BuiltinExchangeType.DIRECT);
        channel.queueDeclare(queueName, true, false, false, null);
        channel.queueBind(queueName, exchange, routingKey, null);
        channel.basicPublish(exchange, routingKey, null, message.getBytes());
    }

    public void send(String message,
                     String exchange,
                     String queueName) throws IOException {
        send(message, exchange, queueName, "");
    }

    public void send(String message) throws IOException {
        send(message, Constant.EXCHANGE_NAME, Constant.QUEUE_NAME, Constant.ROUTING_KEY);
    }
}
