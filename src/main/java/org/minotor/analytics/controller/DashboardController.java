package org.minotor.analytics.controller;

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
import org.minotor.analytics.model.AnalyticsEvent;
import org.minotor.analytics.service.AnalyticsService;
import org.minotor.analytics.utils.SceneManager;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

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
    @FXML private TableColumn<AnalyticsEvent, String> countryColumn;
    @FXML private TableColumn<AnalyticsEvent, String> durationColumn;
    @FXML private TableColumn<AnalyticsEvent, String> typeColumn;

    // Graphiques
    @FXML private BarChart<String, Number> pageViewsChart;
    @FXML private PieChart deviceChart;
    @FXML private PieChart countryChart;

    // Labels statistiques
    @FXML private Label avgDurationLabel;
    @FXML private Label bounceRateLabel;
    @FXML private Label totalEventsLabel;

    // Boutons
    @FXML private Button logoutButton;

    private AnalyticsService analyticsService;
    private ObservableList<AnalyticsEvent> eventsList;
    private AnalyticsService.Period currentPeriod = AnalyticsService.Period.ALL;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        updateStatistics();

        // Mettre le bouton "Tout" en surbrillance par défaut
        highlightActiveFilter(filterAllBtn);
    }

    private void setupTable() {
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        dateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        deviceColumn.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
        countryColumn.setCellValueFactory(new PropertyValueFactory<>("country"));
        durationColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDuration()));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("eventType"));

        analyticsTable.setItems(eventsList);
    }

    private void setupCharts() {
        // Configuration du graphique des pages
        pageViewsChart.setTitle("Pages les plus visitées");
        pageViewsChart.setLegendVisible(false);

        // Configuration des graphiques circulaires
        deviceChart.setTitle("Répartition par appareil");
        countryChart.setTitle("Top 10 pays");
    }

    private void loadData() {
        try {
            List<AnalyticsEvent> events = analyticsService.getEventsByPeriod(currentPeriod);
            eventsList.clear();
            eventsList.addAll(events);

            updatePageViewsChart();
            updateDeviceChart();
            updateCountryChart();
            updateStatsLabel();

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

    private void updateCountryChart() {
        countryChart.getData().clear();

        Map<String, Long> countryStats = analyticsService.getCountryStats();

        countryStats.forEach((country, count) -> {
            String label = country != null ? country : "Inconnu";
            countryChart.getData().add(new PieChart.Data(label + " (" + count + ")", count));
        });
    }

    private void updateStatistics() {
        try {
            // Durée moyenne des sessions
            Double avgDuration = analyticsService.getAverageSessionDuration();
            if (avgDuration != null && avgDuration > 0) {
                long seconds = (long) (avgDuration / 1000);
                avgDurationLabel.setText(seconds + " secondes");
            } else {
                avgDurationLabel.setText("N/A");
            }

            // Taux de rebond
            Double bounceRate = analyticsService.getBounceRate();
            if (bounceRate != null) {
                bounceRateLabel.setText(String.format("%.1f%%", bounceRate));
            } else {
                bounceRateLabel.setText("N/A");
            }

            // Total des événements
            totalEventsLabel.setText(String.valueOf(eventsList.size()));

        } catch (Exception e) {
            System.err.println("Erreur mise à jour statistiques: " + e.getMessage());
            avgDurationLabel.setText("Erreur");
            bounceRateLabel.setText("Erreur");
            totalEventsLabel.setText("Erreur");
        }
    }

    private void updateStatsLabel() {
        int totalEvents = eventsList.size();
        String periodText = switch (currentPeriod) {
            case TODAY -> "aujourd'hui";
            case WEEK -> "cette semaine";
            case MONTH -> "ce mois";
            case ALL -> "au total";
        };

        statsLabel.setText(totalEvents + " événements " + periodText);
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
        updateStatistics();
    }

    @FXML
    private void filterWeek() {
        currentPeriod = AnalyticsService.Period.WEEK;
        highlightActiveFilter(filterWeekBtn);
        loadData();
        updateStatistics();
    }

    @FXML
    private void filterMonth() {
        currentPeriod = AnalyticsService.Period.MONTH;
        highlightActiveFilter(filterMonthBtn);
        loadData();
        updateStatistics();
    }

    @FXML
    private void filterAll() {
        currentPeriod = AnalyticsService.Period.ALL;
        highlightActiveFilter(filterAllBtn);
        loadData();
        updateStatistics();
    }

    @FXML
    private void refreshData() {
        System.out.println("🔄 Actualisation des données...");
        loadData();
        updateStatistics();
        System.out.println("✅ Données actualisées");
    }

    @FXML
    private void onLogoutButtonClick(ActionEvent event) {
        if (analyticsService != null) {
            analyticsService.close();
        }
        SceneManager.setScene(event, "/org/minotor/analytics/login-view.fxml", false);
    }
}