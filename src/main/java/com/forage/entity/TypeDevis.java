package com.forage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "types_devis")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class TypeDevis {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotBlank(message = "Le libellé est obligatoire")
    @Column(nullable = false, unique = true)
    private String libelle;
    
    @OneToMany(mappedBy = "typeDevis")
    private List<Devis> devisList = new ArrayList<>();
}