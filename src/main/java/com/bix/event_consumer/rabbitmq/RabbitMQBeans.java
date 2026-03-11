package com.bix.event_consumer.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQBeans {
    private final RabbitMQConfig rabbitMQConfig;

    @Bean
    public Queue matchesQueue(){
        return new Queue(this.rabbitMQConfig.getExchange());
    }

    @Bean
    public TopicExchange matchesExchange(){
        return new TopicExchange(this.rabbitMQConfig.getExchange());
    }

    @Bean
    public Binding matchesBinding(
            Queue matchesQueue,
            TopicExchange matchesExchange
    ){
        return BindingBuilder
                .bind(matchesQueue)
                .to(matchesExchange)
                .with(this.rabbitMQConfig.getRoutingKey());
    }

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}
