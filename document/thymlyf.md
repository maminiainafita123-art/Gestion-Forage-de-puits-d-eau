# Thymeleaf avec Spring Boot - Guide Complet

## Table des matières
1. [Introduction](#introduction)
2. [Configuration](#configuration)
3. [Syntaxe de base](#syntaxe-de-base)
4. [Attributs Thymeleaf](#attributs-thymeleaf)
5. [Expressions](#expressions)
6. [Contrôle de flux](#contrôle-de-flux)
7. [Fragments et réutilisation](#fragments-et-réutilisation)
8. [Intégration Spring](#intégration-spring)
9. [Exemples pratiques](#exemples-pratiques)
10. [Bonnes pratiques](#bonnes-pratiques)

---

## Introduction

**Thymeleaf** est un moteur de template Java moderne pour Spring Boot qui :
- Génère du contenu dynamique HTML/XML/PDF/Text
- S'intègre parfaitement avec Spring MVC
- Permet la validation côté serveur
- Supporte les expressions OGNL (Object-Graph Navigation Language)
- Est facile à comprendre et à maintenir

### Avantages
- Syntaxe naturelle en HTML (attributs `th:*`)
- Support du cache et de la performance
- Validation intégrée
- Prévisualisation en navigateur
- Intégration Spring complète

---

## Configuration

### 1. Dépendance Maven (`pom.xml`)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 2. Configuration dans `application.properties`
```properties
# Chemin des templates
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Mode de template
spring.thymeleaf.mode=HTML

# Cache (false pour développement)
spring.thymeleaf.cache=false

# Encodage
spring.thymeleaf.encoding=UTF-8

# Format de message personnalisé
spring.messages.basename=messages
```

### 3. Structure des dossiers
```
src/main/resources/
├── templates/           # Tous les templates .html
│   ├── home.html
│   ├── layout.html     # Layout principal
│   ├── client/
│   │   ├── form.html
│   │   ├── list.html
│   │   └── view.html
│   └── ...
├── static/             # Ressources statiques
│   ├── css/
│   ├── js/
│   └── images/
└── messages.properties # Messages i18n
```

---

## Syntaxe de base

### 1. Déclaration du namespace
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Mon application</title>
</head>
<body>
    <!-- Contenu Thymeleaf -->
</body>
</html>
```

### 2. Affichage simple
```html
<!-- th:text remplace le contenu -->
<p th:text="${variable}"></p>

<!-- th:utext pour HTML non échappé (dangereux) -->
<p th:utext="${htmlContent}"></p>

<!-- Syntaxe raccourcie [[ ]] -->
<p>[[${variable}]]</p>
```

---

## Attributs Thymeleaf

### Attributs courants

| Attribut | Usage | Exemple |
|----------|-------|---------|
| `th:text` | Remplace le contenu texte | `<p th:text="${msg}">défaut</p>` |
| `th:html` | Remplace le contenu HTML | `<div th:utext="${html}"></div>` |
| `th:attr` | Modifie un attribut HTML | `<img th:attr="src=${url}">` |
| `th:href` | Génère l'attribut href | `<a th:href="@{/user/{id}(id=${user.id})}">` |
| `th:src` | Génère l'attribut src | `<img th:src="@{/images/logo.png}">` |
| `th:value` | Remplace la valeur | `<input th:value="${user.name}">` |
| `th:checked` | Attribut checked | `<input type="checkbox" th:checked="${active}">` |
| `th:selected` | Attribut selected | `<option th:selected="${choice}">` |
| `th:disabled` | Attribut disabled | `<button th:disabled="${isDisabled}">` |
| `th:class` | Gère les classes CSS | `<div th:class="${active} ? 'on' : 'off'">` |
| `th:classappend` | Ajoute une classe | `<div th:classappend="${error} ? 'has-error'">` |
| `th:style` | Génère l'attribut style | `<div th:style="'color: ' + ${color}">` |

### Exemples d'attributs
```html
<!-- Attribut normal avec expression -->
<a th:href="@{/client/view/{id}(id=${client.id})}" th:title="${client.nom}">
    Voir client
</a>

<!-- Condition sur classe CSS -->
<div th:class="${user.premium} ? 'premium-badge' : ''">
    Premium
</div>

<!-- Attribut style dynamique -->
<div th:style="'background-color: ' + ${color} + '; padding: 10px'">
    Contenu
</div>

<!-- Selected dans un select -->
<select name="status">
    <option value="ACTIF" th:selected="${client.status == 'ACTIF'}">Actif</option>
    <option value="INACTIF" th:selected="${client.status == 'INACTIF'}">Inactif</option>
</select>
```

---

## Expressions

Les expressions Thymeleaf sont le cœur de la templating. Il existe 5 types d'expressions principales :

### 1. Expressions d'attribut `${ }` - VARIABLE

C'est l'expression la plus courante. Elle accède aux variables du modèle envoyées par le controller.

```html
<!-- Variables simples du modèle -->
<p th:text="${user}"></p>
<p th:text="${title}"></p>
<p th:text="${count}"></p>

<!-- Accès aux propriétés (getter) -->
<p th:text="${user.name}"></p>        <!-- Appelle user.getName() -->
<p th:text="${user.email}"></p>
<p th:text="${user.isAdmin}"></p>

<!-- Appel explicite de méthode -->
<p th:text="${user.getFullName()}"></p>
<p th:text="${user.calculateAge()}"></p>

<!-- Accès aux propriétés imbriquées (navigation d'objet) -->
<p th:text="${client.address.city}"></p>
<p th:text="${order.customer.address.zipCode}"></p>

<!-- Accès à la map -->
<p th:text="${map['key']}"></p>
<p th:text="${map.get('key')}"></p>
<p th:text="${userMap['email']}"></p>

<!-- Accès à la liste par index -->
<span th:text="${list[0]}"></span>           <!-- Premier élément -->
<span th:text="${list[1]}"></span>           <!-- Deuxième élément -->
<span th:text="${clients.get(0).name}"></span>

<!-- Accès à la propriété de tableau -->
<span th:text="${array[0]}"></span>
<span th:text="${fruits[2]}"></span>

<!-- Null-safe navigation (si parent est null, retourne null) -->
<p th:text="${user?.address?.city}"></p>    <!-- Ne plante pas si null -->
```

**Important**: L'accès aux propriétés privées se fait toujours par les **getters**, pas directement:
```java
// Class Java
public class User {
    private String name;      // Propriété privée
    public String getName() {  // Getter public
        return name;
    }
}
```

```html
<!-- Template -->
<p th:text="${user.name}"></p>  <!-- ✓ Appelle user.getName() -->
```

---

### 2. Sélection `*{ }` - SELECTION (Avec th:object)

Cette expression simplifie l'accès aux propriétés quand on travaille avec un objet spécifique. Elle doit être utilisée avec `th:object`.

```html
<!-- Premier exemple : simplification simple -->
<div th:object="${user}">
    <p th:text="*{name}"></p>           <!-- Équivalent à ${user.name} -->
    <p th:text="*{email}"></p>          <!-- Équivalent à ${user.email} -->
    <p th:text="*{phone}"></p>          <!-- Équivalent à ${user.phone} -->
</div>

<!-- Très utile avec les formulaires -->
<form th:object="${client}">
    <input type="text" name="nom" th:value="*{nom}">
    <input type="email" name="email" th:value="*{email}">
    <input type="tel" name="telephone" th:value="*{telephone}">
    
    <!-- Accès aux propriétés imbriquées -->
    <input type="text" name="city" th:value="*{address.city}">
    <input type="text" name="zipCode" th:value="*{address.zipCode}">
</form>

<!-- Combinaison de $ et * -->
<div th:object="${user}">
    <p th:text="*{name}"></p>                    <!-- Sélection -->
    <p th:text="${user.name}"></p>              <!-- Variable -->
    <p th:text="${user}"></p>                   <!-- Variable complète -->
</div>

<!-- Sélection imbriquée -->
<div th:object="${company}">
    <p th:text="*{name}"></p>
    
    <div th:object="*{address}">
        <!-- Maintenant * fait référence à l'adresse -->
        <p th:text="*{city}"></p>
        <p th:text="*{country}"></p>
    </div>
</div>

<!-- Sélection avec boucle -->
<div th:each="item : ${items}" th:object="${item}">
    <p th:text="*{name}"></p>
    <p th:text="*{description}"></p>
</div>
```

**Quand l'utiliser?**
- Formulaires avec beaucoup de champs du même objet
- Réduction de la répétition de code
- Amélioration de la lisibilité

---

### 3. URLs `@{ }` - URL

Cette expression génère des URLs dynamiques. Elle est principalement utilisée dans `th:href`, `th:src`, etc.

```html
<!-- URLs simples relatives -->
<a th:href="@{/}">Accueil</a>
<a th:href="@{/clients}">Liste clients</a>
<a th:href="@{/contact}">Contact</a>

<!-- URLs avec variables de chemin -->
<a th:href="@{/user/{id}(id=${user.id})}">Voir profil</a>
<!-- Génère: /user/123 -->

<a th:href="@{/client/{id}/edit(id=${client.id})}">Éditer</a>
<!-- Génère: /client/456/edit -->

<!-- URLs avec paramètres de requête -->
<a th:href="@{/search(query=${searchTerm})}">Rechercher</a>
<!-- Génère: /search?query=java -->

<a th:href="@{/search(query=${q}, page=${currentPage})}">Suivant</a>
<!-- Génère: /search?query=java&page=2 -->

<!-- Combinaison: chemin + paramètres -->
<a th:href="@{/user/{id}/orders(id=${user.id}, sort=${sort})}">Commandes</a>
<!-- Génère: /user/123/orders?sort=date -->

<!-- Plusieurs paramètres complexes -->
<a th:href="@{/products(category=${cat}, min=${minPrice}, max=${maxPrice}, sort=${sort})}">
    Filtrer
</a>
<!-- Génère: /products?category=electronics&min=100&max=500&sort=price -->

<!-- URLs relativement au contexte -->
<a th:href="@{~/other-app}">Autre application</a>

<!-- URLs absolues (externes) -->
<a th:href="@{http://www.example.com}">Lien externe</a>
<a th:href="@{https://github.com/example}">GitHub</a>

<!-- Pour des ressources statiques -->
<img th:src="@{/images/logo.png}">
<link th:href="@{/css/style.css}" rel="stylesheet">
<script th:src="@{/js/app.js}"></script>

<!-- Utiliser des variables pour des chemins dynamiques -->
<img th:src="@{${imagePath}}">

<!-- URLs dans les attributs dynamiques -->
<a th:attr="href=@{/page/{id}(id=${id})}">Lien</a>
```

**Avantages de @{ }:**
- Génère des URLs correctamente échappées
- Gère automatiquement le contexte de l'application
- Évite les erreurs de construction d'URL
- Supporte les paramètres dynamiques facilement

---

### 4. Messages i18n `#{ }` - MESSAGE

Cette expression charge des messages depuis les fichiers de propriétés pour l'internationalisation (i18n).

```html
<!-- Chargement simple d'un message -->
<p th:text="#{welcome.message}"></p>
<p th:text="#{application.title}"></p>
<p th:text="#{home.description}"></p>

<!-- Messages avec paramètres -->
<p th:text="#{welcome.user(${user.name})}"></p>
<!-- Affiche: Bienvenue, Jean (si user.name = "Jean") -->

<p th:text="#{order.total(${order.subtotal}, ${order.tax})}"></p>

<!-- Plusieurs paramètres -->
<p th:text="#{invoice.from(${company.name}, ${company.city})}"></p>

<!-- Messages comme attributs d'éléments HTML -->
<button th:title="#{button.save.tooltip}">Enregistrer</button>
<input type="text" th:placeholder="#{input.search.placeholder}">

<!-- Utilisation dans les attributs -->
<p th:attr="title=#{page.title}">Contenu</p>

<!-- Messages conditionnels -->
<span th:if="${user.isPremium}" th:text="#{premium.label}"></span>

<!-- Messages dans les boucles -->
<li th:each="item : ${items}" th:text="#{item.name(${item})}"></li>

<!-- Utilisation avec d'autres expressions -->
<p th:text="#{user.info(${user.getFullName()}, ${user.email})}"></p>
```

**Structure des fichiers messages.properties:**
```properties
# messages.properties (Anglais par défaut)
welcome.message=Welcome to our application
welcome.user=Welcome, {0}
application.title=My Application

# messages_fr.properties (Français)
welcome.message=Bienvenue dans notre application
welcome.user=Bienvenue, {0}
application.title=Ma Application

# Messages avec paramètres
order.total=Sous-total: {0}€, Taxes: {1}€
invoice.from=Facture de {0} ({1})
user.info=Utilisateur: {0}, Email: {1}
```

**Configuration dans application.properties:**
```properties
spring.messages.basename=messages
spring.messages.encoding=UTF-8
spring.messages.fallback-to-system-locale=false
```

---

### 5. Objets contexte et utilitaires `#{ }`

Ces variables spéciales de Thymeleaf donnent accès aux objets du contexte Spring et aux fonctions d'utilité.

```html
<!-- ========== DATES ========== -->
<p th:text="${#dates.format(now, 'dd/MM/yyyy')}"></p>
<p th:text="${#dates.format(dateClient, 'yyyy-MM-dd HH:mm:ss')}"></p>
<p th:text="${#dates.year(date)}"></p>            <!-- Année seulement -->
<p th:text="${#dates.month(date)}"></p>           <!-- Mois (1-12) -->
<p th:text="${#dates.day(date)}"></p>             <!-- Jour du mois -->
<p th:text="${#dates.dayOfWeek(date)}"></p>       <!-- Jour de semaine (1-7) -->

<!-- ========== STRINGS ========== -->
<p th:text="${#strings.capitalize(text)}"></p>                    <!-- Première lettre maj -->
<p th:text="${#strings.toUpperCase(text)}"></p>                   <!-- MAJUSCULES -->
<p th:text="${#strings.toLowerCase(text)}"></p>                   <!-- minuscules -->
<p th:text="${#strings.substring(text, 0, 5)}"></p>               <!-- Sous-chaîne -->
<p th:text="${#strings.substringAfter(text, '-')}"></p>           <!-- Après délimiteur -->
<p th:text="${#strings.substringBefore(text, '.')}"></p>          <!-- Avant délimiteur -->
<p th:text="${#strings.length(text)}"></p>                        <!-- Longueur -->
<p th:text="${#strings.replace(text, 'old', 'new')}"></p>         <!-- Remplacer -->
<p th:if="${#strings.isEmpty(text)}">Vide</p>                     <!-- Est vide? -->
<p th:if="${#strings.startsWith(text, 'prefix')}">Commence</p>    <!-- Commence par? -->
<p th:if="${#strings.endsWith(text, 'suffix')}">Finit par</p>     <!-- Finit par? -->
<p th:if="${#strings.contains(text, 'substring')}">Contient</p>   <!-- Contient? -->
<p th:text="${#strings.join(array, ' - ')}"></p>                  <!-- Joindre tableau -->
<p th:text="${#strings.split(text, ',')}"></p>                    <!-- Diviser chaîne -->

<!-- ========== NUMBERS ========== -->
<p th:text="${#numbers.formatDecimal(price, 1, 2)}"></p>           <!-- 1 entier, 2 décimales -->
<p th:text="${#numbers.formatInteger(count, 5)}"></p>             <!-- Entier aligné 5 chiffres -->
<p th:text="${#numbers.formatCurrency(price)}"></p>               <!-- Devise locale -->
<p th:text="${#numbers.sequence(1, 5)}"></p>                       <!-- Séquence 1-5 -->

<!-- ========== COLLECTIONS ========== -->
<p th:text="${#lists.size(list)}"></p>                            <!-- Taille liste -->
<p th:if="${#lists.isEmpty(list)}">Liste vide</p>                 <!-- Vide? -->
<p th:text="${#lists.first(list)}"></p>                           <!-- Premier élément -->
<p th:text="${#lists.last(list)}"></p>                            <!-- Dernier élément -->
<p th:if="${#lists.contains(list, item)}">Contient</p>            <!-- Contient? -->

<p th:text="${#sets.size(set)}"></p>
<p th:if="${#sets.isEmpty(set)}">Ensemble vide</p>

<p th:text="${#maps.size(map)}"></p>
<p th:if="${#maps.isEmpty(map)}">Map vide</p>
<p th:if="${#maps.containsKey(map, 'key')}">Clé existe</p>

<!-- ========== OBJETS REQUEST, SESSION, APPLICATION ========== -->
<p th:text="${#httpServletRequest.requestURI}"></p>        <!-- URL actuelle -->
<p th:text="${#httpServletRequest.method}"></p>            <!-- GET, POST, etc. -->
<p th:text="${#httpServletRequest.getHeader('referer')}"></p>  <!-- En-tête HTTP -->

<p th:text="${session.userId}"></p>                        <!-- Variable de session -->
<p th:text="${session.userName}"></p>

<p th:text="${application.appVersion}"></p>                <!-- Variable d'application -->
<p th:text="${application.appName}"></p>

<!-- ========== AUTHENTIFICATION (Avec Spring Security) ========== -->
<p th:text="${#authentication.principal.username}"></p>    <!-- Utilisateur connecté -->
<p th:text="${#authentication.isAuthenticated()}"></p>     <!-- Est authentifié? -->
<p th:if="${#authorization.hasRole('ADMIN')}">Admin</p>   <!-- Vérifier rôle -->
```

---

### Opérateurs

#### Opérateurs arithmétiques
```html
<span th:text="${10 + 5}"></span>          <!-- Addition: 15 -->
<span th:text="${10 - 5}"></span>          <!-- Soustraction: 5 -->
<span th:text="${10 * 5}"></span>          <!-- Multiplication: 50 -->
<span th:text="${10 / 5}"></span>          <!-- Division: 2 -->
<span th:text="${10 % 3}"></span>          <!-- Modulo: 1 -->

<!-- Avec variables -->
<span th:text="${price * quantity}"></span>
<span th:text="${total / numberOfItems}"></span>
<span th:text="${discount + tax}"></span>
```

#### Opérateurs logiques
```html
<!-- AND (et) - Les deux doivent être vrais -->
<span th:text="${isActive and isVerified}"></span>
<span th:text="${user.isPremium and user.hasAccess}"></span>

<!-- OR (ou) - Au moins un doit être vrai -->
<span th:text="${isExpired or isCancelled}"></span>
<span th:text="${admin or moderator}"></span>

<!-- NOT (non) - Inverse la valeur -->
<span th:text="${not disabled}"></span>
<span th:text="${!completed}"></span>      <!-- Alternative -->
<span th:if="${not user.isAdmin}">Non-admin</span>
```

#### Opérateurs de comparaison
```html
<!-- Égal -->
<span th:if="${age == 18}">Adulte</span>
<span th:if="${name == 'Admin'}">Administrateur</span>

<!-- Pas égal -->
<span th:if="${status != 'pending'}">Pas en attente</span>
<span th:if="${count != 0}">Non vide</span>

<!-- Plus grand que -->
<span th:if="${age > 18}">Plus de 18 ans</span>
<span th:if="${price > 100}">Cher</span>

<!-- Plus grand ou égal -->
<span th:if="${price >= 50}">Au moins 50€</span>

<!-- Plus petit que -->
<span th:if="${stock < 5}">Stock faible</span>

<!-- Plus petit ou égal -->
<span th:if="${age <= 65}">Pas retraité</span>
```

#### Opérateur ternaire (conditionnel)
```html
<!-- Syntaxe: condition ? valeur_si_vrai : valeur_si_faux -->
<span th:text="${user.isPremium ? 'Abonné' : 'Gratuit'}"></span>

<div th:class="${isActive ? 'active' : 'inactive'}">
    Statut
</div>

<p th:text="${order.total > 100 ? 'Commande élevée' : 'Commande normale'}"></p>

<!-- Ternaire imbriqué -->
<span th:text="${age < 18 ? 'Mineur' : age < 65 ? 'Adulte' : 'Senior'}"></span>

<!-- Avec expressions complexes -->
<span th:text="${user.role == 'ADMIN' and user.isActive ? 
              'Admin actif' : 'Non admin ou inactif'}"></span>
```

#### Elvis operator (null coalescing) - Valeur par défaut
```html
<!-- Syntaxe: expression ?: valeur_par_defaut -->
<!-- Si expression est null ou false, utilise la valeur par défaut -->

<span th:text="${user.name ?: 'Inconnu'}"></span>
<!-- Affiche user.name s'il existe, sinon "Inconnu" -->

<span th:text="${product.description ?: 'Pas de description'}"></span>

<span th:text="${discount ?: 0}"></span>

<!-- Avec expressions imbriquées -->
<span th:text="${user?.profile?.bio ?: 'Pas de bio'}"></span>

<!-- Dans les attributs -->
<input type="text" th:value="${user.email ?: 'email@example.com'}">
```

#### Comparaison de strings
```html
<!-- Comparaison sensible à la casse -->
<span th:if="${name == 'Admin'}">Exact 'Admin'</span>

<!-- Pas de comparaison case-insensitive directe, utiliser #strings -->
<span th:if="${#strings.equals(name, 'admin')}">Équivalent</span>

<!-- Vérifier le début/fin -->
<span th:if="${#strings.startsWith(email, 'test@')}">Email de test</span>
<span th:if="${#strings.endsWith(filename, '.pdf')}">Document PDF</span>

<!-- Contient -->
<span th:if="${#strings.contains(description, 'important')}">Marqué important</span>
```

#### Vérification de null/vide
```html
<!-- Avec Elvis operator -->
<span th:text="${value ?: 'Pas de valeur'}"></span>

<!-- Avec #strings -->
<span th:if="${#strings.isEmpty(text)}">Chaîne vide</span>

<!-- Avec #lists -->
<span th:if="${#lists.isEmpty(items)}">Liste vide</span>

<!-- Accès sécurisé (null-safe) -->
<span th:text="${user?.address?.city ?: 'Non spécifié'}"></span>
<!-- N'affiche rien si user ou address est null -->
```

---

### Résumé des types d'expressions

| Expression | Syntaxe | Utilisation |
|-----------|---------|------------|
| **Variable** | `${variable}` | Accès aux variables du modèle |
| **Sélection** | `*{property}` | Avec `th:object`, raccourci |
| **URL** | `@{/chemin}` | Générer des URLs dynamiques |
| **Message** | `#{key}` | Internationalisation i18n |
| **Objet contexte** | `${#dates}`, `${#strings}`, etc. | Fonctions d'utilité Thymeleaf |

---

## Contrôle de flux

### 1. Conditions `th:if` et `th:unless`
```html
<!-- th:if -->
<div th:if="${user.isAdmin}">
    <p>Panneau d'administration</p>
</div>

<!-- th:unless (inverse de th:if) -->
<div th:unless="${user.isAdmin}">
    <p>Vous n'êtes pas administrateur</p>
</div>

<!-- th:if/th:else (with th:switch) -->
<div th:if="${status == 'ACTIVE'}">
    <p>Client actif</p>
</div>
<div th:if="${status == 'PENDING'}" th:remove="tag">
    <p>Client en attente</p>
</div>
<div th:if="${status == 'INACTIVE'}">
    <p>Client inactif</p>
</div>
```

### 2. Switch/Case `th:switch` et `th:case`
```html
<div th:switch="${user.role}">
    <span th:case="'ADMIN'">Administrateur</span>
    <span th:case="'USER'">Utilisateur</span>
    <span th:case="'GUEST'">Invité</span>
    <span th:case="*">Rôle inconnu</span>
</div>
```

### 3. Boucles `th:each`
```html
<!-- Boucle simple -->
<ul>
    <li th:each="client : ${clients}" th:text="${client.name}"></li>
</ul>

<!-- Avec index -->
<ul>
    <li th:each="client, iterStat : ${clients}">
        <span th:text="${iterStat.index}"></span>
        <span th:text="${client.name}"></span>
    </li>
</ul>

<!-- Objets de statut de boucle -->
<table>
    <tr th:each="item, stat : ${items}" th:class="${stat.odd} ? 'odd' : 'even'">
        <td th:text="${stat.count}"></td>      <!-- Numéro (1-indexed) -->
        <td th:text="${stat.index}"></td>      <!-- Index (0-indexed) -->
        <td th:text="${stat.size}"></td>       <!-- Taille totale -->
        <td th:text="${stat.first}"></td>      <!-- Vrai si premier -->
        <td th:text="${stat.last}"></td>       <!-- Vrai si dernier -->
        <td th:text="${item.name}"></td>
    </tr>
</table>

<!-- Boucle avec condition -->
<li th:each="client : ${clients}" th:if="${client.status == 'ACTIVE'}">
    <span th:text="${client.name}"></span>
</li>
```

---

## Fragments et réutilisation

### 1. Définir des fragments
```html
<!-- layout.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Mon site</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <!-- Fragment header -->
    <header th:fragment="header">
        <nav>
            <a th:href="@{/}">Accueil</a>
            <a th:href="@{/clients}">Clients</a>
        </nav>
    </header>

    <!-- Fragment footer -->
    <footer th:fragment="footer">
        <p>&copy; 2024 Mon application</p>
    </footer>

    <!-- Fragment avec paramètres -->
    <div th:fragment="alert(type, message)">
        <div th:class="'alert alert-' + ${type}">
            <span th:text="${message}"></span>
        </div>
    </div>
</body>
</html>
```

### 2. Utiliser des fragments
```html
<!-- home.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Accueil</title>
</head>
<body>
    <!-- Inclure un fragment -->
    <div th:insert="~{layout :: header}"></div>

    <main>
        <h1>Bienvenue</h1>
        
        <!-- Inclure un fragment avec paramètres -->
        <div th:replace="~{layout :: alert('success', 'Opération réussie')}"></div>
    </main>

    <div th:insert="~{layout :: footer}"></div>
</body>
</html>
```

### 3. Différences entre th:insert, th:replace, th:include
```html
<!-- Fragment original dans layout.html -->
<div th:fragment="box">Contenu du box</div>

<!-- th:insert - Insère le fragment DANS l'élément -->
<div th:insert="~{layout :: box}"></div>
<!-- Résultat: <div><div>Contenu du box</div></div> -->

<!-- th:replace - Remplace l'élément par le fragment -->
<div th:replace="~{layout :: box}"></div>
<!-- Résultat: <div>Contenu du box</div> -->

<!-- th:include - Insère SEULEMENT le contenu (deprecated) -->
<div th:include="~{layout :: box}"></div>
<!-- Résultat: <div>Contenu du box</div> -->
```

---

## Intégration Spring

### 1. Transmission de données au template
```java
@Controller
public class ClientController {
    
    @GetMapping("/clients")
    public String listClients(Model model) {
        List<Client> clients = clientService.findAll();
        model.addAttribute("clients", clients);
        model.addAttribute("title", "Liste des clients");
        return "client/list";  // Résout en client/list.html
    }
    
    @GetMapping("/client/{id}")
    public String viewClient(@PathVariable Long id, Model model) {
        Client client = clientService.findById(id);
        model.addAttribute("client", client);
        return "client/view";
    }
}
```

### 2. Validation et affichage d'erreurs
```java
// Controller
@PostMapping("/client/save")
public String saveClient(
    @Valid @ModelAttribute Client client,
    BindingResult result,
    Model model) {
    
    if (result.hasErrors()) {
        return "client/form";  // Retour au formulaire
    }
    clientService.save(client);
    return "redirect:/clients";
}
```

```html
<!-- Template avec affichage d'erreurs -->
<form method="post" th:action="@{/client/save}" th:object="${client}">
    
    <div>
        <label for="name">Nom:</label>
        <input type="text" id="name" th:field="*{name}" 
               th:class="${#fields.hasErrors('name')} ? 'is-invalid'">
        
        <!-- Affichage des erreurs -->
        <span th:if="${#fields.hasErrors('name')}" 
              th:errors="*{name}" 
              class="error-message"></span>
    </div>
    
    <div>
        <label for="email">Email:</label>
        <input type="email" id="email" th:field="*{email}">
        <span th:if="${#fields.hasErrors('email')}" 
              th:errors="*{email}"></span>
    </div>
    
    <button type="submit">Enregistrer</button>
</form>

<!-- Afficher TOUTES les erreurs -->
<div th:if="${#fields.hasAnyErrors()}">
    <div th:each="err : ${#fields.allErrors()}" 
         th:text="${err}" 
         class="alert alert-danger"></div>
</div>
```

### 3. Utiliser `th:field`
```html
<!-- Automatise name, id, value, th:value -->
<input type="text" th:field="*{name}">
<!-- Génère: id="name" name="name" value="${client.name}" -->

<input type="checkbox" th:field="*{active}">
<!-- Génère automatiquement checked si true -->

<select th:field="*{status}">
    <option value="ACTIVE" th:text="Actif"></option>
    <option value="INACTIVE" th:text="Inactif"></option>
</select>
<!-- Sélectionne automatiquement la bonne option -->

<textarea th:field="*{description}"></textarea>
<!-- Populé automatiquement -->
```

### 4. Utiliser les Beans Spring
```html
<!-- Accès aux beans enregistrés -->
<div th:text="${@myService.getData()}"></div>

<!-- Appel de méthodes statiques -->
<div th:text="${T(java.time.LocalDate).now()}"></div>

<!-- Format avec Thymeleaf et Spring -->
<div th:text="${#temporals.format(date, 'dd/MM/yyyy')}"></div>
```

---

## Exemples pratiques

### 1. Formulaire complet avec validation
```html
<!-- client/form.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout}">

<head>
    <title>Formulaire Client</title>
</head>

<body>
    <div layout:fragment="content">
        <h1>Ajouter/Modifier un client</h1>
        
        <form method="post" th:action="@{/client/save}" th:object="${client}">
            
            <!-- Erreurs globales -->
            <div th:if="${#fields.hasAnyErrors()}" class="alert alert-danger">
                <ul>
                    <li th:each="err : ${#fields.allErrors()}" 
                        th:text="${err}"></li>
                </ul>
            </div>
            
            <!-- Champ Nom -->
            <div class="form-group">
                <label for="nom">Nom *</label>
                <input type="text" id="nom" th:field="*{nom}" 
                       class="form-control"
                       th:classappend="${#fields.hasErrors('nom')} ? 'is-invalid'">
                <span th:if="${#fields.hasErrors('nom')}" 
                      th:errors="*{nom}" 
                      class="invalid-feedback"></span>
            </div>
            
            <!-- Champ Email -->
            <div class="form-group">
                <label for="email">Email *</label>
                <input type="email" id="email" th:field="*{email}" 
                       class="form-control"
                       th:classappend="${#fields.hasErrors('email')} ? 'is-invalid'">
                <span th:if="${#fields.hasErrors('email')}" 
                      th:errors="*{email}" 
                      class="invalid-feedback"></span>
            </div>
            
            <!-- Champ Téléphone -->
            <div class="form-group">
                <label for="telephone">Téléphone</label>
                <input type="tel" id="telephone" th:field="*{telephone}" 
                       class="form-control">
            </div>
            
            <!-- Select -->
            <div class="form-group">
                <label for="status">Statut</label>
                <select id="status" th:field="*{status}" class="form-control">
                    <option value="">-- Sélectionner --</option>
                    <option value="ACTIF" th:text="Actif"></option>
                    <option value="INACTIF" th:text="Inactif"></option>
                </select>
            </div>
            
            <!-- Checkbox -->
            <div class="form-check">
                <input type="checkbox" id="premium" th:field="*{premium}" 
                       class="form-check-input">
                <label for="premium" class="form-check-label">Premium</label>
            </div>
            
            <!-- Boutons -->
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Enregistrer</button>
                <a th:href="@{/clients}" class="btn btn-secondary">Annuler</a>
            </div>
        </form>
    </div>
</body>
</html>
```

### 2. Liste avec page et filtrage
```html
<!-- client/list.html -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout}">

<body>
    <div layout:fragment="content">
        <h1>Clients</h1>
        
        <!-- Lien ajouter -->
        <a th:href="@{/client/form}" class="btn btn-primary mb-3">
            Ajouter un client
        </a>
        
        <!-- Filtre/Recherche -->
        <form method="get" th:action="@{/clients}" class="mb-3">
            <input type="text" name="search" placeholder="Rechercher..." 
                   th:value="${search}">
            <select name="status">
                <option value="">-- Tous --</option>
                <option value="ACTIF" 
                        th:selected="${status == 'ACTIF'}">Actif</option>
                <option value="INACTIF" 
                        th:selected="${status == 'INACTIF'}">Inactif</option>
            </select>
            <button type="submit">Filtrer</button>
        </form>
        
        <!-- Tableau -->
        <table class="table">
            <thead>
                <tr>
                    <th>Nom</th>
                    <th>Email</th>
                    <th>Statut</th>
                    <th>Premium</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="client, stat : ${clients}" 
                    th:class="${stat.odd} ? 'table-light'">
                    <td th:text="${client.nom}"></td>
                    <td th:text="${client.email}"></td>
                    <td>
                        <span th:text="${client.status}"
                              th:class="'badge badge-' + 
                                        (${client.status == 'ACTIF'} ? 'success' : 'danger')">
                        </span>
                    </td>
                    <td>
                        <i th:if="${client.premium}" class="icon-check"></i>
                        <i th:unless="${client.premium}" class="icon-x"></i>
                    </td>
                    <td>
                        <a th:href="@{/client/view/{id}(id=${client.id})}" 
                           class="btn btn-sm btn-info">Voir</a>
                        <a th:href="@{/client/edit/{id}(id=${client.id})}" 
                           class="btn btn-sm btn-warning">Éditer</a>
                        <a th:href="@{/client/delete/{id}(id=${client.id})}" 
                           class="btn btn-sm btn-danger"
                           onclick="return confirm('Confirmer la suppression?')">
                           Supprimer</a>
                    </td>
                </tr>
                <tr th:if="${#lists.isEmpty(clients)}">
                    <td colspan="5" class="text-center">Aucun client</td>
                </tr>
            </tbody>
        </table>
    </div>
</body>
</html>
```

### 3. Template avec layout réutilisable
```html
<!-- layout.html - Layout principal -->
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title th:text="${title ?: 'Mon Application'}"></title>
    <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
    <link rel="stylesheet" th:href="@{/css/style.css}">
</head>
<body>
    <!-- Navigation -->
    <nav class="navbar">
        <div class="navbar-brand">
            <a th:href="@{/}">Mon App</a>
        </div>
        <ul class="navbar-nav">
            <li><a th:href="@{/}">Accueil</a></li>
            <li><a th:href="@{/clients}">Clients</a></li>
            <li><a th:href="@{/demandes}">Demandes</a></li>
            <li th:if="${#authentication}" class="user-menu">
                <span th:text="${#authentication.principal.username}"></span>
                <a th:href="@{/logout}">Déconnexion</a>
            </li>
        </ul>
    </nav>
    
    <!-- Messages Flash -->
    <div th:if="${message}" class="alert alert-success" role="alert">
        <span th:text="${message}"></span>
    </div>
    
    <!-- Contenu principal -->
    <main class="container">
        <div th:fragment="content">
            <!-- Sera remplacé par le contenu des pages -->
        </div>
    </main>
    
    <!-- Footer -->
    <footer class="footer">
        <p>&copy; 2024 Mon Application. Tous droits réservés.</p>
    </footer>
    
    <script th:src="@{/js/bootstrap.min.js}"></script>
    <script th:src="@{/js/app.js}"></script>
</body>
</html>
```

---

## Bonnes pratiques

### 1. Sécurité
```html
<!-- Échappement automatique (par défaut) -->
<p th:text="${userInput}"></p>
<!-- Sûr contre XSS -->

<!-- Utiliser th:utext uniquement pour du HTML de confiance -->
<div th:utext="${trustedHtml}"></div>

<!-- Protection CSRF (si Security est utilisé) -->
<form method="post" th:action="@{/submit}">
    <input type="hidden" th:name="${_csrf.parameterName}" 
           th:value="${_csrf.token}">
    <!-- Champs du formulaire -->
</form>
```

### 2. Performance
```html
<!-- Utiliser th:if au lieu de CSS display:none -->
<div th:if="${expensive_condition}">
    Contenu affiché uniquement si la condition est vraie
</div>

<!-- Utiliser le cache en production -->
<!-- spring.thymeleaf.cache=true -->

<!-- Éviter les appels coûteux dans les boucles -->
<!-- ✗ Mauvais -->
<span th:each="item : ${items}" th:text="${heavyMethod()}"></span>

<!-- ✓ Bon -->
<span th:each="item : ${items}" th:text="${item}"></span>
```

### 3. Lisibilité
```html
<!-- Utiliser des noms significatifs -->
<li th:each="client : ${clients}">
    <span th:text="${client.nom}"></span>
</li>

<!-- Commentaires Thymeleaf -->
<!--/* Ce commentaire est supprimé en production */-->

<!-- Indentation cohérente -->
<div>
    <span th:text="${data}"></span>
</div>
```

### 4. Réutilisabilité
```html
<!-- Créer des fragments pour les éléments réutilisés -->
<div th:fragment="userCard(user)">
    <div class="card">
        <h5 th:text="${user.nom}"></h5>
        <p th:text="${user.email}"></p>
    </div>
</div>

<!-- Utiliser dans d'autres templates -->
<div th:replace="~{fragments/userCard :: userCard(${currentUser})}"></div>
```

### 5. Tests
```html
<!-- Faciliter les tests unitaires pour le contrôleur -->
@GetMapping("/test")
public String test(Model model) {
    model.addAttribute("expectedValue", "value");
    return "test";
}

<!-- Template testable -->
<p th:text="${expectedValue}" data-testid="output">Default</p>
```

---

## Fonctions d'utilité Thymeleaf

### Dates et temps
```html
<span th:text="${#dates.format(date, 'dd/MM/yyyy')}"></span>
<span th:text="${#dates.format(date, 'HH:mm:ss')}"></span>
<span th:text="${#dates.year(date)}"></span>
<span th:text="${#dates.month(date)}"></span>
<span th:text="${#dates.day(date)}"></span>
```

### Strings
```html
<span th:text="${#strings.capitalize(text)}"></span>
<span th:text="${#strings.toUpperCase(text)}"></span>
<span th:text="${#strings.toLowerCase(text)}"></span>
<span th:text="${#strings.substring(text, 0, 5)}"></span>
<span th:text="${#strings.length(text)}"></span>
<span th:text="${#strings.replace(text, 'old', 'new')}"></span>
<span th:if="${#strings.isEmpty(text)}">Vide</span>
<span th:if="${#strings.startsWith(text, 'prefix')}"></span>
```

### Numbers
```html
<span th:text="${#numbers.formatDecimal(price, 1, 2)}"></span>
<span th:text="${#numbers.formatInteger(count, 5)}"></span>
<span th:text="${#numbers.formatCurrency(price)}"></span>
```

### Collections
```html
<span th:text="${#lists.size(list)}"></span>
<span th:if="${#lists.isEmpty(list)}">Liste vide</span>
<span th:text="${#lists.first(list)}"></span>
<span th:text="${#lists.last(list)}"></span>
<span th:if="${#lists.contains(list, item)}"></span>

<span th:text="${#maps.size(map)}"></span>
<span th:if="${#maps.containsKey(map, 'key')}"></span>

<span th:text="${#sets.size(set)}"></span>
```

---

## Ressources supplémentaires

- **Documentation officielle**: http://www.thymeleaf.org
- **Spring Boot + Thymeleaf**: https://spring.io/projects/spring-boot
- **Thymeleaf Expressions**: http://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html
- **Spring Security + Thymeleaf**: https://www.thymeleaf.org/doc/articles/springsecurity.html

---

## Résumé des points clés

✅ **À retenir**:
- Thymeleaf s'intègre parfaitement avec Spring MVC
- Les attributs `th:*` rendent les templates valides en HTML
- Utiliser `${}` pour accéder aux variables du modèle
- `@{}` pour les URLs avec paramètres
- `*{}` avec `th:object` pour simplifier l'accès aux propriétés
- Les fragments permettent la réutilisation du code
- Toujours valider et échapper les entrées utilisateur

❌ **À éviter**:
- Utiliser `th:utext` sans confiance
- Logique métier complexe dans les templates
- Appels coûteux dans les boucles
- Oublier le namespace `xmlns:th="http://www.thymeleaf.org"`
