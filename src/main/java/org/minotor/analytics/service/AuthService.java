package org.minotor.analytics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthService {
    private static final String BASE_URL = "http://localhost:8000"; // Ajustez selon votre API
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String currentToken;
    private JsonNode currentUser; // Stocker les données utilisateur

    // Ajouter une méthode pour récupérer les données utilisateur
    public JsonNode getCurrentUser() {
        return currentUser;
    }

    public AuthService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

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

            System.out.println("🌐 Appel API: " + BASE_URL + "/api/login");
            System.out.println("📤 Données envoyées: " + requestBody);

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("📡 Status Code: " + response.statusCode());
            System.out.println("📡 Response Body: " + response.body());

            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                currentToken = jsonResponse.get("token").asText();

                // Récupérer les données utilisateur si elles sont dans la réponse
                if (jsonResponse.has("user")) {
                    currentUser = jsonResponse.get("user");
                    System.out.println("✅ Données utilisateur récupérées depuis login");
                } else {
                    // Sinon, récupérer les données par un appel séparé
                    fetchUserData();
                }

                String role = extractRole(jsonResponse);

                if (!"ROLE_SALES".equals(role) && !"ROLE_ADMIN".equals(role)) {
                    return new AuthResult(false, "Droits insuffisants - Accès réservé aux commerciaux", null);
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
            System.err.println("❌ Connexion refusée: " + e.getMessage());
            return new AuthResult(false, "Serveur non disponible sur le port 8000", null);
        } catch (IOException | InterruptedException e) {
            System.err.println("❌ Erreur réseau: " + e.getMessage());
            return new AuthResult(false, "Impossible de contacter le serveur. Vérifiez votre connexion.", null);
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
            return new AuthResult(false, "Erreur de connexion: " + e.getMessage(), null);
        }
    }
    private void fetchUserData() {
        try {
            // Récupérer la liste des utilisateurs ou utiliser un endpoint spécifique
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/users"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + currentToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode usersArray = objectMapper.readTree(response.body());
                if (usersArray.isArray() && usersArray.size() > 0) {
                    // Prendre le premier utilisateur ou chercher par email
                    currentUser = usersArray.get(0);
                    System.out.println("✅ Données utilisateur récupérées");
                }
            } else {
                System.err.println("❌ Erreur récupération utilisateur: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur fetchUserData: " + e.getMessage());
        }
    }

    private void getUserByEmail(String email) {
        try {
            // Si votre API a un endpoint pour récupérer par email
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/user/profile")) // Adaptez l'endpoint
                    .header("Authorization", "Bearer " + currentToken)
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                currentUser = objectMapper.readTree(response.body());
                System.out.println("✅ Données utilisateur récupérées");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur API utilisateur: " + e.getMessage());
        }
    }
    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final String token;

        public AuthResult(boolean success, String message, String token) {
            this.success = success;
            this.message = message;
            this.token = token;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getToken() { return token; }
    }
    private String extractRole(JsonNode jsonResponse) {
        String role = "ROLE_SALES"; // Par défaut

        if (jsonResponse.has("roles") && jsonResponse.get("roles").isArray()) {
            JsonNode rolesArray = jsonResponse.get("roles");
            if (!rolesArray.isEmpty()) {
                role = rolesArray.get(0).asText();
            }
        } else if (jsonResponse.has("role")) {
            role = jsonResponse.get("role").asText();
        } else if (currentUser != null && currentUser.has("role")) {
            role = currentUser.get("role").asText();
        }

        return role;
    }
}