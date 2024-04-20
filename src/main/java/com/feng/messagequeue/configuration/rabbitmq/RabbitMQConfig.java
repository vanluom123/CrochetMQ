package com.feng.messagequeue.configuration.rabbitmq;

import com.feng.messagequeue.common.Constant;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.username}")
    private String userName;

    @Value("${rabbitmq.password}")
    private String password;

    @Value("${rabbitmq.uri}")
    private String uri;

    @Value("${rabbitmq.host}")
    private String host;

    @Value("${rabbitmq.port}")
    private int port;

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(Constant.DEAD_LETTER_EXCHANGE_NAME);
    }

    @Bean
    Queue dlq() {
        return QueueBuilder.durable(Constant.DEAD_LETTER_QUEUE_NAME)
                .build();
    }

    @Bean
    Queue queue() {
        return QueueBuilder.durable(Constant.QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", Constant.DEAD_LETTER_EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", Constant.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    DirectExchange exchange() {
        return new DirectExchange(Constant.EXCHANGE_NAME);
    }

    @Bean
    Binding DLQbinding(Queue dlq, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(dlq).to(deadLetterExchange).with(Constant.DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(Constant.ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory cf = new CachingConnectionFactory();
        cf.setUsername(userName);
        cf.setPassword(password);
        cf.setUri(uri);
        cf.setHost(host);
        cf.setPort(port);
        cf.setChannelCheckoutTimeout(5000);
        cf.setConnectionTimeout(10000);
        return cf;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(connectionFactory);
        return containerFactory;
    }

    @Bean("amqpTemplate")
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RetryTemplate retryTemplate = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(5000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setRetryTemplate(retryTemplate);
        return rabbitTemplate;
    }

    @Bean
    public Channel channel(ConnectionFactory cf) throws IOException, TimeoutException {
        CachingConnectionFactory cacheCF = (CachingConnectionFactory) cf;
        Connection conn = cacheCF.getRabbitConnectionFactory().newConnection();
        return conn.createChannel();
    }
}
