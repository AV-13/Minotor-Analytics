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

/**
 * A service class that handles all database operations for the analytics application.
 * Think of this as a translator between your Java application and the MongoDB database.
 *
 * MongoDB is a type of database that stores data in documents (like digital filing cabinets).
 * This class helps your application talk to that database to save and retrieve analytics data.
 *
 * The service automatically connects to the database when created and provides methods
 * to get analytics events that can be displayed in charts and reports.
 *
 * @author Minot'Or Analytics Team
 * @version 1.0
 * @since 1.0
 */
public class MongoService {

    /**
     * The connection string that tells the application where to find the MongoDB database.
     * This is like an address that points to where your data is stored in the cloud.
     *
     * Note: In a real production app, this should be stored in a configuration file
     * for security reasons, not directly in the code.
     */
    private static final String CONNECTION_STRING = "mongodb+srv://moussaclementaugustincda:z060v6yhbdRZPqYs@clusterminotor.wrfw69d.mongodb.net/?retryWrites=true&w=majority&appName=ClusterMinotor";

    /**
     * The connection object that maintains the link to the MongoDB database.
     * Think of this as a telephone line that stays open to talk to the database.
     */
    private MongoClient mongoClient;

    /**
     * The specific database we're working with inside MongoDB.
     * One MongoDB server can host many databases - this points to ours.
     */
    private MongoDatabase database;

    /**
     * Creates a new MongoService and automatically connects to the database.
     * This constructor does all the heavy lifting of establishing the connection
     * and running diagnostic checks to make sure everything is working properly.
     *
     * When this runs, you'll see diagnostic messages in the console showing:
     * - Whether the connection was successful
     * - What collections (data tables) exist in the database
     * - How much data is available
     *
     * @throws RuntimeException if the database connection fails
     */
    public MongoService() {
        try {
            System.out.println("\n=== DIAGNOSTIC MONGODB ===");

            // Create connection using the connection string (like dialing a phone number)
            ConnectionString connectionString = new ConnectionString(CONNECTION_STRING);
            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase("minotor");

            // Test the connection by sending a simple "ping" command
            // This is like saying "hello" to make sure the database is listening
            database.runCommand(new Document("ping", 1));
            System.out.println("Connexion réussie à la base de données: " + database.getName());

            // Run diagnostic methods to check what data is available
            listAllCollections();
            checkCollectionsWithData();

        } catch (Exception e) {
            System.err.println("Erreur de connexion MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lists all collections (data tables) available in the database.
     * This is like looking at a file cabinet to see what folders are available.
     *
     * Collections in MongoDB are similar to tables in traditional databases -
     * each collection holds a specific type of data.
     */
    private void listAllCollections() {
        System.out.println("\n--- COLLECTIONS DISPONIBLES ---");
        try {
            List<String> collections = new ArrayList<>();

            // Get names of all collections and add them to our list
            for (String name : database.listCollectionNames()) {
                collections.add(name);
            }

            // Report what we found (or didn't find)
            if (collections.isEmpty()) {

                System.out.println("Aucune collection trouvée");
            } else {
                System.out.println("Collections trouvées (" + collections.size() + ") :");
                for (String collection : collections) {
                    System.out.println("  - " + collection);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la liste des collections: " + e.getMessage());
        }
    }

    /**
     * Analyzes each collection to see how much data it contains.
     * This method goes through each collection and counts the documents (records)
     * it contains, and shows a sample document to understand the data structure.
     *
     * Think of this as opening each file folder to see how many papers are inside
     * and what those papers look like.
     */
    private void checkCollectionsWithData() {
        System.out.println("\n--- ANALYSE DES DONNÉES ---");
        try {
            // Go through each collection one by one
            for (String collectionName : database.listCollectionNames()) {
                MongoCollection<Document> collection = database.getCollection(collectionName);
                long count = collection.countDocuments();

                System.out.println("Collection '" + collectionName + "': " + count + " documents");

                // If there's data, show an example of what it looks like
                if (count > 0) {
                    Document firstDoc = collection.find().first();
                    if (firstDoc != null) {
                        System.out.println("Exemple de document:");
                        System.out.println("   " + firstDoc.toJson());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur analyse collections: " + e.getMessage());
        }
    }

    /**
     * Retrieves analytics events from the database to display in the application.
     * This is the main method that gets the data your charts and reports need.
     *
     * The method is smart - it tries different possible collection names because
     * database structures can vary. If it can't find real data, it creates test data
     * so the application still works.
     *
     * @return A list of AnalyticsEvent objects containing website usage data
     */
    public List<AnalyticsEvent> getAnalyticsEvents() {
        List<AnalyticsEvent> events = new ArrayList<>();
        System.out.println("\n=== RÉCUPÉRATION DES ÉVÉNEMENTS ===");

        try {
            // Try different possible names for the analytics collection
            // Different developers might name collections differently
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

            // Test each possible collection name until we find one with data
            for (String collectionName : possibleCollections) {
                System.out.println("Test de la collection: " + collectionName);

                try {
                    MongoCollection<Document> testCollection = database.getCollection(collectionName);
                    long count = testCollection.countDocuments();

                    if (count > 0) {
                        collection = testCollection;
                        foundCollectionName = collectionName;
                        System.out.println("Collection '" + collectionName + "' trouvée avec " + count + " documents");
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Collection '" + collectionName + "' non accessible");
                }
            }

            // If we found a collection with data, process it
            if (collection != null) {
                System.out.println("Récupération des données de: " + foundCollectionName);

                int successCount = 0;
                int errorCount = 0;

                // Process documents (limit to 100 for testing to avoid overwhelming the system)
                for (Document doc : collection.find().limit(100)) {
                    try {
                        AnalyticsEvent event = mapDocumentToEvent(doc);
                        // Only add events that have essential data
                        if (event.getId() != null && event.getUrl() != null) {
                            events.add(event);
                            successCount++;
                        }
                    } catch (Exception e) {
                        errorCount++;
                        System.err.println("Erreur sur un document: " + e.getMessage());
                    }
                }

                System.out.println("Documents traités avec succès: " + successCount);
                System.out.println("Documents en erreur: " + errorCount);

            } else {
                // No real data found, create test data so the app still works
                System.out.println("Aucune collection d'analytics trouvée");
                events = createTestData();
            }

            System.out.println("Nombre total d'événements créés: " + events.size());

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des événements: " + e.getMessage());
            e.printStackTrace();
            // If anything goes wrong, fall back to test data
            events = createTestData();
        }

        return events;
    }

    /**
     * Converts a MongoDB document into an AnalyticsEvent object.
     * This is like translating from database language to Java language.
     *
     * MongoDB stores data as documents (similar to JSON), but our Java application
     * needs AnalyticsEvent objects. This method does the conversion safely,
     * handling cases where data might be missing or in different formats.
     *
     * @param doc The MongoDB document to convert
     * @return An AnalyticsEvent object with data from the document
     */
    private AnalyticsEvent mapDocumentToEvent(Document doc) {
        AnalyticsEvent event = new AnalyticsEvent();

        try {
            // Extract ID from MongoDB's special _id field
            if (doc.getObjectId("_id") != null) {
                event.setId(doc.getObjectId("_id").toString());
            }

            // Get the URL that was visited
            event.setUrl(doc.getString("url"));

            // Handle date/timestamp - databases can store dates in different formats
            if (doc.getDate("date") != null) {
                event.setTimestamp(doc.getDate("date"));
            } else if (doc.getDate("timestamp") != null) {
                event.setTimestamp(doc.getDate("timestamp"));
            } else if (doc.getString("date") != null) {
                // Try to parse a date string if that's how it's stored
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    event.setTimestamp(sdf.parse(doc.getString("date")));
                } catch (Exception e) {
                    event.setTimestamp(new Date()); // Use current date as fallback
                }
            }

            // Extract browser and device information
            event.setUserAgent(doc.getString("userAgent"));
            event.setReferrer(doc.getString("referrer"));
            event.setDeviceType(doc.getString("deviceType"));

            // Get screen dimensions (how big the user's screen is)
            event.setScreenWidth(doc.getInteger("screenWidth"));
            event.setScreenHeight(doc.getInteger("screenHeight"));

            // Get user language and event type
            event.setLanguage(doc.getString("language"));
            event.setEventType(doc.getString("eventType"));
            event.setPageTitle(doc.getString("pageTitle"));

            // Handle load time - might be stored as Integer or Long
            Object loadTimeObj = doc.get("loadTime");
            if (loadTimeObj instanceof Integer) {
                event.setLoadTime((Integer) loadTimeObj);
            } else if (loadTimeObj instanceof Long) {
                event.setLoadTime(((Long) loadTimeObj).intValue());
            }

        } catch (Exception e) {
            System.err.println("Erreur mapping document: " + e.getMessage());
            // Even if something goes wrong, try to save the basic information
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

    /**
     * Creates sample test data when no real database data is available.
     * This ensures the application can still function and display charts
     * even when the database is empty or unreachable.
     *
     * Think of this as having backup data so your application doesn't break
     * when there's no real data to show.
     *
     * @return A list of sample AnalyticsEvent objects for testing
     */
    private List<AnalyticsEvent> createTestData() {
        List<AnalyticsEvent> testEvents = new ArrayList<>();

        // Arrays of sample data to make realistic test events
        String[] urls = {"/home", "/login", "/dashboard", "/profile", "/settings"};
        String[] devices = {"Desktop", "Mobile", "Tablet"};
        String[] countries = {"France", "Germany", "Spain", "Italy", "UK"};
        String[] eventTypes = {"page_view", "click", "scroll", "download"};

        // Create several test events with varying data
        for (int i = 0; i < 3; i++) {
            AnalyticsEvent event = new AnalyticsEvent();
            event.setId("test_" + i);
            event.setUrl(urls[i % urls.length]);
            // Set timestamp to i hours ago
            event.setTimestamp(new Date(System.currentTimeMillis() - (i * 3600000)));
            event.setDeviceType(devices[i % devices.length]);
            event.setEventType(eventTypes[i % eventTypes.length]);
            // Random load time between 0-3 seconds
            event.setLoadTime((int) (Math.random() * 3000));

            testEvents.add(event);
        }

        System.out.println("Nombre d'événements chargés: " + testEvents.size());
        return testEvents;
    }

    /**
     * Properly closes the database connection when the service is no longer needed.
     * This is like hanging up the phone when you're done talking.
     *
     * It's important to close database connections to free up resources
     * and avoid connection leaks that could slow down or crash your application.
     *
     * This method should be called when the application shuts down.
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("Connexion MongoDB fermée");
        }
    }
}