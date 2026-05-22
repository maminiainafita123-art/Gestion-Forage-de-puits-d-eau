package com.forage.service;

import com.forage.entity.Demande;
import com.forage.entity.DemandeStatus;
import com.forage.entity.DemandeStatusAvecNiveau;
import com.forage.entity.Indicateur;
import com.forage.entity.Status;
import com.forage.repository.DemandeRepository;
import com.forage.repository.DemandeStatusRepository;
import com.forage.repository.IndicateurRepository;
import com.forage.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeStatusService {
    
    private final DemandeStatusRepository demandeStatusRepository;
    private final DemandeRepository demandeRepository;
    private final StatusRepository statusRepository;
    private final DureeCalculService dureeCalculService;
    private final IndicateurRepository indicateurRepository;

    /**
     * Créer un nouveau statut avec calcul automatique des durées
     */
    public DemandeStatus save(int demandeId, int statusId, String commentaire) {
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new RuntimeException("Status non trouvé"));
        
        LocalDateTime maintenant = LocalDateTime.now();
        
        // Chercher le dernier statut de cette demande
        List<DemandeStatus> historique = demandeStatusRepository
            .findByDemandeIdOrderByDateStatusDesc(demandeId);
        
        int dureeEstimer = 0;
        int dureeReel = 0;
        
        if (!historique.isEmpty()) {
            // Calculer la durée depuis le dernier statut
            LocalDateTime datePrecedente = historique.get(0).getDateStatus();
            dureeEstimer = dureeCalculService.calculerDureeEstimer(datePrecedente, maintenant);
            dureeReel = dureeCalculService.calculerDureeReel(datePrecedente, maintenant);
        }
        
        DemandeStatus ds = new DemandeStatus();
        ds.setDemande(demande);
        ds.setStatus(status);
        ds.setCommentaire(commentaire);
        ds.setDateStatus(maintenant);
        ds.setDureeEstimer(dureeEstimer);
        ds.setDureeReel(dureeReel);
        
        return demandeStatusRepository.save(ds);
    }
    
    /**
     * Modifier commentaire et date (recalcule les durées)
     */
    public DemandeStatus updateCommentaireEtDate(int id, String commentaire, LocalDateTime dateStatus) {
        DemandeStatus ds = findById(id);
        ds.setCommentaire(commentaire);
        ds.setDateStatus(dateStatus);
        
        // Recalculer les durées
        recalculerDurees(ds);
        misAjourAll(ds);
        
        return demandeStatusRepository.save(ds);
    }

    /*
    * Recalculer les durées de TOUS les statuts APRÈS celui modifié
    * 
    * Exemple:
    * [Status 1: 01/01 08:00] → [Status 2: 05/01 10:00] → [Status 3: 10/01 14:00]
    *                               ↑ modifié à 06/01 09:00
    * → Status 3 doit être recalculé car son "précédent" a changé de date
    */
    public void misAjourAll(DemandeStatus dsModifie) {
        int demandeId = dsModifie.getDemande().getId();
        
        // Récupérer TOUT l'historique de la demande (ordre croissant pour traitement)
        List<DemandeStatus> historique = demandeStatusRepository
            .findByDemandeIdOrderByDateStatusAsc(demandeId);
        
        // Trouver la position du statut modifié
        int indexModifie = -1;
        for (int i = 0; i < historique.size(); i++) {
            if (historique.get(i).getId() == dsModifie.getId()) {
                indexModifie = i;
                // Mettre à jour avec les nouvelles valeurs du statut modifié
                historique.set(i, dsModifie);
                break;
            }
        }
        
        if (indexModifie == -1) return;
        
        // Recalculer TOUS les statuts APRÈS le statut modifié
        for (int i = indexModifie + 1; i < historique.size(); i++) {
            DemandeStatus courant = historique.get(i);
            DemandeStatus precedent = historique.get(i - 1);
            
            LocalDateTime datePrecedente = precedent.getDateStatus();
            LocalDateTime dateCourante = courant.getDateStatus();
            
            // Recalculer les durées
            int nouvelleEstimer = dureeCalculService.calculerDureeEstimer(datePrecedente, dateCourante);
            int nouvelleReel = dureeCalculService.calculerDureeReel(datePrecedente, dateCourante);
            
            courant.setDureeEstimer(nouvelleEstimer);
            courant.setDureeReel(nouvelleReel);
            
            // Sauvegarder chaque statut mis à jour
            demandeStatusRepository.save(courant);
        }
    }

    
    /**
     * Recalculer les durées par rapport au statut précédent
     */
    private void recalculerDurees(DemandeStatus ds) {
        List<DemandeStatus> historique = demandeStatusRepository
            .findByDemandeIdOrderByDateStatusDesc(ds.getDemande().getId());
        
        // Trouver le statut juste avant celui-ci
        LocalDateTime datePrecedente = null;
        boolean found = false;
        
        for (DemandeStatus h : historique) {
            if (found) {
                datePrecedente = h.getDateStatus();
                break;
            }
            if (h.getId() == ds.getId()) {
                found = true;
            }
        }
        
        if (datePrecedente != null) {
            ds.setDureeEstimer(
                dureeCalculService.calculerDureeEstimer(datePrecedente, ds.getDateStatus()));
            ds.setDureeReel(
                dureeCalculService.calculerDureeReel(datePrecedente, ds.getDateStatus()));
        } else {
            ds.setDureeEstimer(0);
            ds.setDureeReel(0);
        }
    }
    
    public void deleteById(int id) {
        demandeStatusRepository.deleteById(id);
    }
    
    public List<DemandeStatus> getHistorique(int demandeId) {
        return demandeStatusRepository.findByDemandeIdOrderByDateStatusDesc(demandeId);
    }
    
    public List<DemandeStatus> getHistoriqueWithDetails(int demandeId) {
        return demandeStatusRepository.findByDemandeWithDetails(demandeId);
    }
    
    public Optional<DemandeStatus> getDernierStatus(int demandeId) {
        return demandeStatusRepository.findFirstByDemandeIdOrderByDateStatusDesc(demandeId);
    }
    
    public DemandeStatus findById(int id) {
        return demandeStatusRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("DemandeStatus non trouvé: " + id));
    }
    
    public DemandeStatus ajouterStatus(int demandeId, int statusId, String commentaire) {
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée: " + demandeId));
        
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new RuntimeException("Status non trouvé: " + statusId));
        
        DemandeStatus demandeStatus = new DemandeStatus();
        demandeStatus.setDemande(demande);
        demandeStatus.setStatus(status);
        demandeStatus.setDateStatus(LocalDateTime.now());
        demandeStatus.setCommentaire(commentaire);
        
        return demandeStatusRepository.save(demandeStatus);
    }
    
    public DemandeStatus creerStatusInitial(Demande demande, int statusId) {
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new RuntimeException("Status non trouvé: " + statusId));
        
        DemandeStatus demandeStatus = new DemandeStatus();
        demandeStatus.setDemande(demande);
        demandeStatus.setStatus(status);
        demandeStatus.setDateStatus(LocalDateTime.now());
        demandeStatus.setCommentaire("Statut initial");
        
        return demandeStatusRepository.save(demandeStatus);
    }
    
    public DemandeStatus creerStatusInitialParDefaut(Demande demande) {
        Status statusParDefaut = statusRepository.findAll()
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Aucun status dans la base"));
        
        DemandeStatus demandeStatus = new DemandeStatus();
        demandeStatus.setDemande(demande);
        demandeStatus.setStatus(statusParDefaut);
        demandeStatus.setDateStatus(LocalDateTime.now());
        demandeStatus.setCommentaire("Création de la demande");
        
        return demandeStatusRepository.save(demandeStatus);
    }
    
    public void supprimer(int id) {
        demandeStatusRepository.deleteById(id);
    }

    public void supprimerHistorique(int demandeId) {
        demandeStatusRepository.deleteByDemandeId(demandeId);
    }
    
    public int nombreChangements(int demandeId) {
        return (int) demandeStatusRepository.countByDemandeId(demandeId);
    }

    
    public List<DemandeStatus> findAll() {
        return demandeStatusRepository.findAll();
    }

    public List<DemandeStatus> findByDemande(int demandeId) {
        return demandeStatusRepository.findByDemandeIdOrderByDateStatusDesc(demandeId);
    }


    public int trouverNombreTousLesStatusCree(){
        return demandeStatusRepository.trouverNombreTousLesStatusCree();
    }

    public int trouverNombreTousLesStatusForage(){
        return demandeStatusRepository.trouverNombreTousLesStatusForage();
    }

    public int trouverNombreTousLesStatusEtude(){
        return demandeStatusRepository.trouverNombreTousLesStatusEtude();
    }

    public List<DemandeStatus> trouverTousLesStatus(String text){
        return demandeStatusRepository.trouverTousLesStatus(text);
    }

    // Ajouter dans DemandeStatusService.java

/**
 * Récupérer l'historique avec les niveaux d'indicateur
 */
public List<DemandeStatusAvecNiveau> getHistoriqueAvecNiveau(int demandeId) {
    List<DemandeStatus> historique = demandeStatusRepository
        .findByDemandeIdOrderByDateStatusAsc(demandeId);
    
    List<Indicateur> indicateurs = indicateurRepository.findAll();
    List<DemandeStatusAvecNiveau> result = new java.util.ArrayList<>();
    
    for (int i = 0; i < historique.size(); i++) {
        DemandeStatus courant = historique.get(i);
        String niveau = "normal";
        Indicateur indicateurTrouve = null;
        int dureeCalculee = 0;
        
        if (i > 0) {
            DemandeStatus precedent = historique.get(i - 1);
            
            // Chercher un indicateur pour cette paire de status
            for (Indicateur indic : indicateurs) {
                if (indic.getStatusDebut().getId() == precedent.getStatus().getId()
                    && indic.getStatusFin().getId() == courant.getStatus().getId()) {
                    
                    int duree = courant.getDureeReel();
                    dureeCalculee = duree;
                    
                    if (duree >= indic.getHeure1() && duree < indic.getHeure2()) {
                        if (estPire(indic.getLevel(), niveau)) {
                            niveau = indic.getLevel();
                            indicateurTrouve = indic;
                        }
                    } else if (duree >= indic.getHeure2()) {
                        if (estPire(indic.getLevel(), niveau)) {
                            niveau = indic.getLevel();
                            indicateurTrouve = indic;
                        }
                    }
                }
            }
        }
        
        result.add(new DemandeStatusAvecNiveau(courant, niveau, indicateurTrouve, dureeCalculee));
    }
    
    // Inverser pour avoir DESC (plus récent en premier)
    java.util.Collections.reverse(result);
    return result;
}

private boolean estPire(String nouveau, String actuel) {
    return getValeurNiveau(nouveau) > getValeurNiveau(actuel);
}

private int getValeurNiveau(String niveau) {
    if (niveau == null) return 0;
    return switch (niveau.toLowerCase()) {
        case "critique" -> 3;
        case "eleve" -> 2;
        case "normal" -> 1;
        default -> 0;
    };
}


}