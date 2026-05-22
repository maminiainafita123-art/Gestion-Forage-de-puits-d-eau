package com.forage.repository;

import com.forage.entity.Indicateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndicateurRepository extends JpaRepository<Indicateur, Integer> {
    
    List<Indicateur> findByStatusDebutIdAndStatusFinId(int statusDebutId, int statusFinId);
    
    // Vérifier si un indicateur existe déjà pour cette paire + level
    boolean existsByStatusDebutIdAndStatusFinIdAndLevel(
        int statusDebutId, int statusFinId, String level);
    
    // Vérifier doublon en excluant un ID (pour modification)
    @Query("SELECT COUNT(i) > 0 FROM Indicateur i " +
           "WHERE i.statusDebut.id = :statusDebutId " +
           "AND i.statusFin.id = :statusFinId " +
           "AND i.level = :level " +
           "AND i.id != :excludeId")
    boolean existsByStatusPairAndLevelExcluding(
        @Param("statusDebutId") int statusDebutId,
        @Param("statusFinId") int statusFinId,
        @Param("level") String level,
        @Param("excludeId") int excludeId);
    
    // Compter combien de levels existent pour une paire
    @Query("SELECT i.level FROM Indicateur i " +
           "WHERE i.statusDebut.id = :statusDebutId " +
           "AND i.statusFin.id = :statusFinId")
    List<String> findLevelsByStatusPair(
        @Param("statusDebutId") int statusDebutId,
        @Param("statusFinId") int statusFinId);
}