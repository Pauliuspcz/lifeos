package no.kristiania.reportservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "lifeos.progress";
    public static final String QUEUE_CREATED = "report.progress.created";
    public static final String QUEUE_DELETED = "report.progress.deleted";
    public static final String ROUTING_KEY_CREATED = "progress.created";
    public static final String ROUTING_KEY_DELETED = "progress.deleted";

    @Bean
    public TopicExchange progressExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue createdQueue() {
        return QueueBuilder.durable(QUEUE_CREATED).build();
    }

    @Bean
    public Queue deletedQueue() {
        return QueueBuilder.durable(QUEUE_DELETED).build();
    }

    @Bean
    public Binding createdBinding(){
        return BindingBuilder.bind(createdQueue()).to(progressExchange()).with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding deletedBinding(){
        return BindingBuilder.bind(deletedQueue()).to(progressExchange()).with(ROUTING_KEY_DELETED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
