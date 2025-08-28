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

public class LoginController implements Initializable {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private ImageView logoImageView;

    private AuthService authService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthService();

        // Charger le logo
        var stream = getClass().getResourceAsStream("/org/minotor/analytics/images/logo_transparent.png");
        if (stream != null) {
            logoImageView.setImage(new Image(stream));
        }

        // Masquer l'indicateur de chargement par défaut
        loadingIndicator.setVisible(false);
        errorLabel.setVisible(false);

        // Permettre la connexion avec Entrée
        passwordField.setOnAction(this::onLoginButtonClick);

        // Charger le CSS après que la scène soit prête
        Platform.runLater(() -> {
            loadModernStyles();
        });
    }

    private void loadModernStyles() {
        try {
            // Attendre que la scène soit disponible
            if (emailField.getScene() != null) {
                String cssPath = "/modern-dashboard.css"; // Chemin corrigé
                var cssUrl = getClass().getResource(cssPath);
                System.out.println("🔍 Recherche CSS à : " + cssPath);
                System.out.println("🔍 Fichier trouvé : " + (cssUrl != null ? cssUrl.toString() : "NON TROUVÉ"));

                if (cssUrl != null) {
                    String cssString = cssUrl.toExternalForm();

                    // Appliquer le CSS à la scène
                    emailField.getScene().getStylesheets().clear();
                    emailField.getScene().getStylesheets().add(cssString);

                    // Forcer l'application des styles
                    emailField.getScene().getRoot().applyCss();

                    System.out.println("✅ CSS Login chargé et appliqué avec succès");
                }
            } else {
                // Réessayer plus tard si la scène n'est pas prête
                Platform.runLater(() -> loadModernStyles());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement CSS login : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onLoginButtonClick(ActionEvent event) {
        System.out.println("🔍 Début de la connexion...");

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        System.out.println("📧 Email: " + email);
        System.out.println("🔒 Mot de passe: " + (password.isEmpty() ? "VIDE" : "NON VIDE (" + password.length() + " chars)"));

        // Validation des champs
        if (email.isEmpty()) {
            System.out.println("❌ Email vide");
            showError("Veuillez saisir votre adresse email");
            emailField.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            System.out.println("❌ Mot de passe vide");
            showError("Veuillez saisir votre mot de passe");
            passwordField.requestFocus();
            return;
        }

        System.out.println("✅ Validations passées, démarrage de la connexion...");

        // Désactiver les contrôles pendant la connexion
        setControlsEnabled(false);
        showLoading(true);
        hideError();

        // Exécuter la connexion en arrière-plan
        Task<AuthService.AuthResult> loginTask = new Task<>() {
            @Override
            protected AuthService.AuthResult call() {
                System.out.println("🌐 Appel API en cours...");
                AuthService.AuthResult result = authService.login(email, password);
                System.out.println("📡 Réponse API reçue: " + result.isSuccess() + " - " + result.getMessage());
                return result;
            }
        };

        loginTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                System.out.println("✅ Tâche de connexion terminée avec succès");
                AuthService.AuthResult result = loginTask.getValue();

                if (result.isSuccess()) {
                    System.out.println("✅ Connexion réussie, redirection vers dashboard...");
                    SceneManager.setScene(event, "/org/minotor/analytics/dashboard-view.fxml", true);
                } else {
                    System.out.println("❌ Échec de connexion: " + result.getMessage());
                    // Gestion spécifique selon le type d'erreur
                    String errorMessage = result.getMessage();

                    if (errorMessage.contains("Droits insuffisants")) {
                        showError("❌ " + errorMessage);
                    } else if (errorMessage.contains("incorrect")) {
                        showError("🔐 " + errorMessage);
                        // Effacer le mot de passe en cas d'erreur d'authentification
                        passwordField.clear();
                        emailField.requestFocus();
                    } else {
                        showError("⚠️ " + errorMessage);
                    }

                    setControlsEnabled(true);
                    showLoading(false);
                }
            });
        });

        loginTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                System.err.println("💥 Échec de la tâche de connexion: " + e.getSource().getException().getMessage());
                e.getSource().getException().printStackTrace();
                showError("❌ Erreur de connexion. Veuillez réessayer.");
                setControlsEnabled(true);
                showLoading(false);
            });
        });

        // Exécuter la tâche dans un thread séparé
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();

        System.out.println("🚀 Thread de connexion démarré");
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".");
    }

    private void setControlsEnabled(boolean enabled) {
        emailField.setDisable(!enabled);
        passwordField.setDisable(!enabled);
        loginButton.setDisable(!enabled);
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        loginButton.setText(show ? "Connexion..." : "Se connecter");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}