package com.forage.repository;

import com.forage.entity.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Integer> {
    
    // Liste avec Client + Historique Status (pour la liste)
    @Query("SELECT DISTINCT d FROM Demande d " +
           "LEFT JOIN FETCH d.client " +
           "LEFT JOIN FETCH d.historiqueStatus hs " +
           "LEFT JOIN FETCH hs.status " +
           "ORDER BY d.date DESC")
    List<Demande> findAllWithClientAndStatus();
    
    // Détail complet d'une demande
    @Query("SELECT d FROM Demande d " +
           "LEFT JOIN FETCH d.client " +
           "LEFT JOIN FETCH d.historiqueStatus hs " +
           "LEFT JOIN FETCH hs.status " +
           "LEFT JOIN FETCH d.devis " +
           "WHERE d.id = :id")
    Optional<Demande> findByIdWithDetails(@Param("id") int id);
    
    // Simple avec client seulement
    @Query("SELECT d FROM Demande d " +
           "LEFT JOIN FETCH d.client " +
           "WHERE d.id = :id")
    Optional<Demande> findByIdWithClient(@Param("id") int id);
}