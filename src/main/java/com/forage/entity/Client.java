package com.forage.entity;

import jakarta.persistence.*;
import jakarta.persistence.OneToMany;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "le nom est requis pour le cients qui essaye d'envoyer leur demande")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "le Contact est requis pour le clients qui essaye d'envoyer leur demande")
    @Column(nullable = false)
    private String contact;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Demande> demandes = new ArrayList<>();
}
