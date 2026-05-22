# Guide Complet: Utilisation des Services du Projet Forage

Ce document explique **comment utiliser chaque fonction** de tous les services du projet, avec des exemples pratiques et détaillés.

---

## 📚 Table des matières

1. [ClientService](#clientservice)
2. [DemandeService](#demandeservice)
3. [DemandeStatusService](#demandestatusservice)
4. [DevisService](#devisservice)
5. [DetailsDevisService](#detailsdevisservice)
6. [StatusService](#statusservice)
7. [TypeDevisService](#typedevisservice)

---

## ClientService

Le service pour gérer les **clients** (Create, Read, Update, Delete).

### Injection du Service

```java
@Controller
@RequiredArgsConstructor  // Injecte les dépendances automatiquement
public class MaController {
    
    private final ClientService clientService;
}
```

### 1. `findAll()` - Récupérer tous les clients

**Explication:** Cette fonction retourne la liste complète de TOUS les clients enregistrés dans la base de données. Utile pour afficher un tableau de tous les clients ou faire des statistiques.

**Signature:**
```java
public List<Client> findAll()
```

**Utilisation:**
```java
// Récupérer la liste de TOUS les clients
List<Client> tousLesClients = clientService.findAll();

// Afficher le nombre de clients
System.out.println("Nombre de clients: " + tousLesClients.size());

// Utiliser dans un template
model.addAttribute("clients", tousLesClients);

// Boucler sur les clients
for (Client client : tousLesClients) {
    System.out.println("Client: " + client.getNom() + " - Contact: " + client.getContact());
}
```

**Exemple dans un Controller:**
```java
@GetMapping("/clients")
public String listClients(Model model) {
    List<Client> clients = clientService.findAll();
    model.addAttribute("clients", clients);
    return "client/list";  // client/list.html affiche la liste
}
```

**Template Thymeleaf:**
```html
<table>
    <tr th:each="client : ${clients}">
        <td th:text="${client.nom}"></td>
        <td th:text="${client.contact}"></td>
    </tr>
</table>
```

---

### 2. `findById(int id)` - Récupérer un client par son ID

**Explication:** Cherche UN client spécifique via son ID. Lève une exception si le client n'existe pas. Utilisé pour afficher/éditer les détails d'un client particulier.

**Signature:**
```java
public Client findById(int id) throws RuntimeException
```

**Utilisation:**
```java
// Récupérer UN client par son ID
Client client = clientService.findById(5);
System.out.println("Nom: " + client.getNom());
System.out.println("Contact: " + client.getContact());

// Utiliser les propriétés du client
String nomClient = client.getNom();
List<Demande> demandesClient = client.getDemandes();  // Récupérer ses demandes

// Afficher dans un template
model.addAttribute("client", client);
```

**Exemple dans un Controller:**
```java
@GetMapping("/client/{id}")
public String viewClient(@PathVariable int id, Model model) {
    Client client = clientService.findById(id);  // Peut lancer RuntimeException
    model.addAttribute("client", client);
    return "client/view";
}

@GetMapping("/client/{id}")
public String viewClientAvecGestionErreur(@PathVariable int id, Model model) {
    try {
        Client client = clientService.findById(id);
        model.addAttribute("client", client);
        return "client/view";
    } catch (RuntimeException e) {
        model.addAttribute("error", "Client non trouvé");
        return "redirect:/clients";
    }
}
```

**Erreur possible:**
```
RuntimeException: Client non trouvé: 5
```

---

##Explication:** Enregistre un client nouveau ou existant dans la base de données. Si le client n'a pas d'ID, il sera créé; s'il a un ID, il sera modifié. Retourne le client avec son ID attribué.

**# 3. `save(Client client)` - Créer ou modifier un client

**Signature:**
```java
public Client save(Client client)
```

**Utilisation:**
```java
// Créer un nouveau client
Client nouveauClient = new Client();
nouveauClient.setNom("Rakoto Jean");
nouveauClient.setContact("rakoto@email.com");

Client client_cree = clientService.save(nouveauClient);
System.out.println("Client créé avec l'ID: " + client_cree.getId());

// Modifier un client existant
Client client = clientService.findById(5);
client.setNom("Rakoto Jean Modifié");
client.setContact("nouveau@email.com");

Client client_modifie = clientService.save(client);  // Mise à jour
System.out.println("Client modifié: " + client_modifie.getNom());
```

**Exemple dans un Controller (Création):**
```java
@GetMapping("/client/new")
public String createForm(Model model) {
    model.addAttribute("client", new Client());
    return "client/form";
}

@PostMapping("/client/save")
public String save(@Valid @ModelAttribute Client client, 
                   BindingResult result) {
    if (result.hasErrors()) {
        return "client/form";  // Retour avec erreurs
    }
    
    Client saved = clientService.save(client);
    return "redirect:/clients/" + saved.getId();
}
```

**Exemple dans un Controller (Modification):**
```java
@GetMapping("/client/{id}/edit")
public String editForm(@PathVariable int id, Model model) {
    Client client = clientService.findById(id);
    model.addAttribute("client", client);
    return "client/form";  // Même formulaire que création
}

@PostMapping("/client/save")
public String save(@Valid @ModelAttribute Client client, 
                   BindingResult result) {
    if (result.hasErrors()) {
        return "client/form";
    }
    
    clientService.save(client);  // Si client.id existe, c'est une update
    return "redirect:/clients";
}
```

--Explication:** Supprime UN client de la base de données en utilisant son ID. Attention: Cela peut causer une erreur s'il y a des demandes liées à ce client (dépendances).

**-

### 4. `deleteById(int id)` - Supprimer un client

**Signature:**
```java
public void deleteById(int id)
```

**Utilisation:**
```java
// Supprimer un client
int idASupprimer = 5;
clientService.deleteById(idASupprimer);
System.out.println("Client supprimé");

// Supprimer après avoir vérifié son existence
try {
    Client client = clientService.findById(5);
    clientService.deleteById(client.getId());
    System.out.println("Client supprimé avec succès");
} catch (RuntimeException e) {
    System.out.println("Client non trouvé");
}
```

**Exemple dans un Controller:**
```java
@GetMapping("/client/{id}/delete")
public String delete(@PathVariable int id) {
    try {
        clientService.deleteById(id);
        return "redirect:/clients?success=Client supprimé";
    } catch (Exception e) {
        return "redirect:/clients?error=Erreur lors de la suppression";
    }
}

// Avec confirmation
@PostMapping("/client/{id}/delete")
public String deleteConfirmed(@PathVariable int id) {
    clientService.deleteById(id);
    return "redirect:/clients";
}
```

--Explication:** Cherche tous les clients dont le nom CONTIENT la chaîne de caractères fournie (non sensible à la casse: "rao", "RAO" donnent le même résultat). Utile pour les barres de recherche.

**-

### 5. `search(String nom)` - Chercher les clients par nom

**Signature:**
```java
public List<Client> search(String nom)
```

**Utilisation:**
```java
// Chercher les clients dont le nom contient "Rao"
List<Client> resultats = clientService.search("Rao");
System.out.println("Résultats trouvés: " + resultats.size());

// Afficher les résultats
for (Client client : resultats) {
    System.out.println(client.getNom());
}

// Chercher sensible à la casse? NON, utilise IgnoreCase
// "rao", "RAO", "Rao" -> même résultat
List<Client> memes_resultats = clientService.search("RAO");
```

**Exemple dans un Controller:**
```java
@GetMapping("/clients")
public String listClients(@RequestParam(required = false) String search, 
                          Model model) {
    List<Client> clients;
    
    if (search != null && !search.isEmpty()) {
        clients = clientService.search(search);
        model.addAttribute("search", search);
    } else {
        clients = clientService.findAll();
    }
    
    model.addAttribute("clients", clients);
    return "client/list";
}
```

**Template HTML avec formulaire:**
```html
<form method="get" action="/clients">
    <input type="text" name="search" placeholder="Rechercher par nom...">
    <button type="submit">Rechercher</button>
</form>

<table>
    <tr th:each="client : ${clients}">
        <td th:text="${client.nom}"></td>
    </tr>
</table>
```

---

## DemandeService

Le service pour gérer les **demandes** avec états (statuts).

### 1. `findAll()` - Récupérer toutes les demandes avec détails

**Explication:** Retourne TOUTES les demandes de la base de données avec les informations complètes (client, statut, etc.). Utilisé pour afficher la liste complète des demandes.

**Signature:**
```java
public List<Demande> findAll()
```

**Utilisation:**
```java
// Récupérer TOUTES les demandes (avec client et statut associés)
List<Demande> toutesDemandesdemandes = demandeService.findAll();

// Accéder aux informations
for (Demande demande : toutesDemandesdemandes) {
    System.out.println("Demande ID: " + demande.getId());
    System.out.println("Client: " + demande.getClient().getNom());
    System.out.println("Description: " + demande.getDescription());
}
```

---

### 2. `findById(int id)` - Récupérer une demande avec TOUS ses détails

**Explication:** Cherche UNE demande complète (avec client, historique des statuts, etc.) par son ID. Lève une exception si la demande n'existe pas.

**Signature:**
```java
public Demande findById(int id) throws RuntimeException
```

**Utilisation:**
```java
// Récupérer une demande avec tous ses détails (statuts, etc.)
Demande demande = demandeService.findById(10);

// Accéder aux informations complètes
String descriptionDemande = demande.getDescription();
Client clientDemande = demande.getClient();
List<DemandeStatus> historiqueStatuts = demande.getStatuts();  // Historique complet

// Afficher les statuts de la demande
for (DemandeStatus status : historiqueStatuts) {
    System.out.println(status.getStatus().getNom() + " - " + status.getDateStatus());
}
```

---

##Explication:** Récupère une demande minimale (juste la demande + client) sans charger tout l'historique des statuts. Plus rapide que findById(), utilisé quand on veut juste éditer la demande.

**# 3. `findByIdSimple(int id)` - Récupérer une demande légère (pour édition rapide)

**Signature:**
```java
public Demande findByIdSimple(int id) throws RuntimeException
```

**Utilisation:**
```java
// Récupérer une demande légère (juste demande + client, pas tout l'historique)
Demande demande = demandeService.findByIdSimple(10);  // Plus rapide

// Utilisé quand on veut modifier rapidement, pas afficher l'historique
model.addAttribute("demande", demande);  // Pour un formulaire d'édition
```

--Explication:** Enregistre une demande SANS attribuer de statut initial. À utiliser avec prudence car la demande n'aura pas d'état. Préférer `creerAvecStatusInitial()` ou `creerAvecStatusParDefaut()` pour la création.

**-

### 4. `save(Demande demande)` - Enregistrer une demande simple

**Signature:**
```java
public Demande save(Demande demande)
```

**Utilisation:**
```java
// Créer une nouvelle demande SANS statut initial
Demande demande = new Demande();
demande.setDescription("Demande de forage pour puits");
demande.setClient(client);  // Doit avoir un client

Demande demandeSaved = demandeService.save(demande);
System.out.println("Demande créée ID: " + demandeSaved.getId());

// ⚠️ ATTENTION: Il faudra ajouter un statut manuellement après!
``Explication:** Crée une demande ET attribue immédiatement un statut spécifique. C'est la bonne méthode pour créer une demande correctement avec un état initial choisi.

**`

---

### 5. `creerAvecStatusInitial(Demande demande, int statusId)` - Créer avec statut

**Signature:**
```java
public Demande creerAvecStatusInitial(Demande demande, int statusId)
```

**Utilisation:**
```java
// Créer une demande ET attribuer un statut initial
Demande demande = new Demande();
demande.setDescription("Forage classique");
demande.setClient(clientService.findById(5));

int statusPendingId = 1;  // ID du statut "En attente" ou "Nouveau"
Demande demandeCreee = demandeService.creerAvecStatusInitial(demande, statusPendingId);

// La demande est créée ET a déjà un statut initial
System.out.println("Demande créée avec statut initial");
```

**Exemple dans un Controller:**
```java
@PostMapping("/demande/save")
public String save(@Valid @ModelAttribute Demande demande, 
                   BindingResult result,
                   @RequestParam int statusId) {
    if (result.hasErrors()) {
        return "demande/form";
    }
    
    // Créer la demande avec statut initial
    demandeService.creerAvecStatusInitial(demande, statusId);
  Explication:** Crée une demande avec le PREMIER statut disponible dans la base (statut par défaut). Plus simple que creerAvecStatusInitial() quand on n'a pas besoin de choisir le statut.

**  return "redirect:/demandes";
}
```

---

### 6. `creerAvecStatusParDefaut(Demande demande)` - Créer avec statut par défaut

**Signature:**
```java
public Demande creerAvecStatusParDefaut(Demande demande)
```

**Utilisation:**
```java
// Créer une demande avec le PREMIER statut disponible (par défaut)
Demande demande = new Demande();
demande.setDescription("Nouvelle demande rapide");
demande.setClient(clientService.findById(3));
Explication:** Change le statut d'une demande et ajoute un commentaire explicatif. Crée automatiquement une entrée dans l'historique avec la date/heure du changement.

**
// Ne pas besoin de spécifier le statut, il prend le premier
Demande demandeCreee = demandeService.creerAvecStatusParDefaut(demande);
System.out.println("Demande créée avec statut par défaut");
```

---

### 7. `changerStatus(int demandeId, int statusId, String commentaire)` - Changer le statut

**Signature:**
```java
public void changerStatus(int demandeId, int statusId, String commentaire)
```

**Utilisation:**
```java
// Changer le statut d'une demande
int demandeId = 10;
int nouveauStatusId = 3;  // Nouveau statut
String commentaire = "Forage approuvé et commencé";

demandeService.changerStatus(demandeId, nouveauStatusId, commentaire);
System.out.println("Statut changé");

// Un nouvel historique (DemandeStatus) a été créé automatiquement
```

**Exemple de workflow:**
```java
// Étape 1: Client crée une demande
Demande demande = new Demande();
demande.setDescription("Forage puits");
demande.setClient(client);
Demande d = demandeService.creerAvecStatusParDefaut(demande);  // Statut = Nouveau

// Étape 2: Admin valide la demande
demandeService.changerStatus(d.getId(), STATUS_VALIDE_ID, "Demande validée");
Explication:** Retourne TOUS les changements de statuts d'une demande, du plus ancien au plus récent, avec les dates et commentaires. Utile pour tracer l'évolution d'une demande.

**
// Étape 3: Forage commence
demandeService.changerStatus(d.getId(), STATUS_EN_COURS_ID, "Forage commencé");

// Étape 4: Forage terminé
demandeService.changerStatus(d.getId(), STATUS_TERMINE_ID, "Forage terminé - Débit: 500L/h");
```

---

### 8. `getHistoriqueStatus(int demandeId)` - Récupérer l'historique des statuts

**Signature:**
```java
public List<DemandeStatus> getHistoriqueStatus(int demandeId)
```

**Utilisation:**
```java
// Obtenir TOUS les changements de statut d'une demande
List<DemandeStatus> historique = demandeService.getHistoriqueStatus(10);

// Afficher l'historique
for (DemandeStatus ds : historique) {
    System.out.println(ds.getDateStatus() + " - " + ds.getStatus().getNom());
    System.out.println("Commentaire: " + ds.getCommentaire());
}

// Afficher dans un template
model.addAttribute("historique", historique);
```

**Template HTML pour afficher l'historique:**
```html
<h3>Historique des statuts</h3>
<ul>
    <li th:each="status : ${historique}">
        <strong th:text="${status.status.nom}"></strong>
        (<span th:text="${#dates.format(status.dateStatus, 'dd/MM/yyyy HH:mm')}"></span>)
        <p th:text="${status.commentaire}"></p>
    </li>
</ul>
```

---

## DemandeStatusService

Le service pour gérer l'**historique et les statuts des demandes**.

### 1. `getHistorique(int demandeId)` - Obtenir l'historique trié

**Explication:** Retourne tous les statuts d'une demande, TRIÉS du plus récent au plus ancien (ordre décroissant). Parfait pour afficher l'historique dans une interface utilisateur.

**Signature:**
```java
public List<DemandeStatus> getHistorique(int demandeId)
```

**Utilisation:**
```java
// Récupérer l'historique des statuts (du plus récent au plus ancien)
List<DemandeStatus> historique = demandeStatusService.getHistorique(10);

// Afficher du plus récent au plus ancien
for (DemandeStatus ds : historique) {
    System.out.println(ds.getStatus().getNom() + " - " + ds.getDateStatus());
}
```

---

### 2. `getDernierStatus(int demandeId)` - Obtenir le DERNIER statut

**Explication:** Retourne seulement le DERNIER statut d'une demande (le changement le plus récent). Retourne Optional car il peut n'y avoir aucun statut. Utilisé pour afficher l'état actuel d'une demande.

**Signature:**
```java
public Optional<DemandeStatus> getDernierStatus(int demandeId)
```

**Utilisation:**
```java
// Récupérer le DERNIER statut (le plus récent)
Optional<DemandeStatus> dernierStatusOpt = demandeStatusService.getDernierStatus(10);

if (dernierStatusOpt.isPresent()) {
    DemandeStatus dernierStatus = dernierStatusOpt.get();
    System.out.println("Dernier statut: " + dernierStatus.getStatus().getNom());
} else {
    System.out.println("Aucun statut trouvé");
}

// Ou en une ligne
String dernier = demandeStatusService.getDernierStatus(10)
    .map(ds -> ds.getStatus().getNom())
    .orElse("Inconnu");
System.out.println("Statut actuel: " + dernier);
```

---

##Explication:** Ajoute UN NOUVEAU statut à l'historique d'une demande sans modifier l'état global. Retourne le nouvel objet DemandeStatus créé avec la date/heure actuelle.

**# 3. `ajouterStatus(int demandeId, int statusId, String commentaire)` - Ajouter un statut

**Signature:**
```java
public DemandeStatus ajouterStatus(int demandeId, int statusId, String commentaire)
```

**Utilisation:**
```java
// Ajouter UN nouveau statut à l'historique
DemandeStatus nouvea uStatus = demandeStatusService.ajouterStatus(
    10,  // Demande ID
    3,   // Status ID
    "Travaux commencés ce jour"  // Commentaire
);

System.out.println("Statut ajouté le: " + nouveauStatus.getDateStatus());
```

**Différence avec `demandeService.changerStatus()`:**
- `demandeStatusService.ajouterStatus()` → Ajouter juste le statut
- `demandeService.changerStatus()` → Ajouter le statut + gérer les relations

```java
// Utiliser dans le service métier
public void approuverDemande(int demandeId) {
    demandeStatusService.ajouterStatus(
        demandeId, 
        STATUS_VALIDE_ID, 
        "Demande approuvée par administrateur"
    );
}
```

--Explication:** Retourne le NOMBRE TOTAL de changements de statut d'une demande (0 si aucun changement). Utile pour identifier les demandes qui traînent ou qui sont compliquées.

**-

### 4. `nombreChangements(int demandeId)` - Compter les changements de statut

**Signature:**
```java
public int nombreChangements(int demandeId)
```

**Utilisation:**
```java
// Combien de fois le statut a changé?
int nombreChangements = demandeStatusService.nombreChangements(10);
System.out.println("Nombre de changements: " + nombreChangements);

// Utilité: Voir si une demande a traîné longtemps
if (nombreChangements < 2) {
    System.out.println("Demande rapide");
} else if (nombreChangements < 5) {
    System.out.println("Demande normale");
} else {
    System.out.println("Demande compliquée ou traînante");
}
```

---

##Explication:** Retourne la liste COMPLÈTE de tous les devis avec toutes leurs informations et détails. Utilisé pour afficher le tableau de bord des devis.

** DevisService

Le service pour gérer les **devis** (offres de prix).

### 1. `findAll()` - Récupérer tous les devis

**Signature:**
```java
public List<Devis> findAll()
```

**Utilisation:**
```java
// Récupérer TOUS les devis (avec détails associés)
List<Devis> tousDevis = devisService.findAll();

// Afficher les informations
for (Devis devis : tousDevis) {
    System.out.println("Devis ID: " + devis.getId());
    System.out.println("Date: " + devis.getDateDevis());
    System.out.println("Montant: " + devis.getMontantTotal() + " Ar");
    System.out.println("Demande: " + devis.getDemande().getId());
}

//Explication:** Cherche UN devis spécifique avec TOUTES ses informations (demande, client, détails, montants). Utile pour afficher ou éditer un devis particulier.

** Afficher dans un template
model.addAttribute("devis", tousDevis);
```

---

### 2. `findById(int id)` - Récupérer UN devis avec ses détails

**Signature:**
```java
public Devis findById(int id)
```

**Utilisation:**
```java
// Récupérer UN devis avec TOUS ses détails
Devis devis = devisService.findById(5);

// Accéder aux informations
System.out.println("Date devis: " + devis.getDateDevis());
System.out.println("Montant total: " + devis.getMontantTotal());

// Accéder à la demande associée
Demande demande = devis.getDemande();
System.out.println("Demande liée: " + demande.getDescription());

// Accéder au client via la demande
Client client = demande.getClient();
System.out.println("Client: " + client.getNom());

// Accéder au type de devis
TypeDevis typeDevis = devis.getTypeDevis();
SyExplication:** Retourne les LIGNES détaillées d'un devis (chaque ligne = une tâche/service avec quantité et prix). Utilisé pour afficher le détail des prestations facturées.

**stem.out.println("Type: " + typeDevis.getNom());

// Afficher dans un template
model.addAttribute("devis", devis);
```

---

### 3. `getDetails(int devisId)` - Récupérer les détails d'un devis

**Signature:**
```java
public List<DetailsDevis> getDetails(int devisId)
```

**Utilisation:**
```java
// Récupérer TOUS les détails (lignes) d'un devis
// Ex: Devis = Forage à 1000 Ar/m. Détails = Profondeur 100m (100 Ar), prêtage 50m (50 Ar), etc.
List<DetailsDevis> details = devisService.getDetails(5);

// Afficher chaque détail
for (DetailsDevis detail : details) {
    System.out.println("Description: " + detail.getDescription());
    System.out.println("Quantité: " + detail.getQuantite());
    System.out.println("Prix unitaire: " + detail.getPrixUnitaire() + " Ar");
    System.out.println("Total: " + detail.getTotal() + " Ar");
}

// Afficher dans un template
model.addAttribute("details", details);
```

**Template HTML:**
```html
<table>
    <thead>
        <tr>
            <th>Description</th>
            <th>Quantité</th>
            <th>Prix unitaire</th>
            <th>Total</th>
        </tr>
    </thead>
    <tbody>
        <tr th:each="detail : ${details}">
            <td th:text="${detail.description}"></td>
  Explication:** Retourne toutes les demandes qui N'ONT PAS encore de devis associé. Utilisé pour les dropdowns/autocomplete lors de la création d'un nouveau devis.

**          <td th:text="${detail.quantite}"></td>
            <td th:text="${detail.prixUnitaire} + ' Ar'"></td>
            <td th:text="${detail.total} + ' Ar'"></td>
        </tr>
    </tbody>
</table>
```

---

### 4. `getDemandesSansDevis()` - Obtenir les demandes sans devis

**Signature:**
```java
public List<Demande> getDemandesSansDevis()
```

**Utilisation:**
```java
// Récupérer TOUTES les demandes qui N'ONT PAS de devis
List<Demande> demandesSansDevis = devisService.getDemandesSansDevis();

// Afficher les demandes disponibles
for (Demande demande : demandesSansDevis) {
    System.out.println("Demande ID: " + demande.getId());
    System.out.println("Description: " + demande.getDescription());
}

// Utilité: Pour un formulaire de création de devis (dropdown)
```

**Template HTML - Dropdown pour créer un devis:**
```html
<form>
  Explication:** Crée un devis COMPLET (demande + type + détails) et change le statut de la demande. C'est la méthode recommandée pour créer un devis correctement.

**  <select name="demandeId">
        <option value="">Choisir une demande</option>
        <option th:each="demande : ${demandesSansDevis}" 
                th:value="${demande.id}"
                th:text="${demande.id} + ' - ' + ${demande.description}">
        </option>
    </select>
</form>
```

---

### 5. `creerDevis(...)` - Créer un devis COMPLET

**Signature:**
```java
public Devis creerDevis(int demandeId, int typeDevisId, Integer statusId,
                        List<DetailsDevis> details)
```

**Utilisation (étape par étape):**

```java
// Étape 1: Préparer les détails
List<DetailsDevis> details = new ArrayList<>();

// Détail 1
DetailsDevis detail1 = new DetailsDevis();
detail1.setDescription("Forage 0-100m");
detail1.setQuantite(100);
detail1.setPrixUnitaire(1000);  // 100 * 1000 = 100,000 Ar

// Détail 2
DetailsDevis detail2 = new DetailsDevis();
detail2.setDescription("Installation pompe");
detail2.setQuantite(1);
detail2.setPrixUnitaire(50000);  // 1 * 50,000 = 50,000 Ar

details.add(detail1);
details.add(detail2);

// Étape 2: Créer le devis
int demandeId = 10;      // Quelle demande?
int typeDevisId = 1;     // Quel type? (Classique, Premium, etc.)
Integer statusId = 2;    // Quel statut attribuer à la demande? (null = pas de changement)

Devis devisCreated = devisService.creerDevis(
    demandeId, 
    typeDevisId, 
    statusId, 
    details
);

// Résultat
System.out.println("Devis créé ID: " + devisCreated.getId());
System.out.println("Montant total: " + devisCreated.getMontantTotal() + " Ar");  // 150,000 Ar
```

**Exemple dans un Controller:**
```java
@PostMapping("/devis/save")
public String createDevis(@RequestParam int demandeId,
                          @RequestParam int typeDevisId,
                          @RequestParam(required = false) Integer statusId,
                          @RequestParam String[] descriptions,
                          @RequestParam double[] quantites,
                          @RequestParam double[] prixUnitaires) {
    
    // Construire la liste des détails
    List<DetailsDevis> details = new ArrayList<>();
    for (int i = 0; i < descriptions.length; i++) {
        DetailsDevis detail = new DetailsDevis();
        detail.setDescription(descriptions[i]);
  Explication:** Met à jour un devis existant: type, détails et montant total recalculé. Peut aussi changer le statut de la demande associée. Les anciens détails sont supprimés et remplacés.

**      detail.setQuantite(quantites[i]);
        detail.setPrixUnitaire(prixUnitaires[i]);
        details.add(detail);
    }
    
    // Créer le devis
    devisService.creerDevis(demandeId, typeDevisId, statusId, details);
    
    return "redirect:/devis";
}
```

---

### 6. `updateDevis(...)` - Modifier un devis

**Signature:**
```java
public Devis updateDevis(int devisId, int typeDevisId, Integer statusId,
                         List<DetailsDevis> details)
```

**Utilisation:**
```java
// Modifier un devis existant
List<DetailsDevis> nouvellesDetails = new ArrayList<>();

// Ajouter les nouveaux détails
DetailsDevis detail1 = new DetailsDevis();
detail1.setDescription("Forage 0-100m - MODIFIÉ");
detail1.setQuantite(100);
detail1.setPrixUnitaire(1200);  // Prix augmenté

nouvellesDetails.add(detail1);

// Mettre à jour le devis
inExplication:** Supprime UN devis ET TOUS ses détails automatiquement (suppression en cascade). Utile si un devis doit être annulé.

**t devisId = 5;
int typeDevisId = 2;  // Changer le type?
Integer statusId = 3;  // Changer le statut de la demande?

Devis devisModified = devisService.updateDevis(
    devisId, 
    typeDevisId, 
    statusId, 
    nouvellesDetails
);

System.out.println("Devis modifié - Nouveau montant: " + devisModified.getMontantTotal());
```

---

### 7. `deleteById(int id)` - Supprimer un devis

**Signature:**
```java
public void deleteById(int id)
```

**Utilisation:**
```java
// Supprimer UN devis (supprime aussi ses détails automatiquement)
int devisId = 5;
devisService.deleteById(devisId);
System.out.println("Devis supprimé");

// Important: Cela supprime AUSSI tous les DetailsDevis associés (cascade)
```

**Exemple dans un Controller:**
``Explication:** Récupère UNE LIGNE de détail de devis par son ID (prix unitaire, quantité, description). Utilisé rarement, généralement on utilise `devisService.getDetails()` pour avoir tous les détails à la fois.

**`java
@GetMapping("/devis/{id}/delete")
public String deleteDevis(@PathVariable int id) {
    try {
        devisService.deleteById(id);
        return "redirect:/devis?success=Devis supprimé";
    } catch (Exception e) {
        return "redirect:/devis?error=Erreur";
    }
}
```

---

## DetailsDevisService

Le service pour gérer les **détails des devis** (lignes individuelles).

### 1. `findById(int id)` - Récupérer UN détail

**Signature:**
```java
puExplication:** Retourne la liste COMPLÈTE de tous les statuts disponibles (Ex: Nouveau, Validé, En cours, Terminé, etc.). Utilisé pour les dropdowns et sélecteurs dans les formulaires.

**blic DetailsDevis findById(int id)
```

**Utilisation:**
```java
// Récupérer UN détail de devis
DetailsDevis detail = detailsDevisService.findById(7);

System.out.println("Description: " + detail.getDescription());
System.out.println("Quantité: " + detail.getQuantite());
System.out.println("Prix unitaire: " + detail.getPrixUnitaire());
System.out.println("Total: " + detail.getTotal());  // Quantité * Prix unitaire
```

---

## StatusService

Le service pour gérer les **statuts** (états des demandes).

### 1. `getAllStatus()` ou `findAll()` - Récupérer tous les statuts

**Signature:**
```java
public List<Status> getAllStatus()
public List<Status> findAll()
```

**Utilisation:**
```java
// Récupérer TOUS les statuts disponibles
List<Status> tousStatuts = statusService.getAllStatus();

// Afficher tous les statuts
for (Status status : tousStatuts) {
  Explication:** Cherche UN statut spécifique (Ex: ID 1 = "Nouveau"). Utile pour afficher le nom d'un statut quand on a seulement son ID.

**  System.out.println("ID: " + status.getId());
    System.out.println("Nom: " + status.getNom());
    System.out.println("Description: " + status.getDescription());
}

// Afficher dans un template (dropdown)
model.addAttribute("statuts", tousStatuts);
```

**Template HTML - Dropdown de statuts:**
```html
<select name="statusId">
    <option value="">Choisir un statut</option>
    <option th:each="status : ${statuts}" 
            th:value="${status.id}"
            th:text="${status.nom}">
    </option>
</select>
```

---

### 2. `getStatusById(int id)` - Récupérer UN statut par ID

**Signature:**
```java
public Status getStatusById(int id)
```

**Utilisation:**
```java
//Explication:** Retourne la liste de tous les TYPES de devis disponibles. Chaque type peut avoir des caractéristiques/services différents. Utilisé pour les dropdowns lors de la création d'un devis.

** Récupérer UN statut spécifique
Status statusValide = statusService.getStatusById(2);

System.out.println("Statut: " + statusValide.getNom());
System.out.println("Description: " + statusValide.getDescription());

// Afficher le nom du statut dans un template
model.addAttribute("status", statusValide);
```

**Template HTML:**
```html
<p>Statut actuel: <span th:text="${status.nom}"></span></p>
<p>Description: <span th:text="${status.description}"></span></p>
```

---

## TypeDevisService

Le service pour gérer les **types de devis** (Classique, Premium, VIP, etc.).

### 1. `findAll()` - Récupérer tous les types

**Signature:**
```java
public List<TypeDevis> findAll()
```

**Utilisation:**
```java
// Récupérer TOUS les types de devis
LiExplication:** Cherche UN type de devis spécifique (Ex: ID 1 = "Classique"). Utile pour afficher le type d'un devis ou pour valider qu'un type existe avant de l'utiliser.

**st<TypeDevis> tousTypes = typeDevisService.findAll();

// Afficher
for (TypeDevis type : tousTypes) {
    System.out.println("Type: " + type.getNom());
    System.out.println("Description: " + type.getDescription());
}

// Afficher dans un template
model.addAttribute("types", tousTypes);
```

**Template HTML - Dropdown:**
```html
<select name="typeDevisId">
    <option th:each="type : ${types}" 
            th:value="${type.id}"
            th:text="${type.nom}">
    </option>
</select>
```
Explication:** Enregistre un type de devis nouveau ou existant. Si pas d'ID = création, sinon modification. Utilisé pour gérer les types disponibles (admin).

**
---

### 2. `findById(int id)` - Récupérer UN type par ID

**Signature:**
```java
public TypeDevis findById(int id)
```

**Utilisation:**
```java
// Récupérer UN type de devis
TypeDevis typeClassique = typeDevisService.findById(1);

System.out.println("Nom: " + typeClassique.getNom());
System.out.println("Description: " + typeClassique.getDescription());

// Afficher dans un template
model.addAttribute("typeDevis", typeClassique);
```

---
Explication:** Supprime UN type de devis. À utiliser avec prudence: vérifier d'abord qu'aucun devis n'utilise ce type, sinon cela peut causer des erreurs.

**
### 3. `save(TypeDevis typeDevis)` - Créer ou modifier un type

**Signature:**
```java
public TypeDevis save(TypeDevis typeDevis)
```

**Utilisation:**
```java
// Créer un nouveau type
TypeDevis nouveauType = new TypeDevis();
nouveauType.setNom("Premium Plus");
nouveauType.setDescription("Devis pour forages ultra profonds");

TypeDevis typeSave = typeDevisService.save(nouveauType);
System.out.println("Type créé ID: " + typeSave.getId());

// Modifier un type existant
TypeDevis typeExistant = typeDevisService.findById(1);
typeExistant.setNom("Classique Modifié");
typeDevisService.save(typeExistant);
```

---

### 4. `deleteById(int id)` - Supprimer un type

**Signature:**
```java
public void deleteById(int id)
```

**Utilisation:**
```java
// Supprimer UN type de devis
typeDevisService.deleteById(1);
System.out.println("Type supprimé");

// ⚠️ Attention: Vérifier qu'aucun devis n'utilise ce type avant de supprimer
```

---

## Résumé des Services et leurs Fonctionnalités

| Service | Fonction | Utilité |
|---------|----------|---------|
| **ClientService** | findAll() | Lister tous les clients |
| | findById(id) | Récupérer 1 client |
| | save(client) | Créer/Modifier un client |
| | deleteById(id) | Supprimer un client |
| | search(nom) | Chercher clients par nom |
| **DemandeService** | findAll() | Lister avant toutes les demandes |
| | findById(id) | Récupérer 1 demande complète |
| | save(demande) | Créer/modifier simple |
| | creerAvecStatusInitial(...) | Créer avec statut |
| | changerStatus(...) | Changer le statut |
| | getHistoriqueStatus(...) | Voir l'historique des statuts |
| **DemandeStatusService** | getHistorique(...) | Lister tous les statuts |
| | getDernierStatus(...) | Récupérer le dernier statut |
| | ajouterStatus(...) | Ajouter un statut à l'historique |
| | nombreChangements(...) | Compter les changements |
| **DevisService** | findAll() | Lister tous les devis |
| | findById(id) | Récupérer 1 devis |
| | getDetails(...) | Lister les détails d'un devis |
| | getDemandesSansDevis() | Demandes sans devis (autocomplete) |
| | creerDevis(...) | Créer un devis complet |
| | updateDevis(...) | Modifier un devis |
| | deleteById(id) | Supprimer un devis |
| **StatusService** | findAll() / getAllStatus() | Lister tous les statuts |
| | getStatusById(id) | Récupérer 1 statut |
| **TypeDevisService** | findAll() | Lister tous les types |
| | findById(id) | Récupérer 1 type |
| | save(...) | Créer/modifier un type |
| | deleteById(id) | Supprimer un type |
| **DetailsDevisService** | findById(id) | Récupérer 1 détail |

---

## Workflow Complet: Exemple Réel

Voici un **exemple complet** du workflow d'une demande de forage jusqu'au devis:

```java
@Service
@RequiredArgsConstructor
public class ForageWorkflowService {
    
    private final ClientService clientService;
    private final DemandeService demandeService;
    private final DevisService devisService;
    private final StatusService statusService;
    
    /**
     * Workflow complet: Du client au devis
     */
    public void creerCommandeForage() {
        // 1️⃣ Créer/Récupérer le client
        List<Client> clients = clientService.findAll();
        Client client = clients.get(0);  // Prendre le premier client
        
        // 2️⃣ Créer une demande pour ce client
        Demande demande = new Demande();
        demande.setDescription("Forage puits privé 150m");
        demande.setClient(client);
        
        // Créer la demande avec statut initial "Nouveau"
        Demande demandeSaved = demandeService.creerAvecStatusParDefaut(demande);
        System.out.println("✅ Demande créée: " + demandeSaved.getId());
        
        // 3️⃣ Admin valide la demande
        List<Status> statuts = statusService.findAll();
        Status statusValide = statuts.get(1);  // 2ème statut = Validé
        demandeService.changerStatus(demandeSaved.getId(), statusValide.getId(), 
                                     "Demande validée par admin");
        System.out.println("✅ Demande validée");
        
        // 4️⃣ Créer un devis pour cette demande
        List<DetailsDevis> details = new ArrayList<>();
        
        DetailsDevis detail1 = new DetailsDevis();
        detail1.setDescription("Forage 0-100m");
        detail1.setQuantite(100);
        detail1.setPrixUnitaire(1000);  // 100,000 Ar
        details.add(detail1);
        
        DetailsDevis detail2 = new DetailsDevis();
        detail2.setDescription("Tuyauterie");
        detail2.setQuantite(1);
        detail2.setPrixUnitaire(50000);  // 50,000 Ar
        details.add(detail2);
        
        // Créer devis (type 1 = Classique, status 2 = Accepté)
        Devis devis = devisService.creerDevis(
            demandeSaved.getId(), 
            1,  // TypeDevis ID
            statusValide.getId(),
            details
        );
        System.out.println("✅ Devis créé: " + devis.getId());
        System.out.println("💰 Montant total: " + devis.getMontantTotal() + " Ar");
        
        // 5️⃣ Récupérer le devis et l'afficher
        Devis devisAffiche = devisService.findById(devis.getId());
        List<DetailsDevis> detailsAffichage = devisService.getDetails(devis.getId());
        
        System.out.println("\n=== DEVIS DE FORAGE ===");
        System.out.println("Client: " + devisAffiche.getDemande().getClient().getNom());
        System.out.println("Date: " + devisAffiche.getDateDevis());
        System.out.println("\nDétails:");
        for (DetailsDevis d : detailsAffichage) {
            System.out.println("- " + d.getDescription() + " x" + d.getQuantite() + 
                             " = " + d.getTotal() + " Ar");
        }
        System.out.println("\nMontant TOTAL: " + devisAffiche.getMontantTotal() + " Ar");
    }
}
```

---

## Bonnes Pratiques

✅ **À faire:**
```java
// 1. Toujours vérifier les null
Optional<Client> clientOpt = clientRepository.findById(5);
if (clientOpt.isPresent()) {
    // Utiliser le client
}

// 2. Utiliser les méthodes du service au lieu du repository
List<Devis> devis = devisService.findAll();  // ✓ BIEN

// 3. Ajouter des commentaires pour les workflows compliqués
demandeService.changerStatus(id, statusId, "Forage lancé - Profondeur 100m");

// 4. Gérer les exceptions
try {
    Demande d = demandeService.findById(999);
} catch (RuntimeException e) {
    // Afficher message d'erreur à l'utilisateur
}
```

❌ **À éviter:**
```java
// 1. Ne pas accéder au repository directement depuis le controller
List<Devis> devis = devisRepository.findAll();  // ✗ MAUVAIS

// 2. Ne pas créer plusieurs services pour la même entité
DevisService1 vs DevisService2  // ✗ MAUVAIS

// 3. Ne pas oublier les détails quand on crée un devis
devisService.creerDevis(id, typeId, null, null);  // ✗ Pas de détails = devis vide

// 4. Ne pas supprimer sans vérifier les dépendances
devisService.deleteById(id);  // Vérifie que la demande n'est pas en cours!
```

