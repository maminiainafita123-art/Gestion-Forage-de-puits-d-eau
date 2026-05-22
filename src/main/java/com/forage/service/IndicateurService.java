package com.forage.service;

import com.forage.entity.Indicateur;
import com.forage.entity.Demande;
import com.forage.entity.DemandeStatus;
import com.forage.entity.Status;
import com.forage.repository.DemandeStatusRepository;
import com.forage.repository.IndicateurRepository;
import com.forage.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IndicateurService {
    
    private final IndicateurRepository indicateurRepository;
    private final DemandeStatusRepository demandeStatusRepository;
    private final StatusRepository statusRepository;
    private final DureeCalculService dureeCalculService;
    
    public List<Indicateur> findAll() {
        return indicateurRepository.findAll();
    }
    
    public Indicateur findById(int id) {
        return indicateurRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Indicateur non trouvé"));
    }
    
    /**
     * Créer un indicateur avec vérification de doublon
     */
    public Indicateur save(int statusDebutId, int statusFinId,
                           int heure1, int heure2, String level) {
        
        // Vérifier que le level est valide
        if (!"eleve".equals(level) && !"critique".equals(level)) {
            throw new RuntimeException("Le niveau doit être 'eleve' ou 'critique'");
        }
        
        // Vérifier que les status sont différents
        if (statusDebutId == statusFinId) {
            throw new RuntimeException("Le status début et fin doivent être différents");
        }
        
        // Vérifier que heure1 < heure2
        if (heure1 >= heure2) {
            throw new RuntimeException("L'heure de début doit être inférieure à l'heure de fin");
        }
        
        // Vérifier si cet indicateur existe déjà
        boolean existe = indicateurRepository
            .existsByStatusDebutIdAndStatusFinIdAndLevel(statusDebutId, statusFinId, level);
        
        if (existe) {
            throw new RuntimeException(
                "Un indicateur '" + level + "' existe déjà pour cette paire de status");
        }
        
        Status statusDebut = statusRepository.findById(statusDebutId)
            .orElseThrow(() -> new RuntimeException("Status début non trouvé"));
        Status statusFin = statusRepository.findById(statusFinId)
            .orElseThrow(() -> new RuntimeException("Status fin non trouvé"));
        
        Indicateur indic = new Indicateur();
        indic.setStatusDebut(statusDebut);
        indic.setStatusFin(statusFin);
        indic.setHeure1(heure1);
        indic.setHeure2(heure2);
        indic.setLevel(level);
        
        return indicateurRepository.save(indic);
    }
    
    /**
     * Modifier un indicateur avec vérification de doublon
     */
    public Indicateur update(int id, int statusDebutId, int statusFinId,
                             int heure1, int heure2, String level) {
        
        Indicateur indic = findById(id);
        
        // Vérifier level
        if (!"eleve".equals(level) && !"critique".equals(level)) {
            throw new RuntimeException("Le niveau doit être 'eleve' ou 'critique'");
        }
        
        if (statusDebutId == statusFinId) {
            throw new RuntimeException("Le status début et fin doivent être différents");
        }
        
        if (heure1 >= heure2) {
            throw new RuntimeException("L'heure de début doit être inférieure à l'heure de fin");
        }
        
        // Vérifier doublon en excluant l'indicateur actuel
        boolean existe = indicateurRepository
            .existsByStatusPairAndLevelExcluding(statusDebutId, statusFinId, level, id);
        
        if (existe) {
            throw new RuntimeException(
                "Un autre indicateur '" + level + "' existe déjà pour cette paire de status");
        }
        
        Status statusDebut = statusRepository.findById(statusDebutId)
            .orElseThrow(() -> new RuntimeException("Status début non trouvé"));
        Status statusFin = statusRepository.findById(statusFinId)
            .orElseThrow(() -> new RuntimeException("Status fin non trouvé"));
        
        indic.setStatusDebut(statusDebut);
        indic.setStatusFin(statusFin);
        indic.setHeure1(heure1);
        indic.setHeure2(heure2);
        indic.setLevel(level);
        
        return indicateurRepository.save(indic);
    }
    
    /**
     * Vérifier quels levels sont déjà pris pour une paire de status
     */
    public List<String> getLevelsExistants(int statusDebutId, int statusFinId) {
        return indicateurRepository.findLevelsByStatusPair(statusDebutId, statusFinId);
    }
    
    public void deleteById(int id) {
        indicateurRepository.deleteById(id);
    }
    
    /**
     * Déterminer le niveau d'alerte pour une demande
     */
    public String determinerNiveau(Demande demande) {
        NiveauInfo info = determinerNiveauDetail(demande);
        return info.getNiveau();
    }
    
    public NiveauInfo determinerNiveauDetail(Demande demande) {
        List<DemandeStatus> historique = demandeStatusRepository
            .findByDemandeIdOrderByDateStatusAsc(demande.getId());
        
        if (historique.isEmpty()) {
            return new NiveauInfo("normal", 0, null);
        }
        
        List<Indicateur> indicateurs = indicateurRepository.findAll();
        
        if (indicateurs.isEmpty()) {
            return new NiveauInfo("normal", 0, null);
        }
        
        NiveauInfo pireInfo = new NiveauInfo("normal", 0, null);
        
        for (Indicateur indic : indicateurs) {
            DemandeStatus statusDebut = null;
            DemandeStatus statusFin = null;
            
            for (DemandeStatus ds : historique) {
                if (ds.getStatus().getId() == indic.getStatusDebut().getId()) {
                    statusDebut = ds;
                }
                if (ds.getStatus().getId() == indic.getStatusFin().getId()
                    && statusDebut != null) {
                    statusFin = ds;
                    break;
                }
            }
            
            if (statusDebut != null) {
                LocalDateTime dateFin = (statusFin != null)
                    ? statusFin.getDateStatus()
                    : LocalDateTime.now();
                
                int dureeHeures = dureeCalculService.calculerDureeReel(
                    statusDebut.getDateStatus(), dateFin);
                
                // Vérifier si la durée est dans la plage
                if (dureeHeures >= indic.getHeure1() && dureeHeures < indic.getHeure2()) {
                    if (estPire(indic.getLevel(), pireInfo.getNiveau())) {
                        pireInfo = new NiveauInfo(indic.getLevel(), dureeHeures, indic);
                    }
                } else if (dureeHeures >= indic.getHeure2()) {
                    if (estPire(indic.getLevel(), pireInfo.getNiveau())) {
                        pireInfo = new NiveauInfo(indic.getLevel(), dureeHeures, indic);
                    }
                }
            }
        }
        
        return pireInfo;
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
    
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class NiveauInfo {
        private String niveau;
        private int dureeHeures;
        private Indicateur indicateur;
    }
}