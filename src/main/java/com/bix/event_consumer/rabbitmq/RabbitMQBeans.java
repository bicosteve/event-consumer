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
    public Queue resultsQueue(){
        return new Queue(this.rabbitMQConfig.getResults().getQueue());
    }

    @Bean
    public TopicExchange resultsExchange(){
        return new TopicExchange(this.rabbitMQConfig.getResults().getExchange());
    }

    @Bean
    public Binding resultsBinding(Queue resultsQueue, TopicExchange resultsExchange){
        return BindingBuilder
                .bind(resultsQueue)
                .to(resultsExchange)
                .with(this.rabbitMQConfig.getResults().getRoutingKey());
    }

    @Bean
    public Queue transactionsQueue(){
        return new Queue(this.rabbitMQConfig.getTransactions().getQueue());
    }

    @Bean
    public TopicExchange transactionsExchange(){
        return new TopicExchange(this.rabbitMQConfig.getTransactions().getExchange());
    }

    @Bean
    Binding transactionsBinding(Queue transactionsQueue, TopicExchange transactionsExchange){
        return BindingBuilder
                .bind(transactionsQueue)
                .to(transactionsExchange)
                .with(this.rabbitMQConfig.getTransactions().getRoutingKey());
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
