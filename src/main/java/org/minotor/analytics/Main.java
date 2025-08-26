package org.minotor.analytics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.minotor.analytics.utils.SceneManager;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Scène temporaire vide
        primaryStage.setScene(new Scene(new StackPane(), 800, 600));
        primaryStage.setTitle("Minotor Analytics - Connexion");
        primaryStage.show();

        // Charge la vue login via SceneManager (maximisée)
        SceneManager.setScene(primaryStage, "/org/minotor/analytics/login-view.fxml", true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}