package com.bix.event_consumer.messaging;

import com.bix.event_consumer.kafka.KafkaJsonPublisher;
import com.bix.event_consumer.kafka.KafkaMessagingConfig;
import com.bix.event_consumer.kafka.KafkaMessagingConfiguration;
import com.bix.event_consumer.rabbitmq.RabbitMQBeans;
import com.bix.event_consumer.rabbitmq.RabbitMQConfig;
import com.bix.event_consumer.rabbitmq.RabbitResultTriggerPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class BrokerAutoConfigurationTest {

private final ApplicationContextRunner brokerValidationContext = new ApplicationContextRunner()
.withUserConfiguration(BrokerPropertiesConfiguration.class);



@Test
void missingBrokerFailsStartup() {
brokerValidationContext.run(context -> assertThat(context).hasFailed());
}

@Test
void invalidBrokerFailsStartup() {
brokerValidationContext.withPropertyValues("app.messaging.broker=pulsar")
.run(context -> assertThat(context).hasFailed());
}

@Test
void kafkaModeUsesSpringFactoriesFilterToExcludeRabbitInfrastructure() {
try (ConfigurableApplicationContext context = start("kafka")) {
assertThat(context.getBeansOfType(KafkaTemplate.class)).hasSize(2);
assertThat(context.getBeansOfType(ProducerFactory.class)).hasSize(1);
assertThat(context.getBeansOfType(ConsumerFactory.class)).hasSize(1);
assertThat(context.getBeansOfType(KafkaJsonPublisher.class)).hasSize(1);
assertThat(context.getBeansOfType(ConnectionFactory.class)).isEmpty();
assertThat(context.getBeansOfType(RabbitTemplate.class)).isEmpty();
assertThat(context.getBeansOfType(RabbitMQBeans.class)).isEmpty();
}
}

@Test
void rabbitModeUsesSpringFactoriesFilterToExcludeKafkaInfrastructure() {
try (ConfigurableApplicationContext context = start("rabbitmq")) {
assertThat(context.getBeansOfType(ConnectionFactory.class)).hasSize(1);
assertThat(context.getBeansOfType(RabbitTemplate.class)).hasSize(1);
assertThat(context.getBeansOfType(RabbitResultTriggerPublisher.class)).hasSize(1);
assertThat(context.getBeansOfType(KafkaTemplate.class)).isEmpty();
assertThat(context.getBeansOfType(ProducerFactory.class)).isEmpty();
assertThat(context.getBeansOfType(KafkaMessagingConfiguration.class)).isEmpty();
Queue matches = context.getBean("matchesQueue", Queue.class);
assertThat(matches.getArguments()).containsEntry("x-dead-letter-exchange", "matches.dlx")
.containsEntry("x-dead-letter-routing-key", "matches.dlq");
}
}

private ConfigurableApplicationContext start(String broker) {
SpringApplication application = new SpringApplication(BrokerModeApplication.class);
application.setWebApplicationType(WebApplicationType.NONE);
application.setLogStartupInfo(false);
return application.run(
"--spring.config.location=optional:classpath:/broker-auto-configuration-test/",
"--app.messaging.broker=" + broker,
"--spring.kafka.bootstrap-servers=localhost:9092",
"--spring.kafka.listener.auto-startup=false",
"--spring.rabbitmq.host=localhost",
"--spring.rabbitmq.port=1",
"--spring.rabbitmq.connection-timeout=1ms",
"--app.messaging.kafka.matches-topic=matches.queue",
"--app.messaging.kafka.results-topic=results.queue",
"--app.messaging.kafka.transactions-topic=transactions.queue",
"--app.messaging.kafka.matches-group=matches-group",
"--app.messaging.kafka.results-group=results-group",
"--app.messaging.kafka.transactions-group=transactions-group",
"--app.messaging.kafka.create-topics=false",
"--app.messaging.rabbitmq.matches.exchange=matches.exchange",
"--app.messaging.rabbitmq.matches.queue=matches.queue",
"--app.messaging.rabbitmq.matches.routing-key=matches.key",
"--app.messaging.rabbitmq.matches.dead-letter-exchange=matches.dlx",
"--app.messaging.rabbitmq.matches.dead-letter-routing-key=matches.dlq",
"--app.messaging.rabbitmq.matches.dead-letter-queue=matches.dlq",
"--app.messaging.rabbitmq.results.exchange=results.exchange",
"--app.messaging.rabbitmq.results.queue=results.queue",
"--app.messaging.rabbitmq.results.routing-key=results.key",
"--app.messaging.rabbitmq.results.dead-letter-exchange=results.dlx",
"--app.messaging.rabbitmq.results.dead-letter-routing-key=results.dlq",
"--app.messaging.rabbitmq.results.dead-letter-queue=results.dlq",
"--app.messaging.rabbitmq.transactions.exchange=transactions.exchange",
"--app.messaging.rabbitmq.transactions.queue=transactions.queue",
"--app.messaging.rabbitmq.transactions.routing-key=transactions.key",
"--app.messaging.rabbitmq.transactions.dead-letter-exchange=transactions.dlx",
"--app.messaging.rabbitmq.transactions.dead-letter-routing-key=transactions.dlq",
"--app.messaging.rabbitmq.transactions.dead-letter-queue=transactions.dlq");
}

@Configuration(proxyBeanMethods = false)
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(MessagingBrokerProperties.class)
@Import({KafkaMessagingConfiguration.class, KafkaMessagingConfig.class, RabbitMQBeans.class, RabbitMQConfig.class, RabbitResultTriggerPublisher.class})
static class BrokerModeApplication {
}

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MessagingBrokerProperties.class)
static class BrokerPropertiesConfiguration {
}
}
