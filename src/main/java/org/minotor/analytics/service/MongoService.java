package org.minotor.analytics.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.minotor.analytics.model.AnalyticsEvent;
import com.mongodb.ConnectionString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoService {
    private static final String CONNECTION_STRING = "mongodb+srv://moussaclementaugustincda:z060v6yhbdRZPqYs@clusterminotor.wrfw69d.mongodb.net/?retryWrites=true&w=majority&appName=ClusterMinotor";
    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoService() {
        try {
            System.out.println("\n=== DIAGNOSTIC MONGODB ===");
            ConnectionString connectionString = new ConnectionString(CONNECTION_STRING);
            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase("minotor");

            // Test de connexion
            database.runCommand(new Document("ping", 1));
            System.out.println("Connexion réussie à la base de données: " + database.getName());

            listAllCollections();
            checkCollectionsWithData();

        } catch (Exception e) {
            System.err.println("Erreur de connexion MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void listAllCollections() {
        System.out.println("\n--- COLLECTIONS DISPONIBLES ---");
        try {
            List<String> collections = new ArrayList<>();
            for (String name : database.listCollectionNames()) {
                collections.add(name);
            }

            if (collections.isEmpty()) {
                System.out.println("❌ Aucune collection trouvée");
            } else {
                System.out.println("✅ Collections trouvées (" + collections.size() + ") :");
                for (String collection : collections) {
                    System.out.println("  - " + collection);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la liste des collections: " + e.getMessage());
        }
    }

    private void checkCollectionsWithData() {
        System.out.println("\n--- ANALYSE DES DONNÉES ---");
        try {
            for (String collectionName : database.listCollectionNames()) {
                MongoCollection<Document> collection = database.getCollection(collectionName);
                long count = collection.countDocuments();

                System.out.println("📊 Collection '" + collectionName + "': " + count + " documents");

                if (count > 0) {
                    // Afficher un exemple de document
                    Document firstDoc = collection.find().first();
                    if (firstDoc != null) {
                        System.out.println("   📄 Exemple de document:");
                        System.out.println("   " + firstDoc.toJson());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur analyse collections: " + e.getMessage());
        }
    }

    public List<AnalyticsEvent> getAnalyticsEvents() {
        List<AnalyticsEvent> events = new ArrayList<>();
        System.out.println("\n=== RÉCUPÉRATION DES ÉVÉNEMENTS ===");

        try {
            String[] possibleCollections = {
                    "AnalyticsEvent",
                    "AnalyticsEvents",
                    "analytics_events",
                    "events",
                    "analytics",
                    "user_events"
            };

            MongoCollection<Document> collection = null;
            String foundCollectionName = null;

            for (String collectionName : possibleCollections) {
                System.out.println("🔍 Test de la collection: " + collectionName);

                try {
                    MongoCollection<Document> testCollection = database.getCollection(collectionName);
                    long count = testCollection.countDocuments();

                    if (count > 0) {
                        collection = testCollection;
                        foundCollectionName = collectionName;
                        System.out.println("✅ Collection '" + collectionName + "' trouvée avec " + count + " documents");
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Collection '" + collectionName + "' non accessible");
                }
            }

            if (collection != null) {
                System.out.println("📈 Récupération des données de: " + foundCollectionName);

                int successCount = 0;
                int errorCount = 0;

                for (Document doc : collection.find().limit(100)) { // Limiter pour les tests
                    try {
                        AnalyticsEvent event = mapDocumentToEvent(doc);
                        if (event.getId() != null && event.getUrl() != null) {
                            events.add(event);
                            successCount++;
                        }
                    } catch (Exception e) {
                        errorCount++;
                        System.err.println("Erreur sur un document: " + e.getMessage());
                    }
                }

                System.out.println("✅ Documents traités avec succès: " + successCount);
                System.out.println("❌ Documents en erreur: " + errorCount);

            } else {
                System.out.println("❌ Aucune collection d'analytics trouvée");
                events = createTestData();
            }

            System.out.println("📈 Nombre total d'événements créés: " + events.size());

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des événements: " + e.getMessage());
            e.printStackTrace();
            events = createTestData();
        }

        return events;
    }

    private AnalyticsEvent mapDocumentToEvent(Document doc) {
        AnalyticsEvent event = new AnalyticsEvent();

        try {
            // ID
            if (doc.getObjectId("_id") != null) {
                event.setId(doc.getObjectId("_id").toString());
            }

            // URL
            event.setUrl(doc.getString("url"));

            // Date/Timestamp - Gérer les différents formats
            if (doc.getDate("date") != null) {
                event.setTimestamp(doc.getDate("date"));
            } else if (doc.getDate("timestamp") != null) {
                event.setTimestamp(doc.getDate("timestamp"));
            } else if (doc.getString("date") != null) {
                try {
                    // Parser une chaîne de date si nécessaire
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    event.setTimestamp(sdf.parse(doc.getString("date")));
                } catch (Exception e) {
                    event.setTimestamp(new Date()); // Date par défaut
                }
            }

            // Nouveaux champs avec gestion sécurisée des types
            event.setUserAgent(doc.getString("userAgent"));
            event.setReferrer(doc.getString("referrer"));

            event.setDeviceType(doc.getString("deviceType"));

            // ScreenWidth et ScreenHeight - déjà Integer, pas de problème
            event.setScreenWidth(doc.getInteger("screenWidth"));
            event.setScreenHeight(doc.getInteger("screenHeight"));

            event.setLanguage(doc.getString("language"));
            event.setEventType(doc.getString("eventType"));
            event.setPageTitle(doc.getString("pageTitle"));

            // LoadTime - gérer Integer/Long si nécessaire
            Object loadTimeObj = doc.get("loadTime");
            if (loadTimeObj instanceof Integer) {
                event.setLoadTime((Integer) loadTimeObj);
            } else if (loadTimeObj instanceof Long) {
                event.setLoadTime(((Long) loadTimeObj).intValue());
            }

        } catch (Exception e) {
            System.err.println("Erreur mapping document: " + e.getMessage());
            // Retourner un événement avec au moins les données de base
            if (event.getId() == null && doc.getObjectId("_id") != null) {
                event.setId(doc.getObjectId("_id").toString());
            }
            if (event.getUrl() == null) {
                event.setUrl(doc.getString("url"));
            }
            if (event.getTimestamp() == null) {
                event.setTimestamp(new Date());
            }
        }

        return event;
    }

    private List<AnalyticsEvent> createTestData() {
        List<AnalyticsEvent> testEvents = new ArrayList<>();

        // Données de test plus réalistes
        String[] urls = {"/home", "/login", "/dashboard", "/profile", "/settings"};
        String[] devices = {"Desktop", "Mobile", "Tablet"};
        String[] countries = {"France", "Germany", "Spain", "Italy", "UK"};
        String[] eventTypes = {"page_view", "click", "scroll", "download"};

        for (int i = 0; i < 3; i++) {
            AnalyticsEvent event = new AnalyticsEvent();
            event.setId("test_" + i);
            event.setUrl(urls[i % urls.length]);
            event.setTimestamp(new Date(System.currentTimeMillis() - (i * 3600000))); // Il y a i heures
            event.setDeviceType(devices[i % devices.length]);
            event.setEventType(eventTypes[i % eventTypes.length]);
            event.setLoadTime((int) (Math.random() * 3000)); // 0-3 secondes

            testEvents.add(event);
        }

        System.out.println("Nombre d'événements chargés: " + testEvents.size());
        return testEvents;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("Connexion MongoDB fermée");
        }
    }
}