package no.kristiania.progressservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoalClient {
    private final RestClient goalRestClient;

    public GoalClient(RestClient goalRestClient) {
        this.goalRestClient = goalRestClient;
    }

    public GoalSummary getGoal(Long goalId) {
        return goalRestClient.get()
                .uri("/api/goal/{id}", goalId)
                .retrieve()
                .body(GoalSummary.class);
    }
}
