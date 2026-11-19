package no.kristiania.progressservice.integration.messaging;

import com.jayway.jsonpath.JsonPath;
import no.kristiania.progressservice.integration.setup.AbstractProgressApiTest;
import no.kristiania.progressservice.client.GoalSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;

import static no.kristiania.progressservice.config.RabbitConfig.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ProgressEventMessagingTest extends AbstractProgressApiTest {

    private static final String QUEUE = "test.progress.events";
    private static final long USER = 1L;
    private static final long GOAL_ID = 10L;

    @Autowired AmqpAdmin amqpAdmin;

    @BeforeEach
    void bindTestQueue() {
        TopicExchange exchange = new TopicExchange(EXCHANGE);
        Queue queue = new Queue(QUEUE, false);
        amqpAdmin.declareExchange(exchange);
        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("progress.*"));
        amqpAdmin.purgeQueue(QUEUE, false);
    }

    private Message nextMessage(long timeoutMs) {
        return rabbitTemplate.receive(QUEUE, timeoutMs);
    }

    private static String routingKey(Message message) {
        return message.getMessageProperties().getReceivedRoutingKey();
    }

    private static Object field(Message message, String path) {
        return JsonPath.read(new String(message.getBody(), StandardCharsets.UTF_8), path);
    }

    @Test
    void post_publishesCreatedEvent() throws Exception {
        goalHasStatus(GOAL_ID, "ACTIVE");

        postProgress(USER, GOAL_ID, TODAY);

        Message message = nextMessage(5000);
        assertThat(routingKey(message)).isEqualTo(ROUTING_KEY_CREATED);
        assertThat(((Number) field(message, "$.userId")).longValue()).isEqualTo(USER);
        assertThat(((Number) field(message, "$.goalId")).longValue()).isEqualTo(GOAL_ID);
        assertThat(field(message, "$.date")).isEqualTo(TODAY.toString());
        assertThat(field(message, "$.period")).isEqualTo("DAILY");
    }

    @Test
    void delete_publishesDeletedEvent() throws Exception {
        long id = createProgress(USER, GOAL_ID);
        nextMessage(5000);

        deleteProgress(USER, id);

        Message message = nextMessage(5000);
        assertThat(routingKey(message)).isEqualTo(ROUTING_KEY_DELETED);
        assertThat(((Number) field(message, "$.progressId")).longValue()).isEqualTo(id);
        assertThat(((Number) field(message, "$.userId")).longValue()).isEqualTo(USER);
    }

    @Test
    void sameDayTwice_publishesOneEvent() throws Exception {
        goalHasStatus(GOAL_ID, "ACTIVE");

        postProgress(USER, GOAL_ID, TODAY);
        postProgress(USER, GOAL_ID, TODAY);

        assertThat(routingKey(nextMessage(5000))).isEqualTo(ROUTING_KEY_CREATED);
        assertThat(nextMessage(1000)).isNull();
    }

    @Test
    void rejectedPost_publishesNothing() throws Exception {
        goalHasStatus(GOAL_ID, "ARCHIVED");

        postProgress(USER, GOAL_ID, TODAY);

        assertThat(nextMessage(1000)).isNull();
    }

    @Test
    void monthlyPost_sendsPeriodAndTarget() throws Exception {
        when(goalClient.getGoal(GOAL_ID)).thenReturn(new GoalSummary(
                GOAL_ID, "ACTIVE", "CHECK", 2, "MONTHLY", null, null, TODAY, null));

        postProgress(USER, GOAL_ID, TODAY);

        Message message = nextMessage(5000);
        assertThat(field(message, "$.trackingModel")).isEqualTo("MONTHLY_COUNT");
        assertThat(field(message, "$.period")).isEqualTo("MONTHLY");
        assertThat(field(message, "$.targetCount")).isEqualTo(2);
    }
}
