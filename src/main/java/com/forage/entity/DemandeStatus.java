package com.forage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "demande_status")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DemandeStatus {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "date_status", nullable = false)
    private LocalDateTime dateStatus;
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    
    // ✅ EAGER = chargé automatiquement
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "demande_id", nullable = false)
    @NotNull(message = "La demande est obligatoire")
    private Demande demande;
    
    // ✅ EAGER = chargé automatiquement
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    @NotNull(message = "Le statut est obligatoire")
    private Status status;

    @Column(name ="Duree_estimer", nullable=false)
    private int DureeEstimer;

    @Column(name ="Duree_reel", nullable=false)
    private int DureeReel;
    
    @PrePersist
    public void prePersist() {
        if (dateStatus == null) {
            dateStatus = LocalDateTime.now();
        }
    }
}