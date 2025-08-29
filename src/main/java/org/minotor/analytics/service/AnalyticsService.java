package org.minotor.analytics.service;

import org.minotor.analytics.model.AnalyticsEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A service class that processes and analyzes website analytics data.
 * Think of this as a smart calculator that takes raw website visitor data
 * and turns it into useful statistics and insights.
 *
 * This service works with the MongoService to get raw analytics events from the database,
 * then processes them to create meaningful reports like:
 * - Which pages are most popular
 * - What devices people use to visit the website
 * - How many visitors came during different time periods
 * - What types of interactions happen most often
 *
 * All the heavy lifting of data filtering and calculations is done here,
 * so the user interface can simply ask for specific reports and get ready-to-display results.
 *
 * @author Minot'Or Analytics Team
 * @version 1.0
 * @since 1.0
 */
public class AnalyticsService {

    /**
     * The service that connects to MongoDB database to retrieve analytics events.
     * This is like having a librarian who can fetch specific books (data) from a library (database).
     */
    private final MongoService mongoService;

    /**
     * Creates a new AnalyticsService and sets up the database connection.
     * This constructor automatically creates a MongoService to handle all database operations.
     *
     * When you create an AnalyticsService, it's immediately ready to process analytics data
     * and generate reports.
     */
    public AnalyticsService() {
        this.mongoService = new MongoService();
    }

    /**
     * An enumeration that represents different time periods for filtering analytics data.
     * This is like having preset buttons on a radio - you can quickly select common time ranges
     * without having to specify exact dates.
     *
     * Each period represents a different "looking back" timeframe from the current moment.
     */
    public enum Period {
        /** Events from the last 24 hours */
        TODAY,
        /** Events from the last 7 days */
        WEEK,
        /** Events from the last 30 days */
        MONTH,
        /** All events in the database, regardless of age */
        ALL
    }

    /**
     * Retrieves analytics events filtered by a specific time period.
     * This method is like a time machine filter - it takes all the events in your database
     * and only returns the ones that happened within your chosen time period.
     *
     * For example, if you choose WEEK, you'll get all events from the last 7 days.
     * This is useful for creating time-based reports and understanding trends.
     *
     * @param period The time period to filter by (TODAY, WEEK, MONTH, or ALL)
     * @return A list of AnalyticsEvent objects that occurred within the specified period
     */
    public List<AnalyticsEvent> getEventsByPeriod(Period period) {
        // Get all events from the database first
        List<AnalyticsEvent> allEvents = mongoService.getAnalyticsEvents();

        // Get the current date and time to calculate the cutoff point
        LocalDateTime now = LocalDateTime.now();

        // Filter events based on the selected period
        // This uses Java streams, which is like an assembly line that processes each event
        return allEvents.stream()
                .filter(event -> {
                    // Skip events that don't have a timestamp (we can't determine when they happened)
                    if (event.getTimestamp() == null) return false;

                    // Convert the event's timestamp to a LocalDateTime for easy comparison
                    // This is like converting different date formats to a standard format
                    LocalDateTime eventTime = event.getTimestamp()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();

                    // Use a switch expression to check if the event falls within the period
                    // This is like checking if a date falls within a specific range
                    return switch (period) {
                        case TODAY -> eventTime.isAfter(now.minusDays(1));   // Last 24 hours
                        case WEEK -> eventTime.isAfter(now.minusWeeks(1));   // Last 7 days
                        case MONTH -> eventTime.isAfter(now.minusMonths(1)); // Last 30 days
                        case ALL -> true;                                     // Include everything
                    };
                })
                .collect(Collectors.toList()); // Convert the filtered stream back to a list
    }

    /**
     * Generates statistics showing how many times each page was visited.
     * This method counts visits to each URL and sorts them by popularity.
     *
     * Think of this as creating a "Top 10 Most Popular Pages" list for your website.
     * The results are sorted so the most visited pages appear first.
     *
     * This is useful for understanding which content is most engaging to your visitors
     * and which pages might need more promotion.
     *
     * @return A map where keys are page URLs and values are visit counts, sorted by popularity (highest first)
     */
    public Map<String, Long> getPageStatistics() {
        return mongoService.getAnalyticsEvents().stream()
                // Only include events that have a URL (skip incomplete data)
                .filter(event -> event.getUrl() != null)

                // Group events by URL and count how many times each URL appears
                // This is like sorting mail into different boxes based on the address
                .collect(Collectors.groupingBy(
                        AnalyticsEvent::getUrl,  // Group by the URL field
                        Collectors.counting()    // Count how many events in each group
                ))

                // Convert to a stream of entries so we can sort them
                .entrySet().stream()

                // Sort by count in descending order (most popular first)
                // This is like arranging the mail boxes from fullest to emptiest
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())

                // Collect back into a LinkedHashMap to preserve the sorting order
                // LinkedHashMap remembers the order items were added
                .collect(Collectors.toMap(
                        Map.Entry::getKey,      // URL
                        Map.Entry::getValue,    // Count
                        (e1, e2) -> e1,        // If duplicates, keep the first (shouldn't happen)
                        LinkedHashMap::new     // Use LinkedHashMap to preserve order
                ));
    }

    /**
     * Generates statistics showing the distribution of different event types.
     * This method counts how many times each type of user interaction occurred.
     *
     * Event types might include things like:
     * - page_view (someone loaded a page)
     * - click (someone clicked a button or link)
     * - scroll (someone scrolled down a page)
     * - download (someone downloaded a file)
     *
     * This helps you understand what actions users are taking most often on your website.
     *
     * @return A map where keys are event types and values are the count of each type
     */
    public Map<String, Long> getEventTypeStats() {
        return mongoService.getAnalyticsEvents().stream()
                // Group by event type, but handle null values by labeling them as "Inconnu" (Unknown)
                // This prevents crashes when some events don't have a type specified
                .collect(Collectors.groupingBy(
                        event -> event.getEventType() != null ? event.getEventType() : "Inconnu",
                        Collectors.counting()
                ));
    }

    /**
     * Generates statistics showing what types of devices visitors use.
     * This method counts visits from different device categories.
     *
     * Device types typically include:
     * - Desktop (computers and laptops)
     * - Mobile (phones)
     * - Tablet (iPads and similar devices)
     *
     * This information is valuable for understanding your audience and optimizing
     * your website for the devices they use most often.
     *
     * @return A map where keys are device types and values are the count of visits from each device type
     */
    public Map<String, Long> getDeviceTypeStats() {
        return mongoService.getAnalyticsEvents().stream()
                // Only include events that have device type information
                // This filters out incomplete data that might cause confusion
                .filter(event -> event.getDeviceType() != null)

                // Group by device type and count occurrences
                // This is like sorting visitors into different lines based on what device they're using
                .collect(Collectors.groupingBy(
                        AnalyticsEvent::getDeviceType,
                        Collectors.counting()
                ));
    }

    /**
     * Properly closes the database connection when the service is no longer needed.
     * This method ensures that all database resources are properly released.
     *
     * Think of this as hanging up the phone when you're done talking.
     * It's important to call this method when you're finished using the AnalyticsService
     * to prevent memory leaks and connection problems.
     *
     * This method should typically be called when the application shuts down.
     */
    public void close() {
        mongoService.close();
    }
}