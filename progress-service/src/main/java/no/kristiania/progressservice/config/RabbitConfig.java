package no.kristiania.progressservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "lifeos.progress";
    public static final String ROUTING_KEY_CREATED = "progress.created";
    public static final String ROUTING_KEY_DELETED = "progress.deleted";

    @Bean
    public TopicExchange progressExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
