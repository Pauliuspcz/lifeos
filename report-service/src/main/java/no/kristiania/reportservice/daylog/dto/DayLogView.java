package no.kristiania.reportservice.daylog.dto;

import no.kristiania.reportservice.daylog.entity.DayLog;

import java.time.LocalDate;

public record DayLogView(LocalDate date, Integer mood, Integer sleepHours) {
    public static DayLogView from(DayLog log) {
        return new DayLogView(log.getDate(), log.getMood(), log.getSleepHours());
    }
}
