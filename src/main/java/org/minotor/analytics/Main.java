package org.minotor.analytics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.minotor.analytics.utils.SceneManager;

/**
 * The main entry point for the Minot'Or Analytics Dashboard application.
 * This is like the front door of your house - it's where everything starts when you run the app.
 *
 * This class extends JavaFX Application, which means it can create windows and display graphics.
 * Think of it as the foundation that holds your entire application together.
 *
 * When you double-click the app icon or run it from your IDE, this class is the first thing
 * that gets called to set up and show your application window.
 *
 * @author Minot'Or Analytics Team
 * @version 1.0
 * @since 1.0
 */
public class Main extends Application {

    /**
     * This method is automatically called by JavaFX when the application starts.
     * It's like the "setup" phase where we prepare everything before showing the app to the user.
     *
     * Here we do important tasks like:
     * - Loading the app icon (the little picture in the taskbar)
     * - Setting up the main window (title, size, etc.)
     * - Loading the first screen the user will see (login screen)
     *
     * @param stage The main window of the application (like a picture frame that holds everything)
     *
     * @throws Exception if something goes wrong during application startup
     */
    @Override
    public void start(Stage stage) {
        try {
            // Load the application icon that appears in the taskbar and window title bar
            // Think of this like putting a logo on your business storefront
            var iconStream = getClass().getResourceAsStream("/org/minotor/analytics/images/logo1_minotor.png");
            if (iconStream != null) {
                Image appIcon = new Image(iconStream);
                stage.getIcons().add(appIcon);
                System.out.println("Application icon loaded successfully");
            } else {
                System.err.println("Application icon not found - the app will use a default icon");
            }

            // Set up the main window properties
            // This is like decorating your store window - making it look professional
            stage.setTitle("Minot'Or Analytics Dashboard");

            // Set minimum window size so users can't make it too small to be usable
            // Think of this as setting a "smallest allowed size" for your window
            stage.setMinWidth(1200);
            stage.setMinHeight(800);

            // Load and display the login screen as the first thing users see
            // This uses our SceneManager helper to switch to the login view
            SceneManager.setScene(stage, "/org/minotor/analytics/login-view.fxml", true);

            // Actually show the window to the user
            // Without this line, the app would run but be invisible!
            stage.show();

        } catch (Exception e) {
            // If anything goes wrong during startup, print the error details
            // This helps developers figure out what went wrong
            e.printStackTrace();
        }
    }

    /**
     * The very first method that runs when you start the application.
     * This is the true entry point - like the "main" function in other programming languages.
     *
     * Java requires this exact method signature (public static void main) to recognize
     * this class as something that can be run as a program.
     *
     * This method tells JavaFX to start the application and call the start() method above.
     *
     * @param args Command line arguments passed to the application (usually empty for GUI apps)
     */
    public static void main(String[] args) {
        // Tell JavaFX to launch this application
        // This will create a new instance of Main and call its start() method
        launch(args);
    }
}