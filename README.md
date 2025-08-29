# MINOT'OR Analytics - Documentation

**MINOT'OR Analytics** est une application de bureau JavaFX développée pour analyser les données de trafic web avec MongoDB.

## Vue d'ensemble

L'application fonctionne comme un tableau de bord analytique professionnel permettant de :

- Visualiser le comportement des visiteurs
- Analyser les pages visitées et types d'appareils
- Générer des rapports détaillés
- Exporter les données au format CSV    

## Architecture Technique

### Technologies utilisées

- **JavaFX 17** - Interface utilisateur
- **MongoDB 4.11.1** - Base de données
- **Jackson** - Sérialisation JSON
- **Maven** - Système de build
- **Java 23** - Langage de programmation

### Pattern MVC

Le projet suit une architecture MVC avec :

- **Model** : Classes de données (`AnalyticsEvent`)
- **View** : Fichiers FXML avec charte graphique
- **Controller** : Logique métier et interactions
- **Service** : Accès données et authentification