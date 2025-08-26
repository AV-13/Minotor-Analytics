module org.minotor.analytics {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;
    requires org.mongodb.driver.core;

    exports org.minotor.analytics;
    exports org.minotor.analytics.controller;
    exports org.minotor.analytics.model;
    exports org.minotor.analytics.service;
    exports org.minotor.analytics.utils;

    opens org.minotor.analytics.controller to javafx.fxml;
}