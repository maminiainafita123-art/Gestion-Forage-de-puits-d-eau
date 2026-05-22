package com.forage.service;

import com.forage.entity.Demande;
import com.forage.entity.DemandeStatus;
import com.forage.entity.Status;
import com.forage.repository.DemandeRepository;
import com.forage.repository.DemandeStatusRepository;
import com.forage.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DemandeService {
    
    private final DemandeRepository demandeRepository;
    private final DemandeStatusRepository demandeStatusRepository;
    private final StatusRepository statusRepository;
    
    public List<Demande> findAll() {
        return demandeRepository.findAllWithClientAndStatus();
    }
    
    public Demande findById(int id) {
        return demandeRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée: " + id));
    }
    
    public Demande findByIdSimple(int id) {
        return demandeRepository.findByIdWithClient(id)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée: " + id));
    }
    
    public Demande save(Demande demande) {
        return demandeRepository.save(demande);
    }
    
    /**
     * Créer avec statut spécifié
     */
    public Demande creerAvecStatusInitial(Demande demande, int statusId) {
        Demande savedDemande = demandeRepository.save(demande);
        
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new RuntimeException("Status non trouvé: " + statusId));
        
        creerDemandeStatus(savedDemande, status, "Création de la demande");
        
        return savedDemande;
    }
    
    /**
     * Créer avec statut par défaut (premier de la liste)
     */
    public Demande creerAvecStatusParDefaut(Demande demande) {
        Demande savedDemande = demandeRepository.save(demande);
        
        // Prendre le premier statut disponible
        Status statusParDefaut = statusRepository.findAll()
            .stream()
            .findFirst()
            .orElse(null);
        
        if (statusParDefaut != null) {
            creerDemandeStatus(savedDemande, statusParDefaut, "Création (statut par défaut)");
        }
        
        return savedDemande;
    }
    
    private void creerDemandeStatus(Demande demande, Status status, String commentaire) {
        DemandeStatus demandeStatus = new DemandeStatus();
        demandeStatus.setDemande(demande);
        demandeStatus.setStatus(status);
        demandeStatus.setDateStatus(LocalDateTime.now());
        demandeStatus.setCommentaire(commentaire);
        demandeStatusRepository.save(demandeStatus);
    }
    
    public void deleteById(int id) {
        demandeRepository.deleteById(id);
    }
    
    public void changerStatus(int demandeId, int statusId, String commentaire) {
        Demande demande = demandeRepository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
        
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new RuntimeException("Status non trouvé"));
        
        creerDemandeStatus(demande, status, commentaire);
    }
    
    public List<DemandeStatus> getHistoriqueStatus(int demandeId) {
        return demandeStatusRepository.findByDemandeIdOrderByDateStatusDesc(demandeId);
    }
}