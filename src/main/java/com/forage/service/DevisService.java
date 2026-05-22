package com.forage.service;

import com.forage.entity.*;
import com.forage.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DevisService {
    
    private final DevisRepository devisRepository;
    private final DetailsDevisRepository detailsDevisRepository;
    private final DemandeRepository demandeRepository;
    private final TypeDevisRepository typeDevisRepository;
    private final StatusRepository statusRepository;
    private final DemandeStatusRepository demandeStatusRepository;
    
    public List<Devis> findAll() {
        return devisRepository.findAllWithDetails();
    }
    
    public Devis findById(int id) {
        return devisRepository.findByIdWithDetails(id)
            .orElseThrow(() -> new RuntimeException("Devis non trouvé: " + id));
    }
    
    public List<DetailsDevis> getDetails(int devisId) {
        return detailsDevisRepository.findByDevisId(devisId);
    }
    
    
    private void changerStatusDemande(Demande demande, int statusId, String commentaire) {
        Status status = statusRepository.findById(statusId)
            .orElseThrow(() -> new RuntimeException("Status non trouvé"));
        
        DemandeStatus demandeStatus = new DemandeStatus();
        demandeStatus.setDemande(demande);
        demandeStatus.setStatus(status);
        demandeStatus.setDateStatus(LocalDateTime.now());
        demandeStatus.setCommentaire(commentaire);
        
        demandeStatusRepository.save(demandeStatus);
    }
    
    public void deleteById(int id) {
        // Supprimer d'abord les détails
        List<DetailsDevis> details = detailsDevisRepository.findByDevisId(id);
        if (details != null && !details.isEmpty()) {
            detailsDevisRepository.deleteAll(details);
        }
        // Puis supprimer le devis
        devisRepository.deleteById(id);
    }
    
    /**
     * Obtenir les demandes sans devis (pour autocomplete)
     */
    public List<Demande> getDemandesSansDevis() {
        return demandeRepository.findAllWithClientAndStatus()
            .stream()
            .filter(d -> !devisRepository.existsByDemandeId(d.getId()))
            .toList();
    }

    /**
 * Créer un devis complet avec détails et changement de statut
 */
public Devis creerDevis(int demandeId, int typeDevisId, Integer statusId,
                       List<DetailsDevis> details) {
    
    // Vérifier si la demande a déjà un devis
    if (devisRepository.existsByDemandeId(demandeId)) {
        throw new RuntimeException("Cette demande a déjà un devis");
    }
    
    // Récupérer la demande
    Demande demande = demandeRepository.findById(demandeId)
        .orElseThrow(() -> new RuntimeException("Demande non trouvée"));
    
    // Récupérer le type de devis
    TypeDevis typeDevis = typeDevisRepository.findById(typeDevisId)
        .orElseThrow(() -> new RuntimeException("Type de devis non trouvé"));
    
    // Créer le devis
    Devis devis = new Devis();
    devis.setDateDevis(LocalDate.now());
    devis.setDemande(demande);
    devis.setTypeDevis(typeDevis);
    
    // ✅ Ajouter les détails en utilisant la méthode setAllDetails
    if (details != null && !details.isEmpty()) {
        devis.setAllDetails(details);
    }
    
    // ✅ Sauvegarder (le montant total sera calculé automatiquement par @PrePersist)
    Devis savedDevis = devisRepository.save(devis);
    
    // Changer le statut de la demande si spécifié
    if (statusId != null) {
        changerStatusDemande(demande, statusId, 
            "Devis créé - Montant: " + savedDevis.getMontantTotal() + " Ar");
    }
    
    return savedDevis;
}

/**
 * Mettre à jour un devis existant
 */
public Devis updateDevis(int devisId, int typeDevisId, Integer statusId,
                        List<DetailsDevis> details) {
    
    Devis devis = findById(devisId);
    
    // Mettre à jour le type
    TypeDevis typeDevis = typeDevisRepository.findById(typeDevisId)
        .orElseThrow(() -> new RuntimeException("Type de devis non trouvé"));
    devis.setTypeDevis(typeDevis);
    
    // ✅ Mettre à jour les détails
    if (details != null) {
        devis.setAllDetails(details);
    }
    
    // ✅ Sauvegarder (le montant total sera recalculé automatiquement)
    Devis savedDevis = devisRepository.save(devis);
    
    // Changer le statut si spécifié
    if (statusId != null) {
        changerStatusDemande(devis.getDemande(), statusId, 
            "Devis modifié - Nouveau montant: " + savedDevis.getMontantTotal() + " Ar");
    }
    
    return savedDevis;
}

    public BigDecimal motantDevisTotal(){
        BigDecimal big = devisRepository.MontantDevisTotal();
        return big;
    }
}