package com.forage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devis")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Devis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotNull(message = "La date est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_devis", nullable = false)
    private LocalDate dateDevis;
    
    @Column(name = "montant_total", precision = 15, scale = 2)
    private BigDecimal montantTotal = BigDecimal.ZERO;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_devis_id", nullable = false)
    @NotNull(message = "Le type de devis est obligatoire")
    private TypeDevis typeDevis;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_id", nullable = false, unique = true)
    @NotNull(message = "La demande est obligatoire")
    private Demande demande;
    
    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetailsDevis> details = new ArrayList<>();
    
    // Recalculer le montant total
    public void calculerMontantTotal() {
        if (details == null || details.isEmpty()) {
            this.montantTotal = BigDecimal.ZERO;
            return;
        }
        
        this.montantTotal = details.stream()
            .map(detail -> {
                // Calculer le total du détail si nécessaire
                if (detail.getTotal() == null && detail.getPrixUnitaire() != null && detail.getQuantite() > 0) {
                    detail.calculerTotal();
                }
                return detail.getTotal() != null ? detail.getTotal() : BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Ajouter un détail
    public void addDetail(DetailsDevis detail) {
        if (detail != null) {
            details.add(detail);
            detail.setDevis(this);
            // Calculer le total du détail
            detail.calculerTotal();
            // Recalculer le total du devis
            calculerMontantTotal();
        }
    }
    
    // Supprimer un détail
    public void removeDetail(DetailsDevis detail) {
        if (detail != null) {
            details.remove(detail);
            detail.setDevis(null);
            calculerMontantTotal();
        }
    }
    
    // Vider et ajouter tous les détails
    public void setAllDetails(List<DetailsDevis> newDetails) {
        this.details.clear();
        if (newDetails != null && !newDetails.isEmpty()) {
            for (DetailsDevis detail : newDetails) {
                detail.setDevis(this);
                detail.calculerTotal();
                this.details.add(detail);
            }
            calculerMontantTotal();
        } else {
            this.montantTotal = BigDecimal.ZERO;
        }
    }
    
    // Méthode utilitaire pour mettre à jour le montant total depuis les détails
    @PrePersist
    @PreUpdate
    public void updateMontantTotal() {
        calculerMontantTotal();
    }
}