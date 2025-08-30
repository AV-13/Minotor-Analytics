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
import org.minotor.analytics.model.User;
import org.minotor.analytics.service.AnalyticsService;
import org.minotor.analytics.service.AuthService;
import org.minotor.analytics.utils.SceneManager;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * DashboardController - Main controller for the analytics dashboard interface
 *
 * This controller manages the entire dashboard application including:
 * - Multiple dashboard sections (overview, pages, devices, events, reports)
 * - Data visualization with charts and tables
 * - User authentication and profile management
 * - Data filtering and export functionality
 * - Dynamic CSS styling and UI updates
 *
 * The controller implements JavaFX Initializable interface and serves as the
 * central hub for analytics data presentation and user interaction.
 *
 * @author AV-13
 * @version 1.0
 * @since 2024
 */
public class DashboardController implements Initializable {

    // ===== SERVICES =====

    /**
     * Service for handling user authentication and profile management
     */
    private AuthService authService;

    /**
     * Service for retrieving and processing analytics data
     */
    private AnalyticsService analyticsService;

    // ===== UI COMPONENTS - PROFILE SECTION =====
    @FXML
    private Button refreshBtn;
    /**
     * Label displaying the current user's full name
     */
    @FXML private Label profileNameLabel;

    /**
     * Label displaying the current user's role (Admin, Sales, User)
     */
    @FXML private Label profileRoleLabel;

    /**
     * Label displaying the current user's email address
     */
    @FXML private Label profileEmailLabel;

    // ===== UI COMPONENTS - MAIN SECTIONS =====

    /**
     * Main dashboard overview section container
     */
    @FXML private VBox dashboardSection;

    /**
     * Pages analytics section container
     */
    @FXML private VBox pagesSection;

    /**
     * Devices analytics section container
     */
    @FXML private VBox devicesSection;

    /**
     * Events analytics section container
     */
    @FXML private VBox eventsSection;

    /**
     * Reports section container
     */
    @FXML private VBox reportsSection;

    // ===== UI COMPONENTS - NAVIGATION BUTTONS =====

    /**
     * Navigation button for dashboard section
     */
    @FXML private Button dashboardBtn;

    /**
     * Navigation button for pages section
     */
    @FXML private Button pagesBtn;

    /**
     * Navigation button for devices section
     */
    @FXML private Button devicesBtn;

    /**
     * Navigation button for events section
     */
    @FXML private Button eventsBtn;

    /**
     * Navigation button for reports section
     */
    @FXML private Button reportsBtn;

    /**
     * Navigation button for settings section
     */
    @FXML private Button settingsBtn;

    // ===== UI COMPONENTS - CHARTS =====

    /**
     * Bar chart displaying page visit statistics
     */
    @FXML private BarChart<String, Number> pageViewsChart;

    /**
     * Pie chart showing device type distribution
     */
    @FXML private PieChart deviceChart;

    /**
     * Detailed bar chart for pages section
     */
    @FXML private BarChart<String, Number> pagesDetailChart;

    /**
     * Detailed pie chart for devices section
     */
    @FXML private PieChart devicesDetailChart;

    /**
     * Pie chart showing event types distribution
     */
    @FXML private PieChart eventTypesChart;

    /**
     * Bar chart showing event types statistics
     */
    @FXML private BarChart<String, Number> eventTypesBarChart;

    // ===== UI COMPONENTS - TABLES =====

    /**
     * Main table displaying analytics events
     */
    @FXML private TableView<AnalyticsEvent> analyticsTable;

    /**
     * Table column for URL data
     */
    @FXML private TableColumn<AnalyticsEvent, String> urlColumn;

    /**
     * Table column for date data
     */
    @FXML private TableColumn<AnalyticsEvent, String> dateColumn;

    /**
     * Table column for device type data
     */
    @FXML private TableColumn<AnalyticsEvent, String> deviceColumn;

    /**
     * Table column for event type data
     */
    @FXML private TableColumn<AnalyticsEvent, String> typeColumn;

    /**
     * Table for device statistics in devices section
     */
    @FXML private TableView<Object> deviceStatsTable;

    /**
     * Device name column in device statistics table
     */
    @FXML private TableColumn<Object, String> deviceNameColumn;

    /**
     * Device visits column in device statistics table
     */
    @FXML private TableColumn<Object, Number> deviceVisitsColumn;

    /**
     * Device percentage column in device statistics table
     */
    @FXML private TableColumn<Object, String> devicePercentColumn;

    // ===== UI COMPONENTS - FILTER BUTTONS =====

    /**
     * Filter button for today's data
     */
    @FXML private Button filterTodayBtn;

    /**
     * Filter button for this week's data
     */
    @FXML private Button filterWeekBtn;

    /**
     * Filter button for this month's data
     */
    @FXML private Button filterMonthBtn;

    /**
     * Filter button for all data
     */
    @FXML private Button filterAllBtn;

    // ===== UI COMPONENTS - LABELS AND STATISTICS =====

    /**
     * Application logo image view
     */
    @FXML private ImageView logoImageView;

    /**
     * Main statistics label
     */
    @FXML private Label statsLabel;

    /**
     * Label showing total number of events
     */
    @FXML private Label totalEventsLabel;

    /**
     * Label showing number of unique pages
     */
    @FXML private Label uniquePagesLabel;

    /**
     * Label showing number of unique devices
     */
    @FXML private Label uniqueDevicesLabel;

    // ===== UI COMPONENTS - ACTION BUTTONS =====

    /**
     * Button for user logout functionality
     */
    @FXML private Button logoutButton;

    /**
     * Button to toggle table visibility
     */
    @FXML private Button toggleTableBtn;

    // ===== DATA MANAGEMENT =====

    /**
     * Observable list containing analytics events for table display
     */
    private ObservableList<AnalyticsEvent> eventsList;

    /**
     * Current time period filter applied to data
     */
    private AnalyticsService.Period currentPeriod = AnalyticsService.Period.ALL;

    // ===== INITIALIZATION METHODS =====

    /**
     * Initializes the controller after FXML loading.
     * Sets up services, UI components, loads initial data, and applies styling.
     *
     * @param location The location used to resolve relative paths for the root object
     * @param resources The resources used to localize the root object
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize services - RÉCUPÉRER AuthService depuis SceneManager
        authService = SceneManager.getAuthService(); // Au lieu de new AuthService()
        analyticsService = new AnalyticsService();
        eventsList = FXCollections.observableArrayList();

        // Load application logo
        loadApplicationLogo();

        // Setup UI components
        setupTable();
        setupCharts();

        // Load initial data
        loadData();
        loadUserProfile(); // Cette méthode va maintenant fonctionner

        // Set default filter highlighting
        highlightActiveFilter(filterAllBtn);

        // Schedule CSS loading after scene construction
        scheduleStyleLoading();
    }
    /**
     * Loads the application logo from resources.
     * Falls back gracefully if logo file is not found.
     */
    private void loadApplicationLogo() {
        var stream = getClass().getResourceAsStream("/org/minotor/analytics/images/logo_transparent.png");
        if (stream != null) {
            logoImageView.setImage(new Image(stream));
        }
    }

    // ===== STYLING METHODS =====

    /**
     * Schedules CSS loading after the JavaFX scene is fully constructed.
     * Uses Platform.runLater to ensure proper timing with scene availability.
     */
    private void scheduleStyleLoading() {
        Platform.runLater(() -> {
            if (filterAllBtn.getScene() != null) {
                loadModernStyles();
            } else {
                // Retry if scene not ready
                Platform.runLater(() -> {
                    if (filterAllBtn.getScene() != null) {
                        loadModernStyles();
                    } else {
                        // Final attempt with delay
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

    /**
     * Loads and applies modern CSS styling to the application.
     * Clears existing stylesheets and applies the modern dashboard theme.
     */
    private void loadModernStyles() {
        try {
            String cssPath = "/modern-dashboard.css";
            var cssUrl = getClass().getResource(cssPath);

            if (cssUrl != null && filterAllBtn.getScene() != null) {
                String cssString = cssUrl.toExternalForm();
                filterAllBtn.getScene().getStylesheets().clear();
                filterAllBtn.getScene().getStylesheets().add(cssString);
                filterAllBtn.getScene().getRoot().applyCss();
                System.out.println("Modern CSS loaded and applied successfully");
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS: " + e.getMessage());
        }
    }

    /**
     * Applies specific styling to sidebar navigation buttons.
     * Recursively traverses the scene graph to find and style sidebar buttons.
     */
    private void applySidebarButtonStyles() {
        try {
            var scene = filterAllBtn.getScene();
            if (scene != null && scene.getRoot() != null) {
                applySidebarStylesRecursive(scene.getRoot());
            }
        } catch (Exception e) {
            System.err.println("❌ Error applying sidebar styles: " + e.getMessage());
        }
    }

    /**
     * Recursively applies sidebar styling to buttons with specific CSS classes.
     *
     * @param node The current node being processed in the scene graph
     */
    private void applySidebarStylesRecursive(javafx.scene.Node node) {
        if (node instanceof Button btn && btn.getStyleClass().contains("sidebar-menu-item")) {
            btn.setPrefHeight(44);
            btn.setMaxWidth(Double.MAX_VALUE);
        }

        if (node instanceof javafx.scene.Parent parent) {
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                applySidebarStylesRecursive(child);
            }
        }
    }

    // ===== TABLE SETUP METHODS =====

    /**
     * Configures the main analytics table with appropriate cell value factories.
     * Sets up columns for URL, date, device type, and event type data.
     */
    private void setupTable() {
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        dateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedDate()));
        deviceColumn.setCellValueFactory(new PropertyValueFactory<>("deviceType"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("eventType"));

        analyticsTable.setItems(eventsList);
    }

    // ===== CHART SETUP METHODS =====

    /**
     * Initializes chart configurations with titles and basic settings.
     * Sets up page views bar chart and device distribution pie chart.
     */
    private void setupCharts() {
        pageViewsChart.setTitle("Pages les plus visitées");
        pageViewsChart.setLegendVisible(false);
        deviceChart.setTitle("Répartition par appareil");
    }

    // ===== DATA LOADING METHODS =====

    /**
     * Loads analytics data for the current time period and updates all UI components.
     * Refreshes charts, tables, and statistics based on the selected period filter.
     */
    private void loadData() {
        try {
            List<AnalyticsEvent> events = analyticsService.getEventsByPeriod(currentPeriod);
            eventsList.clear();
            eventsList.addAll(events);

            // Update all charts and statistics
            updatePageViewsChart();
            updateDeviceChart();
            updateStatsLabel();
            updatePagesDetailChart();
            updateDevicesDetailChart();
            updateEventTypesCharts();
            updateProfileLabels();

            System.out.println("Data loaded: " + events.size() + " events for period " + currentPeriod);
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private void debugAuthService() {
        System.out.println("=== DEBUG AUTH SERVICE ===");
        System.out.println("AuthService from SceneManager: " + SceneManager.getAuthService());
        System.out.println("AuthService local: " + authService);

        if (authService != null) {
            System.out.println("Current user: " + authService.getCurrentUser());
            System.out.println("Is logged in: " + authService.isLoggedIn());
        } else {
            System.out.println("❌ AuthService est NULL !");
        }
        System.out.println("========================");
    }
    /**
     * Loads and displays current user profile information.
     * Updates profile labels with user name, role, and email from authentication service.
     */
    private void loadUserProfile() {
        debugAuthService();
        try {
            User currentUser = authService.getCurrentUser();
            System.out.println("USER USER USER USER USER USER " + currentUser);

            if (currentUser != null) {
                // Afficher le nom complet
                profileNameLabel.setText(currentUser.getFullName());

                // Afficher le rôle traduit
                profileRoleLabel.setText(translateRole(currentUser.getRole()));

                // Afficher l'email si le label existe
                if (profileEmailLabel != null) {
                    profileEmailLabel.setText(currentUser.getEmail());
                }

                System.out.println("✅ Profil chargé: " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
            } else {
                System.err.println("❌ Aucun utilisateur connecté trouvé");
                setDefaultProfileValues();
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement du profil: " + e.getMessage());
            setDefaultProfileValues();
        }
    }

    /**
     * Updates profile labels with user information.
     */
    private void updateProfileLabels() {
        try {
            // Récupérer les informations de l'utilisateur connecté
            String userName = authService.getCurrentUserName();
            String userRole = authService.getCurrentUserRole();
            User user = authService.getCurrentUser();
            System.out.println("USER  USER USER USER : " + user);

            // Mettre à jour les labels avec les vraies informations
            if (userName != null && !userName.isEmpty()) {
                profileNameLabel.setText(userName);
            } else {
                profileNameLabel.setText("Utilisateur");
            }

            if (userRole != null && !userRole.isEmpty()) {
                profileRoleLabel.setText(userRole);
            } else {
                profileRoleLabel.setText("Membre");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du profil: " + e.getMessage());
            // Valeurs par défaut en cas d'erreur
            profileNameLabel.setText("Utilisateur");
            profileRoleLabel.setText("Membre");
        }
    }

    /**
     * Sets default values for profile labels when user data is unavailable.
     */
    private void setDefaultProfileValues() {
        if (profileNameLabel != null) profileNameLabel.setText("Utilisateur");
        if (profileRoleLabel != null) profileRoleLabel.setText("Utilisateur");
    }

    /**
     * Translates role codes to user-friendly French labels.
     *
     * @param role The role code from the authentication system
     * @return Translated role name in French
     */
    private String translateRole(String role) {
        if (role == null) return "Utilisateur";

        return switch (role) {
            case "Admin" -> "Administrateur";
            case "Sales" -> "Commercial";
            case "User" -> "Utilisateur";
            // Support des anciens formats si nécessaire
            case "ROLE_ADMIN" -> "Administrateur";
            case "ROLE_SALES" -> "Commercial";
            case "ROLE_USER" -> "Utilisateur";
            default -> "Utilisateur";
        };
    }
    // ===== CHART UPDATE METHODS =====

    /**
     * Updates the page views bar chart with current statistics.
     * Limits display to top 10 most visited pages for readability.
     */
    private void updatePageViewsChart() {
        pageViewsChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Visites");

        Map<String, Long> pageStats = analyticsService.getPageStatistics();

        pageStats.entrySet().stream()
                .limit(10)
                .forEach(entry -> {
                    String shortUrl = entry.getKey().length() > 30 ?
                            entry.getKey().substring(0, 27) + "..." : entry.getKey();
                    series.getData().add(new XYChart.Data<>(shortUrl, entry.getValue()));
                });

        pageViewsChart.getData().add(series);
    }

    /**
     * Updates the device distribution pie chart with current device statistics.
     * Shows breakdown of visits by device type (desktop, mobile, tablet, etc.).
     */
    private void updateDeviceChart() {
        deviceChart.getData().clear();

        Map<String, Long> deviceStats = analyticsService.getDeviceTypeStats();

        deviceStats.forEach((device, count) -> {
            String label = device != null ? device : "Inconnu";
            deviceChart.getData().add(new PieChart.Data(label + " (" + count + ")", count));
        });
    }

    /**
     * Updates the detailed pages chart in the pages section.
     * Shows all pages without the 10-item limit of the main chart.
     */
    private void updatePagesDetailChart() {
        if (pagesDetailChart != null) {
            pagesDetailChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Visites détaillées");

            Map<String, Long> pageStats = analyticsService.getPageStatistics();

            pageStats.entrySet().forEach(entry -> {
                String shortUrl = entry.getKey().length() > 20 ?
                        entry.getKey().substring(0, 17) + "..." : entry.getKey();
                series.getData().add(new XYChart.Data<>(shortUrl, entry.getValue()));
            });

            pagesDetailChart.getData().add(series);
        }
    }

    /**
     * Updates the detailed devices pie chart in the devices section.
     * Mirrors the main device chart with section-specific styling.
     */
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

    /**
     * Updates both event type charts (pie and bar) in the events section.
     * Shows distribution and comparison of different event types.
     */
    private void updateEventTypesCharts() {
        Map<String, Long> eventStats = analyticsService.getEventTypeStats();

        // Update pie chart
        if (eventTypesChart != null) {
            eventTypesChart.getData().clear();
            eventStats.forEach((type, count) -> {
                String label = type != null ? type : "Inconnu";
                eventTypesChart.getData().add(new PieChart.Data(label + " (" + count + ")", count));
            });
        }

        // Update bar chart
        if (eventTypesBarChart != null) {
            eventTypesBarChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Événements par type");

            eventStats.forEach((type, count) -> {
                String label = type != null ? type : "Inconnu";
                series.getData().add(new XYChart.Data<>(label, count));
            });

            eventTypesBarChart.getData().add(series);
        }
    }

    // ===== STATISTICS UPDATE METHODS =====

    /**
     * Updates the main statistics label and total events counter.
     * Displays contextual information based on the current time period filter.
     */
    private void updateStatsLabel() {
        int totalEvents = eventsList.size();
        String periodText = switch (currentPeriod) {
            case TODAY -> "aujourd'hui";
            case WEEK -> "cette semaine";
            case MONTH -> "ce mois";
            case ALL -> "au total";
        };

        totalEventsLabel.setText(String.valueOf(totalEvents));

        if (statsLabel != null) {
            statsLabel.setText("📅 " + totalEvents + " événements " + periodText);
        }

        updateAdditionalStats();
    }

    /**
     * Updates additional statistics labels for unique pages and devices.
     * Provides quick overview metrics for dashboard sections.
     */
    private void updateAdditionalStats() {
        Map<String, Long> pageStats = analyticsService.getPageStatistics();
        uniquePagesLabel.setText(String.valueOf(pageStats.size()));

        Map<String, Long> deviceStats = analyticsService.getDeviceTypeStats();
        uniqueDevicesLabel.setText(String.valueOf(deviceStats.size()));
    }

    // ===== FILTER METHODS =====

    /**
     * Highlights the active filter button and resets others to default styling.
     * Provides visual feedback for the currently selected time period.
     *
     * @param activeButton The button to highlight as active
     */
    private void highlightActiveFilter(Button activeButton) {
        String defaultStyle = "-fx-background-color: #e0e0e0; -fx-text-fill: #333;";
        String activeStyle = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;";

        // Reset all filter buttons
        filterTodayBtn.setStyle(defaultStyle);
        filterWeekBtn.setStyle(defaultStyle);
        filterMonthBtn.setStyle(defaultStyle);
        filterAllBtn.setStyle(defaultStyle);

        // Highlight active button
        activeButton.setStyle(activeStyle);
    }

    /**
     * Filters data to show only today's events.
     */
    @FXML
    private void filterToday() {
        currentPeriod = AnalyticsService.Period.TODAY;
        highlightActiveFilter(filterTodayBtn);
        loadData();
    }

    /**
     * Filters data to show this week's events.
     */
    @FXML
    private void filterWeek() {
        currentPeriod = AnalyticsService.Period.WEEK;
        highlightActiveFilter(filterWeekBtn);
        loadData();
    }

    /**
     * Filters data to show this month's events.
     */
    @FXML
    private void filterMonth() {
        currentPeriod = AnalyticsService.Period.MONTH;
        highlightActiveFilter(filterMonthBtn);
        loadData();
    }

    /**
     * Shows all available data without time filtering.
     */
    @FXML
    private void filterAll() {
        currentPeriod = AnalyticsService.Period.ALL;
        highlightActiveFilter(filterAllBtn);
        loadData();
    }

    // ===== NAVIGATION METHODS =====

    /**
     * Shows the main dashboard section and hides all others.
     */
    @FXML
    private void showDashboard() {
        hideAllSections();
        dashboardSection.setVisible(true);
        dashboardSection.setManaged(true);
        updateActiveButton(dashboardBtn);
    }

    /**
     * Shows the pages analytics section.
     */
    @FXML
    private void showPages() {
        hideAllSections();
        pagesSection.setVisible(true);
        pagesSection.setManaged(true);
        updateActiveButton(pagesBtn);
        updatePagesDetailChart();
    }

    /**
     * Shows the devices analytics section.
     */
    @FXML
    private void showDevices() {
        hideAllSections();
        devicesSection.setVisible(true);
        devicesSection.setManaged(true);
        updateActiveButton(devicesBtn);
        updateDevicesDetailChart();
    }

    /**
     * Shows the events analytics section.
     */
    @FXML
    private void showEvents() {
        hideAllSections();
        eventsSection.setVisible(true);
        eventsSection.setManaged(true);
        updateActiveButton(eventsBtn);
        updateEventTypesCharts();
    }

    /**
     * Shows the reports section.
     */
    @FXML
    private void showReports() {
        hideAllSections();
        reportsSection.setVisible(true);
        reportsSection.setManaged(true);
        updateActiveButton(reportsBtn);
    }

    /**
     * Shows the settings section.
     */
    @FXML
    private void showSettings() {
        hideAllSections();
        updateActiveButton(settingsBtn);
    }

    /**
     * Hides all dashboard sections by setting visibility and management to false.
     */
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

    /**
     * Updates sidebar button styling to show the active section.
     *
     * @param activeButton The button representing the currently active section
     */
    private void updateActiveButton(Button activeButton) {
        // Remove active class from all buttons
        dashboardBtn.getStyleClass().remove("sidebar-menu-active");
        pagesBtn.getStyleClass().remove("sidebar-menu-active");
        devicesBtn.getStyleClass().remove("sidebar-menu-active");
        eventsBtn.getStyleClass().remove("sidebar-menu-active");
        reportsBtn.getStyleClass().remove("sidebar-menu-active");
        settingsBtn.getStyleClass().remove("sidebar-menu-active");

        // Add active class to selected button
        activeButton.getStyleClass().add("sidebar-menu-active");
    }

    // ===== ACTION METHODS =====

    /**
     * Refreshes all data and updates the interface.
     * Reloads current data set and refreshes all charts and statistics.
     */
    @FXML
    private void refreshData() {
        System.out.println("Refreshing data...");
        loadData();
        System.out.println("Data refreshed");
    }

    /**
     * Handles user logout functionality.
     * Closes analytics service connection and redirects to login screen.
     *
     * @param event The action event triggering the logout
     */
    @FXML
    private void onLogoutButtonClick(ActionEvent event) {
        if (analyticsService != null) {
            analyticsService.close();
        }
        SceneManager.clearAuthService();

        SceneManager.setScene(event, "/org/minotor/analytics/login-view.fxml", false);
    }

    /**
     * Exports current analytics data to a CSV file.
     * Opens file chooser dialog for user to select export location.
     */
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
                exportToCSVFile(file);
                System.out.println("Data exported to: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Export error: " + e.getMessage());
        }
    }

    /**
     * Writes analytics data to the specified CSV file.
     *
     * @param file The target file for CSV export
     * @throws Exception If file writing fails
     */
    private void exportToCSVFile(java.io.File file) throws Exception {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // CSV header
            writer.println("URL,Date,Appareil,Pays,Durée,Type");

            // Data rows
            for (AnalyticsEvent event : eventsList) {
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                        event.getUrl() != null ? event.getUrl() : "",
                        event.getFormattedDate(),
                        event.getDeviceType() != null ? event.getDeviceType() : "",
                        event.getEventType() != null ? event.getEventType() : ""
                );
            }
        }
    }

    /**
     * Toggles the visibility of the analytics table.
     * Updates button text to reflect current state (Show/Hide).
     */
    @FXML
    private void toggleTable() {
        boolean isVisible = analyticsTable.isVisible();
        analyticsTable.setVisible(!isVisible);
        analyticsTable.setManaged(!isVisible);

        toggleTableBtn.setText(isVisible ? "▼ Afficher" : "▲ Masquer");
    }
}