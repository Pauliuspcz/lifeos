package no.kristiania.reportservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "lifeos.progress";
    public static final String QUEUE = "report.progress";
    public static final String ROUTING_PATTERN = "progress.#";

    @Bean
    public TopicExchange progressExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue reportQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding reportBinding(Queue reportQueue, TopicExchange progressExchange) {
        return BindingBuilder.bind(reportQueue).to(progressExchange).with(ROUTING_PATTERN);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
