package org.minotor.analytics.model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents an analytics event that tracks user interactions on a website.
 * This class stores information about page visits, user behavior, and device details.
 */
public class AnalyticsEvent {

    // Basic event information
    private String id;
    private String url;
    private Date timestamp;

    // Browser and device information
    private String userAgent;
    private String referrer;
    private String deviceType;
    private Integer screenWidth;
    private Integer screenHeight;
    private String language;

    // Event details
    private String eventType;
    private String pageTitle;
    private Integer loadTime;

    // Date formatter for consistent date display
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    /**
     * Default constructor - creates an empty analytics event
     */
    public AnalyticsEvent() {}

    /**
     * Creates an analytics event with basic required information
     * @param id Unique identifier for this event
     * @param url The webpage URL where the event occurred
     * @param timestamp When the event happened
     */
    public AnalyticsEvent(String id, String url, Date timestamp) {
        this.id = id;
        this.url = url;
        this.timestamp = timestamp;
    }

    // Basic getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    // Browser information getters and setters
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }

    // Device information getters and setters
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public Integer getScreenWidth() { return screenWidth; }
    public void setScreenWidth(Integer screenWidth) { this.screenWidth = screenWidth; }

    public Integer getScreenHeight() { return screenHeight; }
    public void setScreenHeight(Integer screenHeight) { this.screenHeight = screenHeight; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    // Event details getters and setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

    public Integer getLoadTime() { return loadTime; }
    public void setLoadTime(Integer loadTime) { this.loadTime = loadTime; }

    /**
     * Returns a nicely formatted date string for display purposes
     * @return Formatted date as "dd/MM/yyyy HH:mm" or "N/A" if no timestamp
     */
    public String getFormattedDate() {
        if (timestamp != null) {
            return DATE_FORMATTER.format(timestamp);
        }
        return "N/A";
    }
}