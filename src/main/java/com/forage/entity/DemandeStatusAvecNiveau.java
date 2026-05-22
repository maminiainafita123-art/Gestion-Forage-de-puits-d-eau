package com.forage.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DemandeStatusAvecNiveau {
    
    private DemandeStatus demandeStatus;
    private String niveau;           // "normal", "eleve", "critique"
    private Indicateur indicateur;   // l'indicateur qui a déclenché le niveau
    private int dureeCalculee;       // durée réelle calculée
    
    // Méthodes utilitaires pour Thymeleaf
    
    public int getId() {
        return demandeStatus.getId();
    }
    
    public java.time.LocalDateTime getDateStatus() {
        return demandeStatus.getDateStatus();
    }
    
    public String getCommentaire() {
        return demandeStatus.getCommentaire();
    }
    
    public Status getStatus() {
        return demandeStatus.getStatus();
    }
    
    public Demande getDemande() {
        return demandeStatus.getDemande();
    }
    
    public int getDureeEstimer() {
        return demandeStatus.getDureeEstimer();
    }
    
    public int getDureeReel() {
        return demandeStatus.getDureeReel();
    }
    
    public boolean isEleve() {
        return "eleve".equals(niveau);
    }
    
    public boolean isCritique() {
        return "critique".equals(niveau);
    }
    
    public boolean isNormal() {
        return "normal".equals(niveau) || niveau == null;
    }
    
    public String getPlageIndicateur() {
        if (indicateur == null) return "";
        return indicateur.getHeure1() + "h → " + indicateur.getHeure2() + "h";
    }
}