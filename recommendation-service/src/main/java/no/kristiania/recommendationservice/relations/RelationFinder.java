package no.kristiania.recommendationservice.relations;

import java.util.*;
import java.util.stream.Stream;

public final class RelationFinder {
    private RelationFinder() {}

    public static List<RelationHint> find(List<GoalRef> goals, GoalRef candidate, RelationProperties p) {
        List<GoalRef> all = new ArrayList<>(goals);
        if (candidate != null) all.add(candidate);

        List<RelationHint> found = new ArrayList<>();
        for (RelationRule rule : p.rules()) {
            List<GoalRef> as = matching(all, rule.a());
            List<GoalRef> bs = matching(all, rule.b());
            switch (rule.type()) {
                case SUPPORTS -> supports(rule, as, bs, found);
                case TENSION, REDUNDANT -> pairs(rule, as, bs, found);
                case OVERLOAD -> { }
            }
        }
        overload(all, p.overload()).ifPresent(found::add);

        String mine = candidate == null ? null : label(candidate);
        Map<String, RelationHint> unique = new LinkedHashMap<>();
        for (RelationHint h : found) {
            if (mine != null && h.type() != RelationType.OVERLOAD && !h.involved().contains(mine)) continue;
            unique.merge(h.type() + "|" + h.suggestion() + "|" + h.reason(), h, RelationHint::merge);
        }
        return List.copyOf(unique.values());
    }

    private static void supports(RelationRule rule, List<GoalRef> as, List<GoalRef> bs, List<RelationHint> out) {
        if (as.isEmpty()) return;
        List<String> from = as.stream().map(RelationFinder::label).toList();
        if (!bs.isEmpty()) {
            List<String> both = Stream.concat(from.stream(), bs.stream().map(RelationFinder::label)).distinct().toList();
            out.add(new RelationHint(RelationType.SUPPORTS, both, null, rule.reason()));
            return;
        }
        for (String code : rule.b()) {
            out.add(new RelationHint(RelationType.SUPPORTS, from, code, rule.reason()));
        }
    }

    private static void pairs(RelationRule rule, List<GoalRef> as, List<GoalRef> bs, List<RelationHint> out) {
        for (GoalRef x : as) {
            for (GoalRef y : bs) {
                if (x != y) out.add(new RelationHint(rule.type(), List.of(label(x), label(y)), null, rule.reason()));
            }
        }
    }

    private static Optional<RelationHint> overload(List<GoalRef> all, RelationProperties.Overload o) {
        long daily = all.stream().filter(g -> "DAILY".equals(g.period())).count();
        if (daily <= o.maxDailyGoals()) return Optional.empty();
        return Optional.of(new RelationHint(RelationType.OVERLOAD, List.of(), null,
                "You have " + daily + " daily goals. Fewer goals are easier to keep up, so consider pausing one."));
    }

    private static List<GoalRef> matching(List<GoalRef> goals, List<String> codes) {
        return goals.stream().filter(g -> codes.stream().anyMatch(g::matches)).toList();
    }

    private static String label(GoalRef g) {
        return g.templateCode() != null ? g.templateCode() : g.directionCode();
    }
}
