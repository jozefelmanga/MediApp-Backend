package com.mediapp.booking_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the booking service.
 * Configures exchanges, queues, and bindings for appointment events.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.appointment}")
    private String appointmentExchange;

    @Value("${rabbitmq.queue.appointment-created}")
    private String appointmentCreatedQueue;

    @Value("${rabbitmq.queue.appointment-cancelled}")
    private String appointmentCancelledQueue;

    @Value("${rabbitmq.routing-key.appointment-created}")
    private String appointmentCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.appointment-cancelled}")
    private String appointmentCancelledRoutingKey;

    /**
     * Configure the main exchange for appointment events.
     */
    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(appointmentExchange);
    }

    /**
     * Queue for appointment created events.
     */
    @Bean
    public Queue appointmentCreatedQueue() {
        return QueueBuilder.durable(appointmentCreatedQueue)
                .withArgument("x-dead-letter-exchange", appointmentExchange + ".dlx")
                .build();
    }

    /**
     * Queue for appointment cancelled events.
     */
    @Bean
    public Queue appointmentCancelledQueue() {
        return QueueBuilder.durable(appointmentCancelledQueue)
                .withArgument("x-dead-letter-exchange", appointmentExchange + ".dlx")
                .build();
    }

    /**
     * Bind appointment created queue to exchange.
     */
    @Bean
    public Binding appointmentCreatedBinding() {
        return BindingBuilder
                .bind(appointmentCreatedQueue())
                .to(appointmentExchange())
                .with(appointmentCreatedRoutingKey);
    }

    /**
     * Bind appointment cancelled queue to exchange.
     */
    @Bean
    public Binding appointmentCancelledBinding() {
        return BindingBuilder
                .bind(appointmentCancelledQueue())
                .to(appointmentExchange())
                .with(appointmentCancelledRoutingKey);
    }

    /**
     * Configure ObjectMapper for JSON serialization with Java 8 date/time support.
     */
    @Bean
    public ObjectMapper rabbitObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * Configure Jackson message converter for JSON serialization.
     */
    @Bean
    @SuppressWarnings("removal")
    public MessageConverter jsonMessageConverter(ObjectMapper rabbitObjectMapper) {
        return new Jackson2JsonMessageConverter(rabbitObjectMapper);
    }

    /**
     * Configure RabbitTemplate with JSON converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
