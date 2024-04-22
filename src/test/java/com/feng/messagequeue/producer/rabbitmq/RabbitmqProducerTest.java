package com.feng.messagequeue.producer.rabbitmq;

import com.feng.messagequeue.common.Constant;
import com.feng.messagequeue.event.TestEvent;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RabbitmqProducerTest {

    @Mock
    private AmqpTemplate amqpTemplate;

    @Mock
    private Channel channel;

    private RabbitmqProducer rabbitmqProducer;

    private TestEvent event;

    @BeforeEach
    public void setup() {
        event = new TestEvent()
                .setId("1")
                .setName("test name")
                .setAge(10);
        rabbitmqProducer = new RabbitmqProducer(amqpTemplate, channel);
    }

    @Test
    public void sendEvent_shouldCallAmqpTemplate() {
        rabbitmqProducer.send(event);
        verify(amqpTemplate, times(1)).convertAndSend(Constant.EXCHANGE_NAME, Constant.ROUTING_KEY, event);
    }

    @Test
    public void sendWithParameters_shouldCallChannel() throws IOException {
        String message = "message";
        String exchange = "exchange";
        String queueName = "queueName";
        String routingKey = "routingKey";

        rabbitmqProducer.send(message, exchange, queueName, routingKey);

        verify(channel, times(1)).exchangeDeclare(exchange, BuiltinExchangeType.DIRECT);
        verify(channel, times(1)).queueDeclare(queueName, true, false, false, null);
        verify(channel, times(1)).queueBind(queueName, exchange, routingKey, null);
        verify(channel, times(1)).basicPublish(exchange, routingKey, null, message.getBytes());
    }

    @Test
    public void sendWithDefaultRoutingKey_shouldCallChannel() throws IOException {
        String message = "message";
        String exchange = "exchange";
        String queueName = "queueName";

        rabbitmqProducer.send(message, exchange, queueName);

        verify(channel, times(1)).exchangeDeclare(exchange, BuiltinExchangeType.DIRECT);
        verify(channel, times(1)).queueDeclare(queueName, true, false, false, null);
        verify(channel, times(1)).queueBind(queueName, exchange, "", null);
        verify(channel, times(1)).basicPublish(exchange, "", null, message.getBytes());
    }

    @Test
    public void sendWithDefaultExchangeAndQueue_shouldCallChannel() throws IOException {
        String message = "message";

        rabbitmqProducer.send(message);

        verify(channel, times(1)).exchangeDeclare(Constant.EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        verify(channel, times(1)).queueDeclare(Constant.QUEUE_NAME, true, false, false, null);
        verify(channel, times(1)).queueBind(Constant.QUEUE_NAME, Constant.EXCHANGE_NAME, Constant.ROUTING_KEY, null);
        verify(channel, times(1)).basicPublish(Constant.EXCHANGE_NAME, Constant.ROUTING_KEY, null, message.getBytes());
    }
}
