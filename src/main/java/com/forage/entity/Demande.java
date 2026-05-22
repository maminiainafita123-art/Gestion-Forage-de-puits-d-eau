package com.forage.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Entity
@Table(name = "demandes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Demande {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @NotNull(message = "La date est obligatoire")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "date_demande", nullable = false)
    private LocalDate date;
    
    @NotBlank(message = "Le lieu est obligatoire")
    @Column(nullable = false)
    private String lieu;
    
    @NotBlank(message = "Le district est obligatoire")
    @Column(nullable = false)
    private String district;
    
    // ✅ EAGER = client toujours chargé
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", nullable = false)
    @NotNull(message = "Le client est obligatoire")
    private Client client;
    
    @OneToOne(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Devis devis;
    
    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DemandeStatus> historiqueStatus = new ArrayList<>();
    
    @Transient
    public DemandeStatus getDernierStatus() {
        if (historiqueStatus == null || historiqueStatus.isEmpty()) {
            return null;
        }
        return historiqueStatus.stream()
            .max(Comparator.comparing(DemandeStatus::getDateStatus))
            .orElse(null);
    }
    
    @Transient
    public String getDernierStatusLibelle() {
        DemandeStatus dernier = getDernierStatus();
        if (dernier != null && dernier.getStatus() != null) {
            return dernier.getStatus().getLibelle();
        }
        return "Non défini";
    }
}