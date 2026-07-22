package com.bix.event_consumer.messaging;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class MessagingBrokerAutoConfigurationImportFilter implements AutoConfigurationImportFilter, EnvironmentAware {
    private static final String KAFKA_AUTO_CONFIGURATION = "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration";
    private static final String RABBIT_AUTO_CONFIGURATION = "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration";

    private String broker;

    @Override
    public void setEnvironment(Environment environment) {
        broker = environment.getProperty("app.messaging.broker");
    }

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int index = 0; index < autoConfigurationClasses.length; index++) {
            String candidate = autoConfigurationClasses[index];
            matches[index] = !(KAFKA_AUTO_CONFIGURATION.equals(candidate) && !"kafka".equals(broker))
                    && !(RABBIT_AUTO_CONFIGURATION.equals(candidate) && !"rabbitmq".equals(broker));
        }
        return matches;
    }
}
