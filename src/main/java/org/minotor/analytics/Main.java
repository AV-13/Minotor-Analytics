package org.minotor.analytics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.minotor.analytics.utils.SceneManager;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // Charger l'icône de l'application pour la barre des tâches
            var iconStream = getClass().getResourceAsStream("/org/minotor/analytics/images/logo1_minotor.png");
            if (iconStream != null) {
                Image appIcon = new Image(iconStream);
                stage.getIcons().add(appIcon);
                System.out.println("✅ Icône d'application chargée");
            } else {
                System.err.println("❌ Icône d'application non trouvée");
            }

            // Configuration de la fenêtre
            stage.setTitle("Minot'Or Analytics Dashboard");
            stage.setMinWidth(1200);
            stage.setMinHeight(800);

            // Charger la scène de login
            SceneManager.setScene(stage, "/org/minotor/analytics/login-view.fxml", true);

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}