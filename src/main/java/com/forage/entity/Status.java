package com.forage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "status")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Status {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotBlank(message = "Le libellé est obligatoire")
    @Column(nullable = false, unique = true)
    private String libelle;
    
    @OneToMany(mappedBy = "status")
    private List<DemandeStatus> demandeStatuses = new ArrayList<>();
    
    // Pour affichage dans les select
    @Override
    public String toString() {
        return libelle;
    }
}