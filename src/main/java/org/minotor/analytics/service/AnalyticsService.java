package org.minotor.analytics.service;

import org.minotor.analytics.model.AnalyticsEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsService {
    private final MongoService mongoService;

    public AnalyticsService() {
        this.mongoService = new MongoService();
    }

    public enum Period {
        TODAY, WEEK, MONTH, ALL
    }

    public List<AnalyticsEvent> getEventsByPeriod(Period period) {
        List<AnalyticsEvent> allEvents = mongoService.getAnalyticsEvents();
        LocalDateTime now = LocalDateTime.now();

        return allEvents.stream()
                .filter(event -> {
                    if (event.getTimestamp() == null) return false;

                    LocalDateTime eventTime = event.getTimestamp()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    return switch (period) {
                        case TODAY -> eventTime.isAfter(now.minusDays(1));
                        case WEEK -> eventTime.isAfter(now.minusWeeks(1));
                        case MONTH -> eventTime.isAfter(now.minusMonths(1));
                        case ALL -> true;
                    };
                })
                .collect(Collectors.toList());
    }

    public Map<String, Long> getPageStatistics() {
        return mongoService.getAnalyticsEvents().stream()
                .filter(event -> event.getUrl() != null)
                .collect(Collectors.groupingBy(
                        AnalyticsEvent::getUrl,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
    public Map<String, Long> getEventTypeStats() {
        return mongoService.getAnalyticsEvents().stream()
                .collect(Collectors.groupingBy(
                        event -> event.getEventType() != null ? event.getEventType() : "Inconnu",
                        Collectors.counting()
                ));
    }
    public Map<String, Long> getDeviceTypeStats() {
        return mongoService.getAnalyticsEvents().stream()
                .filter(event -> event.getDeviceType() != null)
                .collect(Collectors.groupingBy(
                        AnalyticsEvent::getDeviceType,
                        Collectors.counting()
                ));
    }

    public void close() {
        mongoService.close();
    }
}