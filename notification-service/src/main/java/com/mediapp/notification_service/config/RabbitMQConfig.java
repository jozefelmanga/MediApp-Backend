package com.mediapp.notification_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ config for notification-service. Declares queues and exchange used
 * by booking-service.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.appointment:appointment.events}")
    private String appointmentExchange;

    @Value("${rabbitmq.queue.appointment-created:appointment-created}")
    private String appointmentCreatedQueue;

    @Value("${rabbitmq.queue.appointment-cancelled:appointment-cancelled}")
    private String appointmentCancelledQueue;

    @Value("${rabbitmq.routing-key.appointment-created:appointment.created}")
    private String appointmentCreatedRoutingKey;

    @Value("${rabbitmq.routing-key.appointment-cancelled:appointment.cancelled}")
    private String appointmentCancelledRoutingKey;

    @Bean
    public TopicExchange appointmentExchange() {
        return new TopicExchange(appointmentExchange);
    }

    @Bean
    public Queue appointmentCreatedQueue() {
        return QueueBuilder.durable(appointmentCreatedQueue).build();
    }

    @Bean
    public Queue appointmentCancelledQueue() {
        return QueueBuilder.durable(appointmentCancelledQueue).build();
    }

    @Bean
    public Binding appointmentCreatedBinding() {
        return BindingBuilder.bind(appointmentCreatedQueue()).to(appointmentExchange())
                .with(appointmentCreatedRoutingKey);
    }

    @Bean
    public Binding appointmentCancelledBinding() {
        return BindingBuilder.bind(appointmentCancelledQueue()).to(appointmentExchange())
                .with(appointmentCancelledRoutingKey);
    }

    @Bean
    public ObjectMapper rabbitObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper rabbitObjectMapper) {
        return new Jackson2JsonMessageConverter(rabbitObjectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
