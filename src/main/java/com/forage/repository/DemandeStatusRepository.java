package com.forage.repository;

import com.forage.entity.DemandeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeStatusRepository extends JpaRepository<DemandeStatus, Integer> {
    
    // Dernier statut d'une demande
    Optional<DemandeStatus> findFirstByDemandeIdOrderByDateStatusDesc(Integer demandeId);
    
    // Avec jointures
    @Query("SELECT ds FROM DemandeStatus ds " +
           "LEFT JOIN FETCH ds.status " +
           "LEFT JOIN FETCH ds.demande " +
           "WHERE ds.demande.id = :demandeId " +
           "ORDER BY ds.dateStatus DESC")
    List<DemandeStatus> findByDemandeWithDetails(@Param("demandeId") Integer demandeId);

    @Query("SELECT ds FROM DemandeStatus ds " +
           "LEFT JOIN FETCH ds.status " +
           "LEFT JOIN FETCH ds.demande d " +
           "LEFT JOIN FETCH d.client " +
           "ORDER BY ds.dateStatus DESC")
    List<DemandeStatus> findAllWithDetails();

    @Query("SELECT ds FROM DemandeStatus ds " +
           "LEFT JOIN FETCH ds.status " +
           "LEFT JOIN FETCH ds.demande d " +
           "LEFT JOIN FETCH d.client " +
           "WHERE ds.id = :id")
    Optional<DemandeStatus> findByIdWithDetails(@Param("id") int id);

     @Query("SELECT ds FROM DemandeStatus ds " +
           "LEFT JOIN FETCH ds.status " +
           "LEFT JOIN FETCH ds.demande d " +
           "LEFT JOIN FETCH d.client " +
           "WHERE d.id = :demandeId " +
           "ORDER BY ds.dateStatus DESC")
    List<DemandeStatus> findByDemandeIdOrderByDateStatusDesc(
            @Param("demandeId") int demandeId);

    // Compter les statuts d'une demande
    long countByDemandeId(Integer demandeId);
    
    // Supprimer tout l'historique d'une demande
    void deleteByDemandeId(Integer demandeId);

    @Query("SELECT ds FROM DemandeStatus ds " +
              "LEFT JOIN FETCH ds.status s " +
              "LEFT JOIN FETCH ds.demande d " +
              "LEFT JOIN FETCH d.client " +
              "WHERE s.libelle = :status")
    List<DemandeStatus> trouverTousLesStatus(String status);

    @Query("SELECT COUNT(ds) FROM DemandeStatus ds " +
       "LEFT JOIN ds.status s " +
       "WHERE s.libelle = 'Cree'")
     int trouverNombreTousLesStatusCree();


    @Query("SELECT COUNT(ds) FROM DemandeStatus ds " +
       "LEFT JOIN ds.status s " +
       "WHERE s.libelle = 'Devis_Forage_Cree'")
     int trouverNombreTousLesStatusForage();


    @Query("SELECT COUNT(ds) FROM DemandeStatus ds " +
       "LEFT JOIN ds.status s " +
       "WHERE s.libelle = 'Devis_Etude_Cree'")
     int trouverNombreTousLesStatusEtude();

    List<DemandeStatus> findByDemandeIdOrderByDateStatusAsc(int demandeId);

}