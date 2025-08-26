package org.minotor.analytics.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.minotor.analytics.utils.SceneManager;

public class LoginController {
    @FXML
    private ImageView logoImageView;

    @FXML
    public void initialize() {
        var stream = getClass().getResourceAsStream("/org/minotor/analytics/images/logo1_minotor.png");
        if (stream == null) {
            System.out.println("Image non trouvée !");
        } else {
            System.out.println("Image trouvée !");
            logoImageView.setImage(new Image(stream));
        }
    }

    @FXML
    private void onLoginButtonClick(ActionEvent event) {
        if (checkLogin()) {
            SceneManager.setScene(event, "/org/minotor/analytics/dashboard-view.fxml", true);
        }
    }

    private boolean checkLogin() {
        // Logique d'authentification à compléter
        return true;
    }
}