package org.minotor.analytics.utils;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.minotor.analytics.service.AuthService;

import java.io.IOException;

/**
 * A helper class that makes it easy to switch between different screens in a JavaFX application.
 * Think of it like a TV remote control - it helps you change channels (screens) in your app.
 * This class handles all the complicated stuff like loading new screens, keeping the window size,
 * and making sure everything looks good when switching between different parts of your application.
 *
 * @author Minot'Or Analytics Team
 * @version 1.0
 * @since 1.0
 */
public class SceneManager {

    private static AuthService authService;


    public static void setAuthService(AuthService service) {
        authService = service;
    }

    public static AuthService getAuthService() {
        return authService;
    }
    /**
     * Changes the current screen to a new one by loading an FXML file.
     * This is the main method that does all the heavy work of switching screens.
     * Think of FXML files as blueprints for your app's screens - this method
     * reads those blueprints and builds the actual screen you see.
     *
     * @param stage The main window of your application (like the frame of a picture)
     * @param fxmlPath The location of the FXML file that describes your new screen
     * @param maximize Whether to make the window fill the entire screen (true) or keep current size (false)
     * @param fullScreen Whether to hide the window borders and make it completely full screen (true/false)
     *
     * @throws RuntimeException if the FXML file cannot be found or loaded
     *
     * @example
     * // Switch to login screen and maximize the window
     * SceneManager.setScene(primaryStage, "/login.fxml", true, false);
     */
    public static void setScene(Stage stage, String fxmlPath, boolean maximize, boolean fullScreen) {
        try {
            // Remember how the window looked before we change it
            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            // Load the FXML file (like reading a blueprint to build something)
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Create the new screen with the same size as the old one
            Scene newScene = new Scene(root, currentWidth, currentHeight);

            // Use Platform.runLater to make sure the screen changes happen smoothly
            // (This is like saying "do this when you have time" to JavaFX)
            Platform.runLater(() -> {
                // Temporarily make window normal size to avoid display problems
                if (wasMaximized) {
                    stage.setMaximized(false);
                }

                // Actually switch to the new screen
                stage.setScene(newScene);

                // Set minimum window size so it's never too small to use
                stage.setMinWidth(800);
                stage.setMinHeight(600);

                // Make window maximized if requested or if it was maximized before
                if (maximize || wasMaximized) {
                    stage.setMaximized(true);
                }

                // Make completely full screen if requested (hides window borders)
                if (fullScreen) {
                    stage.setFullScreen(true);
                }
            });
            if (fxmlPath.contains("dashboard")) {
                if (authService == null) {
                    System.err.println("⚠️ AuthService non défini dans SceneManager");
                } else {
                    System.out.println("✅ AuthService disponible pour le dashboard");
                }
            }
        } catch (IOException e) {
            // If something goes wrong loading the FXML file, print the error
            e.printStackTrace();
            throw new RuntimeException("Could not load FXML file: " + fxmlPath, e);
        }
    }

    /**
     * A simpler version of setScene that doesn't use full screen mode.
     * This is a shortcut method for when you don't need full screen.
     *
     * @param stage The main window of your application
     * @param fxmlPath The location of the FXML file for your new screen
     * @param maximize Whether to maximize the window (true) or keep current size (false)
     */
    public static void setScene(Stage stage, String fxmlPath, boolean maximize) {
        setScene(stage, fxmlPath, maximize, false);
    }

    /**
     * Changes the screen using information from a JavaFX event (like a button click).
     * This method figures out which window the event came from and changes its screen.
     *
     * @param event The event that triggered this screen change (usually a button click)
     * @param fxmlPath The location of the FXML file for your new screen
     * @param maximize Whether to maximize the window
     * @param fullScreen Whether to use full screen mode
     */
    public static void setScene(Event event, String fxmlPath, boolean maximize, boolean fullScreen) {
        Stage stage = getStageFromEvent(event);
        if (stage != null) {
            setScene(stage, fxmlPath, maximize, fullScreen);
        }
    }

    /**
     * A simpler version that changes screen from an event without full screen.
     *
     * @param event The event that triggered this screen change
     * @param fxmlPath The location of the FXML file for your new screen
     * @param maximize Whether to maximize the window
     */
    public static void setScene(Event event, String fxmlPath, boolean maximize) {
        setScene(event, fxmlPath, maximize, false);
    }

    /**
     * Changes the screen using any JavaFX component (like a button or label).
     * This method finds the window that contains the component and changes its screen.
     *
     * @param node Any JavaFX component that's currently displayed in your app
     * @param fxmlPath The location of the FXML file for your new screen
     * @param maximize Whether to maximize the window
     * @param fullScreen Whether to use full screen mode
     */
    public static void setScene(Node node, String fxmlPath, boolean maximize, boolean fullScreen) {
        Stage stage = getStageFromNode(node);
        if (stage != null) {
            setScene(stage, fxmlPath, maximize, fullScreen);
        }
    }

    /**
     * A simpler version that changes screen from a component without full screen.
     *
     * @param node Any JavaFX component that's currently displayed in your app
     * @param fxmlPath The location of the FXML file for your new screen
     * @param maximize Whether to maximize the window
     */
    public static void setScene(Node node, String fxmlPath, boolean maximize) {
        setScene(node, fxmlPath, maximize, false);
    }

    /**
     * Helper method that finds the main window from a JavaFX event.
     * This is like detective work - it traces back from the event to find the window.
     *
     * @param event The event to investigate
     * @return The window where the event happened, or null if not found
     */
    private static Stage getStageFromEvent(Event event) {
        Object source = event.getSource();
        if (source instanceof Node node) {
            return getStageFromNode(node);
        }
        return null;
    }

    /**
     * Helper method that finds the main window from any JavaFX component.
     * Every component in JavaFX belongs to a scene, and every scene belongs to a window.
     * This method follows that chain to find the window.
     *
     * @param node The JavaFX component to trace back from
     * @return The window that contains this component
     */
    private static Stage getStageFromNode(Node node) {
        return (Stage) node.getScene().getWindow();
    }
    /**
     * Nettoie l'AuthService lors de la déconnexion
     */
    public static void clearAuthService() {
        if (authService != null) {
            authService.logout();
        }
        authService = null;
        System.out.println("✅ AuthService nettoyé dans SceneManager");
    }
}