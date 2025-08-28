package org.minotor.analytics.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.minotor.analytics.model.AnalyticsEvent;
import org.minotor.analytics.service.AnalyticsService;
import org.minotor.analytics.service.AuthService;
import org.minotor.analytics.utils.SceneManager;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {
    // Sections principales
    private AuthService authService;
    @FXML private Label profileNameLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Label profileEmailLabel;
    @FXML private VBox dashboardSection;
    @FXML private VBox pagesSection;
    @FXML private VBox devicesSection;
    @FXML private VBox eventsSection;
    @FXML private VBox reportsSection;

    // Boutons de navigation sidebar
    @FXML private Button dashboardBtn;
    @FXML private Button pagesBtn;
    @FXML private Button devicesBtn;
    @FXML private Button eventsBtn;
    @FXML private Button reportsBtn;
    @FXML private Button settingsBtn;

    // Graphiques additionnels pour les sections détaillées
    @FXML private BarChart<String, Number> pagesDetailChart;
    @FXML private PieChart devicesDetailChart;
    @FXML private TableView<Object> deviceStatsTable;
    @FXML private PieChart eventTypesChart;
    @FXML private BarChart<String, Number> eventTypesBarChart;

    // Labels additionnels
    @FXML private Label uniquePagesLabel;
    @FXML private Label uniqueDevicesLabel;
    // En-tête
    @FXML private ImageView logoImageView;
    @FXML private Label statsLabel;

    // Boutons de filtre
    @FXML private Button filterTodayBtn;
    @FXML private Button filterWeekBtn;
    @FXML private Button filterMonthBtn;
    @FXML private Button filterAllBtn;

    // Table
    @FXML private TableView<AnalyticsEvent> analyticsTable;
    @FXML private TableColumn<AnalyticsEvent, String> urlColumn;
    @FXML private TableColumn<AnalyticsEvent, String> dateColumn;
    @FXML private TableColumn<AnalyticsEvent, String> deviceColumn;
    @FXML private TableColumn<AnalyticsEvent, String> typeColumn;
    // Colonnes de la table des appareils
    @FXML private TableColumn<Object, String> deviceNameColumn;
    @FXML private TableColumn<Object, Number> deviceVisitsColumn;
    @FXML private TableColumn<Object, String> devicePercentColumn;
    // Graphiques
    @FXML private BarChart<String, Number> pageViewsChart;
    @FXML private PieChart deviceChart;

    // Labels statistiques
    @FXML private Label totalEventsLabel;

    // Boutons
    @FXML private Button logoutButton;

    private AnalyticsService analyticsService;
    private ObservableList<AnalyticsEvent> eventsList;
    private AnalyticsService.Period currentPeriod = AnalyticsService.Period.ALL;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        authService = new AuthService();
        // Charger le logo
        var stream = getClass().getResourceAsStream("/org/minotor/analytics/images/logo_transparent.png");
        if (stream != null) {
            logoImageView.setImage(new Image(stream));
        }

        analyticsService = new AnalyticsService();
        eventsList = FXCollections.observableArrayList();

        setupTable();
        setupCharts();
        loadData();
        loadUserProfile();

        // Mettre le bouton "Tout" en surbrillance par défaut
        highlightActiveFilter(filterAllBtn);

        // Programmer le chargement du CSS après la construction de la scène
        scheduleStyleLoading();
    }

    private void scheduleStyleLoading() {
        // Attendre que la scène soit prête
        Platform.runLater(() -> {
            if (filterAllBtn.getScene() != null) {
                loadModernStyles();
            } else {
                // Si la scène n'est toujours pas prête, réessayer
                Platform.runLater(() -> {
                    if (filterAllBtn.getScene() != null) {
                        loadModernStyles();
                    } else {
                        // Dernier essai avec un délai
                        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                                new javafx.animation.KeyFrame(javafx.util.Duration.millis(100), e -> {
                                    if (filterAllBtn.getScene() != null) {
                                        loadModernStyles();
                                    }
                                })
                        );
                        timeline.play();
                    }
                });
            }
        });
    }

    private void loadModernStyles() {
        try {
            String cssPath = "/modern-dashboard.css";
            var cssUrl = getClass().getResource(cssPath);
            System.out.println("🔍 Recherche CSS à : " + cssPath);
            System.out.println("🔍 Fichier trouvé : " + (cssUrl != null ? cssUrl.toString() : "NON TROUVÉ"));

            if (cssUrl != null && filterAllBtn.getScene() != null) {
                String cssString = cssUrl.toExternalForm();

                filterAllBtn.getScene().getStylesheets().clear();
                filterAllBtn.getScene().getStylesheets().add(cssString);

                // Forcer l'application des styles
                filterAllBtn.getScene().getRoot().applyCss();

                System.out.println("✅ CSS Minot'Or chargé et appliqué avec succès");
            } else {
                System.err.println("❌ CSS non appliqué - scène : " + (filterAllBtn.getScene() != null));
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement CSS : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applySidebarButtonStyles() {
        try {
            var scene = filterAllBtn.getScene();
            if (scene != null && scene.getRoot() != null) {
                // Utiliser lookup au lieu de lookupAll
                var root = scene.getRoot();

                // Parcourir récursivement pour trouver les boutons avec la classe
                applySidebarStylesRecursive(root);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur application styles sidebar: " + e.getMessage());
        }
    }

    private void applySidebarStylesRecursive(javafx.scene.Node node) {
        if (node instanceof Button btn) {
            // Vérifier si le bouton a la classe CSS sidebar-menu-item
            if (btn.getStyleClass().contains("sidebar-menu-item")) {
                btn.setPrefHeight(44);
                btn.setMaxWidth(Double.MAX_VALUE);
            }
        }

        // Si c'est un Parent, parcourir ses enfants
        if (node instanceof javafx.scene.Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                applySidebarStylesRecursive(child);
            }
        }
    }
    private void setupTable() {
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        dateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        deviceColumn.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("eventType"));

        analyticsTable.setItems(eventsList);
    }

    private void setupCharts() {
        // Configuration du graphique des pages
        pageViewsChart.setTitle("Pages les plus visitées");
        pageViewsChart.setLegendVisible(false);

        // Configuration des graphiques circulaires
        deviceChart.setTitle("Répartition par appareil");
    }

    private void loadData() {
        try {
            List<AnalyticsEvent> events = analyticsService.getEventsByPeriod(currentPeriod);
            eventsList.clear();
            eventsList.addAll(events);

            updatePageViewsChart();
            updateDeviceChart();
            updateStatsLabel();

            // Mettre à jour les graphiques des sections spécialisées
            updatePagesDetailChart();
            updateDevicesDetailChart();
            updateEventTypesCharts();

            System.out.println("Données chargées: " + events.size() + " événements pour la période " + currentPeriod);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updatePageViewsChart() {
        pageViewsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Visites");

        Map<String, Long> pageStats = analyticsService.getPageStatistics();

        // Limiter aux 10 pages les plus visitées pour la lisibilité
        pageStats.entrySet().stream()
                .limit(10)
                .forEach(entry -> {
                    String shortUrl = entry.getKey().length() > 30 ?
                            entry.getKey().substring(0, 27) + "..." : entry.getKey();
                    series.getData().add(new XYChart.Data<>(shortUrl, entry.getValue()));
                });

        pageViewsChart.getData().add(series);
    }

    private void updateDeviceChart() {
        deviceChart.getData().clear();

        Map<String, Long> deviceStats = analyticsService.getDeviceTypeStats();

        deviceStats.forEach((device, count) -> {
            String label = device != null ? device : "Inconnu";
            deviceChart.getData().add(new PieChart.Data(label + " (" + count + ")", count));
        });
    }

    private void updateStatsLabel() {
        int totalEvents = eventsList.size();
        String periodText = switch (currentPeriod) {
            case TODAY -> "aujourd'hui";
            case WEEK -> "cette semaine";
            case MONTH -> "ce mois";
            case ALL -> "au total";
        };

        // Mettre à jour le label des événements totaux
        totalEventsLabel.setText(String.valueOf(totalEvents));

        // Afficher des stats dans la console si statsLabel n'existe pas
        if (statsLabel != null) {
            statsLabel.setText("📅 " + totalEvents + " événements " + periodText);
        } else {
            System.out.println("📊 " + totalEvents + " événements " + periodText);
        }

        // Mettre à jour les autres statistiques
        updateAdditionalStats();
    }
    private void updateAdditionalStats() {
        // Mettre à jour les pages uniques
        Map<String, Long> pageStats = analyticsService.getPageStatistics();
        uniquePagesLabel.setText(String.valueOf(pageStats.size()));

        // Mettre à jour les types d'appareils uniques
        Map<String, Long> deviceStats = analyticsService.getDeviceTypeStats();
        uniqueDevicesLabel.setText(String.valueOf(deviceStats.size()));
    }

    private void highlightActiveFilter(Button activeButton) {
        // Réinitialiser le style de tous les boutons
        String defaultStyle = "-fx-background-color: #e0e0e0; -fx-text-fill: #333;";
        String activeStyle = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;";

        filterTodayBtn.setStyle(defaultStyle);
        filterWeekBtn.setStyle(defaultStyle);
        filterMonthBtn.setStyle(defaultStyle);
        filterAllBtn.setStyle(defaultStyle);

        // Mettre en surbrillance le bouton actif
        activeButton.setStyle(activeStyle);
    }

    // Méthodes de filtrage
    @FXML
    private void filterToday() {
        currentPeriod = AnalyticsService.Period.TODAY;
        highlightActiveFilter(filterTodayBtn);
        loadData();
    }

    @FXML
    private void filterWeek() {
        currentPeriod = AnalyticsService.Period.WEEK;
        highlightActiveFilter(filterWeekBtn);
        loadData();
    }

    @FXML
    private void filterMonth() {
        currentPeriod = AnalyticsService.Period.MONTH;
        highlightActiveFilter(filterMonthBtn);
        loadData();
    }

    @FXML
    private void filterAll() {
        currentPeriod = AnalyticsService.Period.ALL;
        highlightActiveFilter(filterAllBtn);
        loadData();
    }

    @FXML
    private void refreshData() {
        System.out.println("🔄 Actualisation des données...");
        loadData();
        System.out.println("✅ Données actualisées");
    }

    @FXML
    private void onLogoutButtonClick(ActionEvent event) {
        if (analyticsService != null) {
            analyticsService.close();
        }
        SceneManager.setScene(event, "/org/minotor/analytics/login-view.fxml", false);
    }
    @FXML
    private void exportData() {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Exporter les données");
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            java.io.File file = fileChooser.showSaveDialog(filterAllBtn.getScene().getWindow());

            if (file != null) {
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    // En-tête CSV
                    writer.println("URL,Date,Appareil,Pays,Durée,Type");

                    // Données
                    for (AnalyticsEvent event : eventsList) {
                        writer.printf("%s,%s,%s,%s,%s,%s%n",
                                event.getUrl() != null ? event.getUrl() : "",
                                event.getFormattedDate(),
                                event.getDeviceType() != null ? event.getDeviceType() : "",
                                event.getEventType() != null ? event.getEventType() : ""
                        );
                    }

                    System.out.println("✅ Données exportées vers: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'export: " + e.getMessage());
        }
    }

    @FXML
    private Button toggleTableBtn;

    @FXML
    private void toggleTable() {
        boolean isVisible = analyticsTable.isVisible();
        analyticsTable.setVisible(!isVisible);
        analyticsTable.setManaged(!isVisible);

        toggleTableBtn.setText(isVisible ? "▼ Afficher" : "▲ Masquer");
    }
    @FXML
    private void showDashboard() {
        hideAllSections();
        dashboardSection.setVisible(true);
        dashboardSection.setManaged(true);
        updateActiveButton(dashboardBtn);
    }

    @FXML
    private void showPages() {
        hideAllSections();
        pagesSection.setVisible(true);
        pagesSection.setManaged(true);
        updateActiveButton(pagesBtn);
        updatePagesDetailChart();
    }

    @FXML
    private void showDevices() {
        hideAllSections();
        devicesSection.setVisible(true);
        devicesSection.setManaged(true);
        updateActiveButton(devicesBtn);
        updateDevicesDetailChart();
    }

    @FXML
    private void showEvents() {
        hideAllSections();
        eventsSection.setVisible(true);
        eventsSection.setManaged(true);
        updateActiveButton(eventsBtn);
        updateEventTypesCharts();
    }

    @FXML
    private void showReports() {
        hideAllSections();
        reportsSection.setVisible(true);
        reportsSection.setManaged(true);
        updateActiveButton(reportsBtn);
    }

    @FXML
    private void showSettings() {
        hideAllSections();
        // Ajouter une section settings si nécessaire
        updateActiveButton(settingsBtn);
    }

    private void hideAllSections() {
        dashboardSection.setVisible(false);
        dashboardSection.setManaged(false);
        pagesSection.setVisible(false);
        pagesSection.setManaged(false);
        devicesSection.setVisible(false);
        devicesSection.setManaged(false);
        eventsSection.setVisible(false);
        eventsSection.setManaged(false);
        reportsSection.setVisible(false);
        reportsSection.setManaged(false);
    }

    private void updateActiveButton(Button activeButton) {
        // Retirer la classe active de tous les boutons
        dashboardBtn.getStyleClass().remove("sidebar-menu-active");
        pagesBtn.getStyleClass().remove("sidebar-menu-active");
        devicesBtn.getStyleClass().remove("sidebar-menu-active");
        eventsBtn.getStyleClass().remove("sidebar-menu-active");
        reportsBtn.getStyleClass().remove("sidebar-menu-active");
        settingsBtn.getStyleClass().remove("sidebar-menu-active");

        // Ajouter la classe active au bouton sélectionné
        activeButton.getStyleClass().add("sidebar-menu-active");
    }
    private void updatePagesDetailChart() {
        if (pagesDetailChart != null) {
            pagesDetailChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Visites détaillées");

            Map<String, Long> pageStats = analyticsService.getPageStatistics();

            pageStats.entrySet().stream()
                    .forEach(entry -> {
                        String shortUrl = entry.getKey().length() > 20 ?
                                entry.getKey().substring(0, 17) + "..." : entry.getKey();
                        series.getData().add(new XYChart.Data<>(shortUrl, entry.getValue()));
                    });

            pagesDetailChart.getData().add(series);
        }
    }

    private void updateDevicesDetailChart() {
        if (devicesDetailChart != null) {
            devicesDetailChart.getData().clear();

            Map<String, Long> deviceStats = analyticsService.getDeviceTypeStats();

            deviceStats.forEach((device, count) -> {
                String label = device != null ? device : "Inconnu";
                devicesDetailChart.getData().add(new PieChart.Data(label + " (" + count + ")", count));
            });
        }
    }

    private void updateEventTypesCharts() {
        // Mise à jour du graphique circulaire des types d'événements
        if (eventTypesChart != null) {
            eventTypesChart.getData().clear();

            Map<String, Long> eventStats = analyticsService.getEventTypeStats();

            eventStats.forEach((type, count) -> {
                String label = type != null ? type : "Inconnu";
                eventTypesChart.getData().add(new PieChart.Data(label + " (" + count + ")", count));
            });
        }

        // Mise à jour du graphique en barres des types d'événements
        if (eventTypesBarChart != null) {
            eventTypesBarChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Événements par type");

            Map<String, Long> eventStats = analyticsService.getEventTypeStats();

            eventStats.forEach((type, count) -> {
                String label = type != null ? type : "Inconnu";
                series.getData().add(new XYChart.Data<>(label, count));
            });

            eventTypesBarChart.getData().add(series);
        }
    }
    private void loadUserProfile() {
        try {
            JsonNode user = authService.getCurrentUser();

            if (user != null) {
                String firstName = user.has("firstName") ? user.get("firstName").asText() : "";
                String lastName = user.has("lastName") ? user.get("lastName").asText() : "";
                String email = user.has("email") ? user.get("email").asText() : "";
                String role = user.has("role") ? user.get("role").asText() : "";

                // Mettre à jour les labels
                if (profileNameLabel != null) {
                    String fullName = (firstName + " " + lastName).trim();
                    profileNameLabel.setText(!fullName.isEmpty() ? fullName : email);
                }

                if (profileRoleLabel != null) {
                    String displayRole = translateRole(role);
                    profileRoleLabel.setText(displayRole);
                }

                if (profileEmailLabel != null) {
                    profileEmailLabel.setText(email);
                }

                System.out.println("✅ Profil utilisateur chargé: " + firstName + " " + lastName);
            } else {
                // Valeurs par défaut si pas de données
                if (profileNameLabel != null) profileNameLabel.setText("Utilisateur");
                if (profileRoleLabel != null) profileRoleLabel.setText("Utilisateur");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement profil: " + e.getMessage());
            // Valeurs par défaut en cas d'erreur
            if (profileNameLabel != null) profileNameLabel.setText("Utilisateur");
            if (profileRoleLabel != null) profileRoleLabel.setText("Utilisateur");
        }
    }

    private String translateRole(String role) {
        return switch (role.toUpperCase()) {
            case "ROLE_ADMIN" -> "Administrateur";
            case "ROLE_SALES" -> "Commercial";
            case "ROLE_USER" -> "Utilisateur";
            default -> "Utilisateur";
        };
    }
}