package com.bix.event_consumer.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
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
        return new Queue(this.rabbitMQConfig.getMatches().getQueue());
    }

    @Bean
    public TopicExchange matchesExchange(){
        return new TopicExchange(this.rabbitMQConfig.getMatches().getExchange());
    }

    @Bean
    public Binding matchesBinding(
            Queue matchesQueue,
            TopicExchange matchesExchange
    ){
        return BindingBuilder
                .bind(matchesQueue)
                .to(matchesExchange)
                .with(this.rabbitMQConfig.getMatches().getRoutingKey());
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper){
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
