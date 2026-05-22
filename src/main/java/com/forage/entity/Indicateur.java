package com.forage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "indicateur")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Indicateur {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id_1", nullable = false)
    @NotNull(message = "Le status de début est obligatoire")
    private Status statusDebut;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id_2", nullable = false)
    @NotNull(message = "Le status de fin est obligatoire")
    private Status statusFin;
    
    @Column(name = "heure_1", nullable = false)
    private int heure1;
    
    @Column(name = "heure_2", nullable = false)
    private int heure2;
    
    @Column(columnDefinition = "TEXT")
    private String level;
}