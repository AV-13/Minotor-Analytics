package org.minotor.analytics.utils;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    // Changement de scène via Stage (ex: Main)
    public static void setScene(Stage stage, String fxmlPath, boolean maximize) {
        try {
            // Récupérer l'état maximisé actuel
            boolean wasMaximized = stage.isMaximized();

            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Créer la nouvelle scène
            Scene scene = new Scene(root);

            // Appliquer les changements en une seule fois pour éviter les flashs
            Platform.runLater(() -> {
                // Enregistrer la taille actuelle avant de changer la scène
                double width = stage.getWidth();
                double height = stage.getHeight();

                // Appliquer la nouvelle scène
                stage.setScene(scene);

                // Restaurer la taille
                stage.setWidth(width);
                stage.setHeight(height);

                // Définir des dimensions minimales
                stage.setMinWidth(800);
                stage.setMinHeight(600);

                // Important: désactiver puis réactiver la maximisation
                if (maximize || wasMaximized) {
                    stage.setMaximized(false); // Désactiver d'abord
                    stage.setMaximized(true);  // Puis réactiver
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Autres méthodes inchangées...

    // Changement de scène via Event (ex: depuis un contrôleur)
    public static void setScene(Event event, String fxmlPath, boolean maximize) {
        Stage stage = getStageFromEvent(event);
        if (stage != null) {
            setScene(stage, fxmlPath, maximize);
        }
    }

    // Changement de scène via un Node (ex: depuis un contrôleur)
    public static void setScene(Node node, String fxmlPath, boolean maximize) {
        Stage stage = getStageFromNode(node);
        if (stage != null) {
            setScene(stage, fxmlPath, maximize);
        }
    }

    // Récupère le Stage à partir d'un Event
    private static Stage getStageFromEvent(Event event) {
        Object source = event.getSource();
        if (source instanceof Node node) {
            return getStageFromNode(node);
        }
        return null;
    }

    // Récupère le Stage à partir d'un Node
    private static Stage getStageFromNode(Node node) {
        return (Stage) node.getScene().getWindow();
    }
} v