package no.kristiania.recommendationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "lifeos.goal";
    public static final String QUEUE_OUTDOOR_CHANGED = "recommendation.goal.outdoor.changed";
    public static final String ROUTING_KEY_OUTDOOR_CHANGED = "goal.outdoor.changed";

    @Bean
    public TopicExchange goalExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue outdoorChangedQueue() {
        return QueueBuilder.durable(QUEUE_OUTDOOR_CHANGED).build();
    }

    @Bean
    public Binding outdoorChangedBinding() {
        return BindingBuilder.bind(outdoorChangedQueue()).to(goalExchange()).with(ROUTING_KEY_OUTDOOR_CHANGED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
