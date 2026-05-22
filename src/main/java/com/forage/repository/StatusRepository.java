package com.forage.repository;

import com.forage.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    Optional<Status> findByLibelle(String libelle);
    boolean existsByLibelle(String libelle);
    List<Status> findByLibelleContainingIgnoreCase(String libelle);
}