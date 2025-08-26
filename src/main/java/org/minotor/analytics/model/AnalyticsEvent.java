package org.minotor.analytics.model;

import java.util.Date;

public class AnalyticsEvent {
    private String id;
    private String url;
    private Date timestamp;
    private String userAgent;
    private String referrer;
    private Long sessionDuration;
    private String deviceType;
    private String country;
    private Integer screenWidth;
    private Integer screenHeight;
    private String language;
    private Boolean isBounce;
    private String eventType;
    private String pageTitle;
    private Integer loadTime;

    // Constructeur par défaut
    public AnalyticsEvent() {}

    // Constructeur avec paramètres essentiels
    public AnalyticsEvent(String id, String url, Date timestamp) {
        this.id = id;
        this.url = url;
        this.timestamp = timestamp;
    }

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }

    public Long getSessionDuration() { return sessionDuration; }
    public void setSessionDuration(Long sessionDuration) { this.sessionDuration = sessionDuration; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Integer getScreenWidth() { return screenWidth; }
    public void setScreenWidth(Integer screenWidth) { this.screenWidth = screenWidth; }

    public Integer getScreenHeight() { return screenHeight; }
    public void setScreenHeight(Integer screenHeight) { this.screenHeight = screenHeight; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getIsBounce() { return isBounce; }
    public void setIsBounce(Boolean isBounce) { this.isBounce = isBounce; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }

    public Integer getLoadTime() { return loadTime; }
    public void setLoadTime(Integer loadTime) { this.loadTime = loadTime; }

    // Méthodes utilitaires pour l'affichage
    public String getFormattedDate() {
        if (timestamp != null) {
            return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(timestamp);
        }
        return "N/A";
    }

    public String getFormattedDuration() {
        if (sessionDuration != null) {
            long seconds = sessionDuration / 1000;
            return seconds + "s";
        }
        return "N/A";
    }
}