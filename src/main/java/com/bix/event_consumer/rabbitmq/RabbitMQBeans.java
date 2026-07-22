package com.bix.event_consumer.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.messaging.broker", havingValue = "rabbitmq")
public class RabbitMQBeans {
     private final RabbitMQConfig config;


     @Bean
     public Queue matchesQueue() {
        return retryableQueue(config.getMatches());
     }


     @Bean
     public TopicExchange matchesExchange() {
        return new TopicExchange(config.getMatches().getExchange());
     }


     @Bean
     public Binding matchesBinding(
             Queue matchesQueue,
             TopicExchange matchesExchange
     ) {
         return BindingBuilder
                 .bind(matchesQueue)
                 .to(matchesExchange)
                 .with(config.getMatches().getRoutingKey());
     }


     @Bean
     public DirectExchange matchesDeadLetterExchange() {
         return new DirectExchange(config.getMatches().getDeadLetterExchange());
     }

     @Bean
     public Queue matchesDeadLetterQueue() {
         return new Queue(config.getMatches().getDeadLetterQueue());
     }

     @Bean
     public Binding matchesDeadLetterBinding(
             Queue matchesDeadLetterQueue,
             DirectExchange matchesDeadLetterExchange
     ) {
         return BindingBuilder
                 .bind(matchesDeadLetterQueue)
                 .to(matchesDeadLetterExchange)
                 .with(config.getMatches().getDeadLetterRoutingKey());
     }

     @Bean
     public Queue resultsQueue() {
         return retryableQueue(config.getResults());
     }

     @Bean
     public TopicExchange resultsExchange() {
          return new TopicExchange(config.getResults().getExchange());
     }


     @Bean
     public Binding resultsBinding(
             Queue resultsQueue,
             TopicExchange resultsExchange
     ) {
          return BindingBuilder
                  .bind(resultsQueue)
                  .to(resultsExchange)
                  .with(config.getResults().getRoutingKey());
     }


     @Bean
     public DirectExchange resultsDeadLetterExchange() {
          return new DirectExchange(config.getResults().getDeadLetterExchange());
     }


     @Bean
     public Queue resultsDeadLetterQueue() {
         return new Queue(config.getResults().getDeadLetterQueue());
     }


     @Bean
     public Binding resultsDeadLetterBinding(
             Queue resultsDeadLetterQueue,
             DirectExchange resultsDeadLetterExchange
     ) {
           return BindingBuilder
                   .bind(resultsDeadLetterQueue)
                   .to(resultsDeadLetterExchange)
                   .with(config.getResults().getDeadLetterRoutingKey());
     }

     @Bean
     public Queue transactionsQueue() {
          return retryableQueue(config.getTransactions());
     }


     @Bean
     public TopicExchange transactionsExchange() {
         return new TopicExchange(config.getTransactions().getExchange());
     }


     @Bean
     public Binding transactionsBinding(
             Queue transactionsQueue,
             TopicExchange transactionsExchange
     ) {
        return BindingBuilder
                .bind(transactionsQueue)
                .to(transactionsExchange)
                .with(config.getTransactions().getRoutingKey());
     }

     @Bean
     public DirectExchange transactionsDeadLetterExchange() {
         return new DirectExchange(config.getTransactions().getDeadLetterExchange());
     }


     @Bean
     public Queue transactionsDeadLetterQueue() {
         return new Queue(config.getTransactions().getDeadLetterQueue());
     }


     @Bean
     public Binding transactionsDeadLetterBinding(
             Queue transactionsDeadLetterQueue,
             DirectExchange transactionsDeadLetterExchange
     ) {
         return BindingBuilder
                 .bind(transactionsDeadLetterQueue)
                 .to(transactionsDeadLetterExchange)
                 .with(config.getTransactions().getDeadLetterRoutingKey());
     }

     private Queue retryableQueue(RabbitMQConfig.QueueConfig channel) {
         return QueueBuilder.durable(channel.getQueue())
         .deadLetterExchange(channel.getDeadLetterExchange())
         .deadLetterRoutingKey(channel.getDeadLetterRoutingKey())
         .build();
     }

     @Bean
     public MessageConverter messageConverter(ObjectMapper objectMapper) {
          return new Jackson2JsonMessageConverter(objectMapper);
     }


     @Bean
     public ConfirmingRabbitPublisher confirmingRabbitPublisher(RabbitTemplate rabbitTemplate) {
          return new ConfirmingRabbitPublisher(rabbitTemplate, Duration.ofMillis(config.getPublisherTimeoutMillis()));
     }

     @Bean
     public RetryOperationsInterceptor rabbitRetryInterceptor() {
            return RetryInterceptorBuilder
                    .stateless()
                    .maxAttempts(3)
                    .backOffOptions(1_000, 2.0, 10_000)
                    .recoverer(new RejectAndDontRequeueRecoverer()).build();
     }


     @Bean
     public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
             ConnectionFactory connectionFactory,
             MessageConverter messageConverter,
             RetryOperationsInterceptor rabbitRetryInterceptor
     ) {
          SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

          factory.setConnectionFactory(connectionFactory);
          factory.setMessageConverter(messageConverter);
          factory.setAdviceChain(rabbitRetryInterceptor);
          factory.setDefaultRequeueRejected(false);

          return factory;
     }
}
