# Guide: Ajouter une entité Promotion au Projet Forage

Ce document explique comment ajouter une nouvelle entité **Promotion** au projet Spring Boot existant. Nous allons suivre l'architecture actuelle en couches et créer tous les fichiers nécessaires.

## Vue d'ensemble de Promotion

- **Entité**: Promotion (codes promotionnels pour les clients)
- **Propriétés**: id, code, description, pourcentageReduction, montantMax, dateDebut, dateFin, active
- **Relations**: Liée aux Devis (un devis peut utiliser une promotion)

---

## Étape 1: Créer l'entité JPA (Entity)

**Fichier**: `src/main/java/com/forage/entity/Promotion.java`

```java
package com.forage.entity;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promotions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Le code de promotion est requis")
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @NotBlank(message = "La description est requise")
    @Column(length = 500)
    private String description;

    @NotNull(message = "Le pourcentage de réduction est requis")
    @Min(value = 1, message = "La réduction doit être au minimum 1%")
    @Max(value = 100, message = "La réduction ne doit pas dépasser 100%")
    @Column(nullable = false)
    private Double pourcentageReduction;

    @NotNull(message = "Le montant maximum requis")
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false)
    private Double montantMax;

    @NotNull(message = "La date de début est requise")
    @Column(nullable = false)
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est requise")
    @Column(nullable = false)
    private LocalDate dateFin;

    @Column(nullable = false)
    private Boolean active = true;

    // Relation: Une promotion peut être utilisée par plusieurs devis
    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Devis> devis = new ArrayList<>();

    // Constructeur personnalisé
    public Promotion(String code, String description, Double pourcentageReduction, 
                     Double montantMax, LocalDate dateDebut, LocalDate dateFin) {
        this.code = code;
        this.description = description;
        this.pourcentageReduction = pourcentageReduction;
        this.montantMax = montantMax;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.active = true;
    }

    // Méthode métier: vérifier si promotion est valide
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return active && 
               today.isAfter(dateDebut.minusDays(1)) && 
               today.isBefore(dateFin.plusDays(1));
    }

    // Méthode métier: calculer la réduction en montant
    public Double calculateDiscount(Double montant) {
        if (!isValid()) return 0.0;
        
        Double discount = (montant * pourcentageReduction) / 100;
        return Math.min(discount, montantMax);
    }
}
```

---

## Étape 2: Créer le Repository

**Fichier**: `src/main/java/com/forage/repository/PromotionRepository.java`

```java
package com.forage.repository;

import com.forage.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer> {
    
    // Trouver une promotion par son code
    Optional<Promotion> findByCode(String code);
    
    // Chercher par code contenant (case-insensitive)
    List<Promotion> findByCodeContainingIgnoreCase(String code);
    
    // Trouver toutes les promotions actives
    List<Promotion> findByActiveTrue();
    
    // Trouver les promotions valides (actives et dans la date)
    @Query("SELECT p FROM Promotion p WHERE p.active = true " +
           "AND p.dateDebut <= CURRENT_DATE " +
           "AND p.dateFin >= CURRENT_DATE")
    List<Promotion> findValidPromotions();
    
    // Compter les promotions actives
    long countByActiveTrue();
    
    // Trouver par date de fin
    List<Promotion> findByDateFinBefore(LocalDate date);
}
```

---

## Étape 3: Créer le Service

**Fichier**: `src/main/java/com/forage/service/PromotionService.java`

```java
package com.forage.service;

import com.forage.entity.Promotion;
import com.forage.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PromotionService {
    
    private final PromotionRepository promotionRepository;
    
    // Récupérer toutes les promotions
    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }
    
    // Récupérer une promotion par ID
    public Promotion findById(int id) {
        return promotionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Promotion non trouvée: " + id));
    }
    
    // Récupérer une promotion par code
    public Promotion findByCode(String code) {
        return promotionRepository.findByCode(code)
            .orElseThrow(() -> new RuntimeException("Code promotion invalide: " + code));
    }
    
    // Enregistrer une promotion
    public Promotion save(Promotion promotion) {
        validatePromotion(promotion);
        return promotionRepository.save(promotion);
    }
    
    // Supprimer une promotion
    public void deleteById(int id) {
        if (!promotionRepository.existsById(id)) {
            throw new RuntimeException("Promotion non trouvée: " + id);
        }
        promotionRepository.deleteById(id);
    }
    
    // Rechercher par code
    public List<Promotion> search(String code) {
        return promotionRepository.findByCodeContainingIgnoreCase(code);
    }
    
    // Récupérer toutes les promotions actives
    public List<Promotion> findActivePromotions() {
        return promotionRepository.findByActiveTrue();
    }
    
    // Récupérer les promotions valides (actives et en date)
    public List<Promotion> findValidPromotions() {
        return promotionRepository.findValidPromotions();
    }
    
    // Activer/Désactiver une promotion
    public Promotion toggleActive(int id, Boolean active) {
        Promotion promotion = findById(id);
        promotion.setActive(active);
        return promotionRepository.save(promotion);
    }
    
    // Valider les données de la promotion
    private void validatePromotion(Promotion promotion) {
        if (promotion.getDateFin().isBefore(promotion.getDateDebut())) {
            throw new IllegalArgumentException("La date de fin doit être après la date de début");
        }
        
        if (promotion.getPourcentageReduction() < 1 || promotion.getPourcentageReduction() > 100) {
            throw new IllegalArgumentException("Le pourcentage doit être entre 1 et 100");
        }
    }
    
    // Calculer la réduction pour un montant
    public Double calculateDiscount(int promotionId, Double montant) {
        Promotion promotion = findById(promotionId);
        return promotion.calculateDiscount(montant);
    }
    
    // Compter les promotions actives
    public long countActivePromotions() {
        return promotionRepository.countByActiveTrue();
    }
}
```

---

## Étape 4: Créer le Controller

**Fichier**: `src/main/java/com/forage/controller/PromotionController.java`

```java
package com.forage.controller;

import com.forage.entity.Promotion;
import com.forage.service.PromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/promotions")
@RequiredArgsConstructor
public class PromotionController {
    
    private final PromotionService promotionService;
    
    // GET: Afficher la liste des promotions
    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        List<Promotion> promotions;
        
        if (search != null && !search.isEmpty()) {
            promotions = promotionService.search(search);
            model.addAttribute("search", search);
        } else {
            promotions = promotionService.findAll();
        }
        
        model.addAttribute("promotions", promotions);
        model.addAttribute("activeCount", promotionService.countActivePromotions());
        return "promotion/list";
    }
    
    // GET: Formulaire pour créer une nouvelle promotion
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        return "promotion/form";
    }
    
    // GET: Formulaire pour éditer une promotion
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Promotion promotion = promotionService.findById(id);
        model.addAttribute("promotion", promotion);
        return "promotion/form";
    }
    
    // GET: Voir les détails d'une promotion
    @GetMapping("/{id}")
    public String view(@PathVariable int id, Model model) {
        Promotion promotion = promotionService.findById(id);
        model.addAttribute("promotion", promotion);
        model.addAttribute("isValid", promotion.isValid());
        return "promotion/view";
    }
    
    // POST: Enregistrer une promotion (création ou modification)
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Promotion promotion,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "promotion/form";
        }
        
        try {
            promotionService.save(promotion);
            redirectAttributes.addFlashAttribute("success", 
                "Promotion '" + promotion.getCode() + "' enregistrée avec succès");
            return "redirect:/promotions";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/promotions/new";
        }
    }
    
    // GET: Supprimer une promotion
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Promotion promotion = promotionService.findById(id);
            String code = promotion.getCode();
            promotionService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", 
                "Promotion '" + code + "' supprimée avec succès");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/promotions";
    }
    
    // POST: Activer/Désactiver une promotion
    @PostMapping("/{id}/toggle")
    public String toggleActive(@PathVariable int id, 
                               @RequestParam Boolean active,
                               RedirectAttributes redirectAttributes) {
        try {
            Promotion promotion = promotionService.toggleActive(id, active);
            String status = active ? "activée" : "désactivée";
            redirectAttributes.addFlashAttribute("success", 
                "Promotion " + status + " avec succès");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/promotions";
    }
}
```

---

## Étape 5: Créer les Templates Thymeleaf

### 5.1 Liste des Promotions

**Fichier**: `src/main/resources/templates/promotion/list.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Gestion des Promotions</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <h1>Gestion des Promotions</h1>
        
        <!-- Messages Flash -->
        <div th:if="${success}" class="alert alert-success">
            <span th:text="${success}"></span>
        </div>
        <div th:if="${error}" class="alert alert-danger">
            <span th:text="${error}"></span>
        </div>
        
        <!-- En-tête avec lien ajouter -->
        <div class="header-section">
            <a th:href="@{/promotions/new}" class="btn btn-primary">
                + Ajouter une Promotion
            </a>
            <span class="badge" th:text="'Promotions actives: ' + ${activeCount}"></span>
        </div>
        
        <!-- Recherche -->
        <form method="get" th:action="@{/promotions}" class="search-form">
            <input type="text" name="search" placeholder="Rechercher par code..." 
                   th:value="${search ?: ''}">
            <button type="submit">Rechercher</button>
        </form>
        
        <!-- Tableau de promotions -->
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>Code</th>
                    <th>Description</th>
                    <th>Réduction</th>
                    <th>Montant Max</th>
                    <th>Du</th>
                    <th>Au</th>
                    <th>Statut</th>
                    <th>Valide?</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="promo : ${promotions}" 
                    th:class="${promo.isValid()} ? 'table-success' : 'table-danger'">
                    <td th:text="${promo.code}"></td>
                    <td th:text="${promo.description}"></td>
                    <td th:text="${promo.pourcentageReduction} + '%'"></td>
                    <td th:text="${promo.montantMax} + '€'"></td>
                    <td th:text="${#dates.format(promo.dateDebut, 'dd/MM/yyyy')}"></td>
                    <td th:text="${#dates.format(promo.dateFin, 'dd/MM/yyyy')}"></td>
                    <td>
                        <span th:class="${promo.active} ? 'badge-success' : 'badge-danger'"
                              th:text="${promo.active} ? 'Actif' : 'Inactif'"></span>
                    </td>
                    <td>
                        <span th:text="${promo.isValid()} ? '✓ Oui' : '✗ Non'"
                              th:class="${promo.isValid()} ? 'text-success' : 'text-danger'"></span>
                    </td>
                    <td>
                        <a th:href="@{/promotions/{id}(id=${promo.id})}" 
                           class="btn btn-sm btn-info">Voir</a>
                        <a th:href="@{/promotions/{id}/edit(id=${promo.id})}" 
                           class="btn btn-sm btn-warning">Éditer</a>
                        <a th:href="@{/promotions/{id}/delete(id=${promo.id})}" 
                           class="btn btn-sm btn-danger"
                           onclick="return confirm('Confirmer la suppression?')">Supprimer</a>
                    </td>
                </tr>
                <tr th:if="${#lists.isEmpty(promotions)}">
                    <td colspan="9" class="text-center">Aucune promotion trouvée</td>
                </tr>
            </tbody>
        </table>
        
        <a th:href="@{/}" class="btn btn-secondary">Retour</a>
    </div>
</body>
</html>
```

### 5.2 Formulaire Promotion

**Fichier**: `src/main/resources/templates/promotion/form.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Formulaire Promotion</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <h1 th:text="${promotion.id == null} ? 'Nouvelle Promotion' : 'Éditer Promotion'"></h1>
        
        <!-- Affichage des erreurs globales -->
        <div th:if="${#fields.hasAnyErrors()}" class="alert alert-danger">
            <h4>Erreurs détectées:</h4>
            <ul>
                <li th:each="err : ${#fields.allErrors()}" th:text="${err}"></li>
            </ul>
        </div>
        
        <!-- Formulaire -->
        <form method="post" th:action="@{/promotions/save}" th:object="${promotion}">
            
            <!-- Champ ID (caché si modification) -->
            <input type="hidden" th:field="*{id}">
            
            <!-- Champ Code -->
            <div class="form-group">
                <label for="code">Code Promotion *</label>
                <input type="text" id="code" th:field="*{code}" 
                       class="form-control"
                       th:classappend="${#fields.hasErrors('code')} ? 'is-invalid' : ''"
                       placeholder="ex: SUMMER2024">
                <span th:if="${#fields.hasErrors('code')}" 
                      th:errors="*{code}" 
                      class="invalid-feedback"></span>
            </div>
            
            <!-- Champ Description -->
            <div class="form-group">
                <label for="description">Description *</label>
                <textarea id="description" th:field="*{description}" 
                          class="form-control"
                          th:classappend="${#fields.hasErrors('description')} ? 'is-invalid' : ''"
                          rows="3"
                          placeholder="ex: Promotion été 2024 - 20% de réduction"></textarea>
                <span th:if="${#fields.hasErrors('description')}" 
                      th:errors="*{description}" 
                      class="invalid-feedback"></span>
            </div>
            
            <!-- Champ Pourcentage Réduction -->
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="pourcentageReduction">Pourcentage Réduction (%) *</label>
                    <input type="number" id="pourcentageReduction" th:field="*{pourcentageReduction}" 
                           class="form-control"
                           th:classappend="${#fields.hasErrors('pourcentageReduction')} ? 'is-invalid' : ''"
                           min="1" max="100" step="0.01"
                           placeholder="20">
                    <span th:if="${#fields.hasErrors('pourcentageReduction')}" 
                          th:errors="*{pourcentageReduction}" 
                          class="invalid-feedback"></span>
                </div>
                
                <!-- Champ Montant Max -->
                <div class="form-group col-md-6">
                    <label for="montantMax">Montant Maximum de Réduction (€) *</label>
                    <input type="number" id="montantMax" th:field="*{montantMax}" 
                           class="form-control"
                           th:classappend="${#fields.hasErrors('montantMax')} ? 'is-invalid' : ''"
                           min="0" step="0.01"
                           placeholder="100">
                    <span th:if="${#fields.hasErrors('montantMax')}" 
                          th:errors="*{montantMax}" 
                          class="invalid-feedback"></span>
                </div>
            </div>
            
            <!-- Dates -->
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="dateDebut">Date de Début *</label>
                    <input type="date" id="dateDebut" th:field="*{dateDebut}" 
                           class="form-control"
                           th:classappend="${#fields.hasErrors('dateDebut')} ? 'is-invalid' : ''">
                    <span th:if="${#fields.hasErrors('dateDebut')}" 
                          th:errors="*{dateDebut}" 
                          class="invalid-feedback"></span>
                </div>
                
                <div class="form-group col-md-6">
                    <label for="dateFin">Date de Fin *</label>
                    <input type="date" id="dateFin" th:field="*{dateFin}" 
                           class="form-control"
                           th:classappend="${#fields.hasErrors('dateFin')} ? 'is-invalid' : ''">
                    <span th:if="${#fields.hasErrors('dateFin')}" 
                          th:errors="*{dateFin}" 
                          class="invalid-feedback"></span>
                </div>
            </div>
            
            <!-- Checkbox Active -->
            <div class="form-check mb-3">
                <input type="checkbox" id="active" th:field="*{active}" 
                       class="form-check-input">
                <label for="active" class="form-check-label">
                    Promotion Active
                </label>
            </div>
            
            <!-- Boutons -->
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Enregistrer</button>
                <a th:href="@{/promotions}" class="btn btn-secondary">Annuler</a>
            </div>
        </form>
    </div>
</body>
</html>
```

### 5.3 Vue Détails Promotion

**Fichier**: `src/main/resources/templates/promotion/view.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Détails Promotion</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <div class="container">
        <h1 th:text="'Promotion: ' + ${promotion.code}"></h1>
        
        <!-- Statut -->
        <div class="status-banner" 
             th:class="${isValid} ? 'alert-success' : 'alert-danger'">
            <p th:text="'Status: ' + (${isValid} ? 'Valide' : 'Invalide')"></p>
            <p th:text="'Active: ' + (${promotion.active} ? 'Oui' : 'Non')"></p>
        </div>
        
        <!-- Détails -->
        <div class="details-section">
            <h2>Informations de la Promotion</h2>
            
            <div class="detail-row">
                <strong>Code:</strong>
                <span th:text="${promotion.code}"></span>
            </div>
            
            <div class="detail-row">
                <strong>Description:</strong>
                <span th:text="${promotion.description}"></span>
            </div>
            
            <div class="detail-row">
                <strong>Réduction:</strong>
                <span th:text="${promotion.pourcentageReduction} + '%'"></span>
            </div>
            
            <div class="detail-row">
                <strong>Montant Maximum:</strong>
                <span th:text="${promotion.montantMax} + '€'"></span>
            </div>
            
            <div class="detail-row">
                <strong>Date de Début:</strong>
                <span th:text="${#dates.format(promotion.dateDebut, 'dd/MM/yyyy')}"></span>
            </div>
            
            <div class="detail-row">
                <strong>Date de Fin:</strong>
                <span th:text="${#dates.format(promotion.dateFin, 'dd/MM/yyyy')}"></span>
            </div>
            
            <div class="detail-row">
                <strong>Nombre de Devis Utilisant Cette Promotion:</strong>
                <span th:text="${#lists.size(promotion.devis)}"></span>
            </div>
        </div>
        
        <!-- Actions -->
        <div class="form-actions">
            <a th:href="@{/promotions/{id}/edit(id=${promotion.id})}" 
               class="btn btn-warning">Éditer</a>
            <a th:href="@{/promotions/{id}/delete(id=${promotion.id})}" 
               class="btn btn-danger"
               onclick="return confirm('Confirmer la suppression?')">Supprimer</a>
            <a th:href="@{/promotions}" class="btn btn-secondary">Retour à la liste</a>
        </div>
    </div>
</body>
</html>
```

---

## Étape 6: Modifier l'entité Devis (association)

**Fichier**: `src/main/java/com/forage/entity/Devis.java`

Ajouter la relation avec Promotion dans la classe Devis existante:

```java
// Dans la classe Devis, ajouter:

@ManyToOne
@JoinColumn(name = "promotion_id")
private Promotion promotion;

// Getter et Setter
public Promotion getPromotion() {
    return promotion;
}

public void setPromotion(Promotion promotion) {
    this.promotion = promotion;
}

// Méthode métier pour calculer le montant avec réduction
public Double getTotalWithDiscount() {
    Double total = this.montantTotal; // ou calculer depuis les détails
    
    if (promotion != null && promotion.isValid()) {
        return total - promotion.calculateDiscount(total);
    }
    return total;
}
```

---

## Étape 7: Script SQL pour la Base de Données

**Fichier**: `sql/DonnerEntre.sql`

```sql
-- Création de la table promotions
CREATE TABLE promotions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL,
    pourcentage_reduction DOUBLE NOT NULL,
    montant_max DOUBLE NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Modification de la table devis pour ajouter la clé étrangère
ALTER TABLE devis ADD COLUMN promotion_id INT;
ALTER TABLE devis ADD CONSTRAINT fk_devis_promotion 
    FOREIGN KEY (promotion_id) REFERENCES promotions(id);

-- Insertion de données de test
INSERT INTO promotions (code, description, pourcentage_reduction, montant_max, date_debut, date_fin, active) 
VALUES 
    ('SUMMER2024', 'Promotion été 2024 - 20% de réduction', 20, 100, '2024-06-01', '2024-08-31', TRUE),
    ('WELCOME10', 'Bienvenue nouveaux clients - 10% de réduction', 10, 50, '2024-01-01', '2024-12-31', TRUE),
    ('BLACKFRIDAY', 'Black Friday 2024 - 30% de réduction', 30, 200, '2024-11-20', '2024-11-30', FALSE),
    ('VIP50', 'Clients VIP - 50% de réduction max 500€', 50, 500, '2024-01-01', '2024-12-31', TRUE);
```

---

## Étape 8: Mise à jour du pom.xml (si nécessaire)

Vérifier que les dépendances suivantes sont présentes dans `pom.xml`:

```xml
<!-- JPA et validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Lombok (déjà présent probablement) -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>

<!-- MySQL Driver (déjà présent probablement) -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

---

## Résumé: Fichiers à Créer/Modifier

### Créer (NEW):
1. ✅ `entity/Promotion.java` - Entité JPA
2. ✅ `repository/PromotionRepository.java` - Accès données
3. ✅ `service/PromotionService.java` - Logique métier
4. ✅ `controller/PromotionController.java` - Contrôleur REST
5. ✅ `templates/promotion/list.html` - Liste des promotions
6. ✅ `templates/promotion/form.html` - Formulaire
7. ✅ `templates/promotion/view.html` - Détails
8. ✅ `sql/DonnerEntre.sql` - Données SQL

### Modifier (EDIT):
1. 🔧 `entity/Devis.java` - Ajouter relation
2. 🔧 `templates/devis/form.html` - Ajouter select promotion
3. 🔧 `templates/devis/view.html` - Afficher promotion et réduction

---

## Workflow pour tester

1. **Exécuter le script SQL** pour créer la table et ajouter les données
2. **Redémarrer l'application**
3. **Accéder à** `http://localhost:8080/promotions`
4. **Tester les opérations CRUD**:
   - Créer une nouvelle promotion
   - Consulter la liste
   - Éditer une promotion
   - Supprimer une promotion
5. **Utiliser une promotion sur un devis**

---

## Architecture de l'ajout Promotion

```
┌─────────────────────────────────────┐
│        PromotionController          │ ← URL Mapping
├─────────────────────────────────────┤
│         PromotionService            │ ← Logique métier
├─────────────────────────────────────┤
│       PromotionRepository           │ ← Database
├─────────────────────────────────────┤
│          Promotion Entity           │ ← Data Model
├─────────────────────────────────────┤
│  Templates Thymeleaf (list, form)   │ ← Views (HTML)
└─────────────────────────────────────┘
```

Cette architecture suit le **pattern MVC** de Spring Boot avec une séparation claire des responsabilités!

