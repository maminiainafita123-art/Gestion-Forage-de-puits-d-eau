package com.forage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "details_devis")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DetailsDevis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotBlank(message = "Le libellé est obligatoire")
    @Column(nullable = false)
    private String libelle;
    
    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix doit être positif")
    @Column(name = "prix_unitaire", nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaire;
    
    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    @Column(nullable = false)
    private int quantite;
    
    // insertable = false, updatable = false car la colonne est générée en base
    @Column(name = "total", precision = 15, scale = 2, insertable = false, updatable = false)
    private BigDecimal total;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;
    
    @PrePersist
    @PreUpdate
    public void calculerTotal() {
        if (prixUnitaire != null && quantite > 0) {
            this.total = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}