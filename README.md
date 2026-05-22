# Système de Gestion de Forage

## Description
Application web de gestion des demandes de forage permettant de suivre les clients, les demandes, les devis et l'historique des statuts. Elle calcule automatiquement les durées estimées et réelles entre chaque changement de statut d'une demande.

## Contexte
- Projet réalisé dans le cadre de ma formation en développement Java
- Sujet imposé : Développement d'un système de gestion pour une entreprise de forage
- Travail en individuel

## Ce que j'ai fait
- Conception et création de la base de données PostgreSQL avec le schéma complet
- Développement du back-end avec Spring Boot (MVC, JPA, Hibernate) et des interfaces Thymeleaf/Bootstrap
- Implémentation du calcul automatique des durées estimées (heures brutes) et réelles (heures ouvrables, hors week-ends et jours fériés malgaches)

## Technologies utilisées
| Catégorie | Technologies |
|-----------|--------------|
| Front-end | HTML, CSS, Bootstrap 5, Bootstrap Icons, Thymeleaf |
| Back-end | Java 17, Spring Boot 3.2, Spring MVC, Spring Data JPA, Hibernate |
| Base de données | PostgreSQL 16 |
| Build | Maven |

## Fonctionnalités
- Gestion des clients, demandes et devis (CRUD complet)
- Suivi de l'historique des statuts pour chaque demande
- Génération automatique des devis avec tableau éditable et calcul des totaux
- Système d'autocomplete pour la recherche des demandes
- Récapitulatif dynamique en temps réel dans le formulaire devis
- Date reel entre les changemenet de tatus et date Total qui sont approximatif
- Indicateur de Temps de statuts par niveau(critique, elever, normal)

## Installation
```bash
# 1. Cloner le projet
git clone https://github.com/maminiainafita123-art/Gestion-Forage-de-puits-d-eau.git
cd forage

# 2. Créer la base de données PostgreSQL
psql -U postgres -c "CREATE DATABASE forage;"

# 3. Importer le schéma et les données
psql -U postgres -d forage -f src/main/resources/sql/01_schema.sql
psql -U postgres -d forage -f src/main/resources/sql/02_data.sql

# 4. Maven installation
mvn clean install
or
mvn clean package

# 5. Lancer l'application
mvn spring-boot:run

# Accès : http://localhost:8086