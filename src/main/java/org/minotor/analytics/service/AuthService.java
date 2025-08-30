package org.minotor.analytics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.minotor.analytics.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * A service class that handles user authentication for the analytics application.
 * Think of this as a security guard that checks if users are allowed to enter the app.
 *
 * This service communicates with a Symfony API server to verify user credentials
 * and manages login sessions. It also checks if users have the right permissions
 * to access the analytics dashboard (only sales and admin users are allowed).
 *
 * The service stores the authentication token and user information for the current session,
 * making it easy to identify the logged-in user throughout the application.
 *
 * @author Minot'Or Analytics Team
 * @version 1.0
 * @since 1.0
 */
public class AuthService {

    /**
     * The base URL of the authentication API server.
     * This is like the main address where we send login requests.
     *
     * Note: In production, this should be configurable and not hardcoded.
     */
    private static final String BASE_URL = "http://localhost:8000";

    /**
     * The HTTP client used to make network requests to the API.
     * Think of this as a postal service that delivers messages between our app and the server.
     */
    private final HttpClient httpClient;

    /**
     * A tool that converts JSON data (text format) into Java objects and vice versa.
     * This is like a translator that helps our Java app understand the server's responses.
     */
    private final ObjectMapper objectMapper;

    /**
     * The authentication token received after successful login.
     * This is like a temporary pass that proves the user is authenticated.
     * The server gives us this token and we include it in future requests.
     */
    private String currentToken;

    /**
     * Information about the currently logged-in user (name, email, role, etc.).
     * This is like an ID card that contains the user's details.
     */
    private User currentUser;


    /**
     * Creates a new AuthService and sets up the HTTP client for making API calls.
     * This constructor prepares all the tools needed to communicate with the authentication server.
     *
     * The HTTP client is configured with a reasonable timeout to avoid waiting forever
     * if the server doesn't respond quickly.
     */
    public AuthService() {
        // Set up HTTP client with a 10-second connection timeout
        // This prevents the app from hanging if the server is unreachable
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Set up JSON parser for handling server responses
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Attempts to log in a user with their email and password.
     * This is the main method that checks if someone is allowed to use the application.
     *
     * The method does several important things:
     * 1. Sends the login credentials to the API server
     * 2. Checks if the user has permission to access the analytics dashboard
     * 3. Stores the user's information and authentication token
     * 4. Returns a result indicating success or failure with a helpful message
     *
     * Only users with "ROLE_SALES" or "ROLE_ADMIN" roles are allowed to access the app.
     *
     * @param email The user's email address (used as username)
     * @param password The user's password
     * @return An AuthResult object containing success status, message, and token (if successful)
     */
    public AuthResult login(String email, String password) {
        try {
            String requestBody = String.format(
                    "{\"email\":\"%s\",\"password\":\"%s\"}",
                    email, password
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/login"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            System.out.println("Appel API: " + BASE_URL + "/api/login");
            System.out.println("Données envoyées: " + requestBody);

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 200) {
                // Parse la réponse pour récupérer le token
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                currentToken = jsonResponse.get("token").asText();

                // IMPORTANT: Récupérer les données utilisateur AVANT de vérifier le rôle
                if (!fetchUserData()) {
                    return new AuthResult(false, "Impossible de récupérer les données utilisateur", null);
                }

                // MAINTENANT on peut vérifier le rôle
                if (!"Sales".equals(currentUser.getRole()) && !"Admin".equals(currentUser.getRole())) {
                    // Nettoyer les données si l'utilisateur n'a pas les droits
                    currentToken = null;
                    currentUser = null;
                    return new AuthResult(false, "Droits insuffisants - Accès réservé aux commerciaux et administrateurs", null);
                }

                return new AuthResult(true, "Connexion réussie", currentToken);

            } else if (response.statusCode() == 401) {
                return new AuthResult(false, "Email ou mot de passe incorrect", null);
            } else if (response.statusCode() == 404) {
                return new AuthResult(false, "Endpoint non trouvé - Vérifiez votre API Symfony", null);
            } else {
                return new AuthResult(false, "Erreur de connexion (" + response.statusCode() + ")", null);
            }

        } catch (java.net.ConnectException e) {
            System.err.println("Connexion refusée: " + e.getMessage());
            return new AuthResult(false, "Serveur non disponible sur le port 8000", null);
        } catch (IOException | InterruptedException e) {
            System.err.println("Erreur réseau: " + e.getMessage());
            return new AuthResult(false, "Impossible de contacter le serveur. Vérifiez votre connexion.", null);
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return new AuthResult(false, "Erreur de connexion: " + e.getMessage(), null);
        }
    }

    /**
     * Fetches user information from the API server using the authentication token.
     * This method is called when the login response doesn't include user details.
     *
     * Think of this as asking for your ID card after you've already been let through security.
     * We use the authentication token to prove we're allowed to get this information.
     */
    private boolean fetchUserData() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users/me"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + currentToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("Données utilisateur récupérées " + response.body());

                currentUser = objectMapper.readValue(response.body(), User.class);
                System.out.println("Utilisateur connecté: " + currentUser);
                return true;
            } else {
                System.err.println("Erreur récupération utilisateur: " + response.statusCode());
                return false;
            }
        } catch (Exception e) {
            System.err.println("Erreur fetchUserData: " + e.getMessage());
            return false;
        }
    }

    // Modifier les méthodes publiques
    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getFullName() : "Invité";
    }

    public String getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : "Aucun rôle";
    }
    public void logout() {
        currentToken = null;
        currentUser = null;
        System.out.println("Utilisateur déconnecté");
    }
    public boolean isLoggedIn() {
        return currentToken != null && currentUser != null;
    }
    /**
     * A simple container class that holds the result of a login attempt.
     * This is like a report card that tells you whether the login worked or not,
     * and if it didn't work, what went wrong.
     *
     * This class is immutable (once created, its values can't be changed),
     * which makes it safe to pass around the application.
     */
    public static class AuthResult {
        /**
         * Whether the login attempt was successful.
         */
        private final boolean success;

        /**
         * A human-readable message explaining what happened.
         * This can be shown to the user to help them understand the result.
         */
        private final String message;

        /**
         * The authentication token (only set if login was successful).
         * This token is used for subsequent API calls to prove the user is authenticated.
         */
        private final String token;

        /**
         * Creates a new AuthResult with the specified values.
         *
         * @param success Whether the authentication was successful
         * @param message A message explaining the result
         * @param token The authentication token (can be null if login failed)
         */
        public AuthResult(boolean success, String message, String token) {
            this.success = success;
            this.message = message;
            this.token = token;
        }

        /**
         * Returns whether the authentication was successful.
         *
         * @return true if login succeeded, false otherwise
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Returns the message explaining the authentication result.
         *
         * @return A human-readable message about what happened
         */
        public String getMessage() {
            return message;
        }
    }
}