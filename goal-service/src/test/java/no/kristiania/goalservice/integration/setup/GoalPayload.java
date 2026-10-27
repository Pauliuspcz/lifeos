package no.kristiania.goalservice.integration.setup;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class GoalPayload {

    private final Map<String, Object> fields = new LinkedHashMap<>();

    public static GoalPayload valid() {
        return new GoalPayload()
                .with("title", "Run").with("description", "Morning runs")
                .with("period", "WEEKLY").with("timesPerPeriod", 3)
                .with("amountPerSession", 5.0).with("unit", "km");
    }

    public GoalPayload with(String field, Object value) { fields.put(field, value); return this; }
    public GoalPayload without(String field) { fields.remove(field); return this; }

    public String json() {
        return fields.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\":" + render(e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    private static String render(Object v) { return v instanceof String s ? "\"" + s + "\"" : String.valueOf(v); }
}
