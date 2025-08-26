module org.minotor.analytics {
    requires javafx.controls;
    requires javafx.fxml;

    exports org.minotor.analytics;
    opens org.minotor.analytics.controller to javafx.fxml;
    exports org.minotor.analytics.controller to javafx.fxml;
}