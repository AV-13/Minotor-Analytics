package org.minotor.analytics.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.minotor.analytics.service.AuthService;
import org.minotor.analytics.utils.SceneManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the login screen of the application.
 * This class handles user login and manages the UI elements.
 */
public class LoginController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    // UI elements from the FXML file
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ImageView logoImageView;

    private AuthService authService;

    /**
     * Called when the login screen is loaded.
     * Sets up the UI and prepares everything for user interaction.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthService();

        // Load the company logo
        var stream = getClass().getResourceAsStream("/org/minotor/analytics/images/logo_transparent.png");
        if (stream != null) {
            logoImageView.setImage(new Image(stream));
        }

        // Hide loading spinner and error message by default
        loadingIndicator.setVisible(false);
        errorLabel.setVisible(false);

        // Allow login by pressing Enter key in password field
        passwordField.setOnAction(this::onLoginButtonClick);

        // Apply styles when everything is ready
        Platform.runLater(this::loadModernStyles);
    }

    /**
     * Loads CSS styles to make the login screen look nice.
     * This method tries to apply modern styling to the interface.
     */
    private void loadModernStyles() {
        try {
            // Wait until the scene is available
            if (emailField.getScene() != null) {
                String cssPath = "/modern-dashboard.css";
                var cssUrl = getClass().getResource(cssPath);
                LOGGER.info("Recherche CSS à : " + cssPath);
                LOGGER.info("Fichier trouvé : " + (cssUrl != null ? cssUrl.toString() : "NON TROUVÉ"));

                if (cssUrl != null) {
                    String cssString = cssUrl.toExternalForm();

                    // Apply CSS to the scene
                    emailField.getScene().getStylesheets().clear();
                    emailField.getScene().getStylesheets().add(cssString);

                    // Force styles to be applied
                    emailField.getScene().getRoot().applyCss();

                    LOGGER.info("CSS Login chargé et appliqué avec succès");
                }
            } else {
                // Try again later if scene is not ready
                Platform.runLater(this::loadModernStyles);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur chargement CSS login", e);
        }
    }

    /**
     * This method runs when user clicks the login button.
     * It checks the email and password, then tries to log the user in.
     */
    @FXML
    private void onLoginButtonClick(ActionEvent event) {
        LOGGER.info("Début de la connexion...");

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Check if email field is empty
        if (email.isEmpty()) {
            LOGGER.warning("Email vide");
            showError("Veuillez saisir votre adresse email");
            emailField.requestFocus();
            return;
        }

        // Check if password field is empty
        if (password.isEmpty()) {
            LOGGER.warning("Mot de passe vide");
            showError("Veuillez saisir votre mot de passe");
            passwordField.requestFocus();
            return;
        }

        LOGGER.info("Validations passées, démarrage de la connexion...");

        // Disable buttons while login is happening
        setControlsEnabled(false);
        showLoading(true);
        hideError();

        // Create a background task to handle login (prevents UI freezing)
        Task<AuthService.AuthResult> loginTask = new Task<>() {
            @Override
            protected AuthService.AuthResult call() {
                LOGGER.info("Appel API en cours...");
                AuthService.AuthResult result = authService.login(email, password);
                LOGGER.info("Réponse API reçue: " + result.isSuccess() + " - " + result.getMessage());
                return result;
            }
        };

        // What to do when login succeeds
        loginTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                LOGGER.info("Tâche de connexion terminée avec succès");
                AuthService.AuthResult result = loginTask.getValue();

                if (result.isSuccess()) {
                    LOGGER.info("Connexion réussie, redirection vers dashboard...");
                    // Go to main dashboard screen
                    SceneManager.setScene(event, "/org/minotor/analytics/dashboard-view.fxml", true);
                } else {
                    LOGGER.warning("Échec de connexion: " + result.getMessage());
                    // Show different error messages based on problem type
                    String errorMessage = result.getMessage();

                    if (errorMessage.contains("Droits insuffisants")) {
                        showError(errorMessage);
                    } else if (errorMessage.contains("incorrect")) {
                        showError(errorMessage);
                        // Clear password if login credentials are wrong
                        passwordField.clear();
                        emailField.requestFocus();
                    } else {
                        showError(errorMessage);
                    }

                    setControlsEnabled(true);
                    showLoading(false);
                }
            });
        });

        // What to do if login completely fails
        loginTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                LOGGER.log(Level.SEVERE, "Échec de la tâche de connexion", e.getSource().getException());
                showError("Erreur de connexion. Veuillez réessayer.");
                setControlsEnabled(true);
                showLoading(false);
            });
        });

        // Start the login process in background
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();

        LOGGER.info("Thread de connexion démarré");
    }

    /**
     * Enable or disable the input fields and login button
     */
    private void setControlsEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
        loginButton.setDisable(!enabled);
    }

    /**
     * Show or hide the loading spinner and change button text
     */
    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        loginButton.setText(show ? "Connexion..." : "Se connecter");
    }

    /**
     * Display an error message to the user
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    /**
     * Hide the error message
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}