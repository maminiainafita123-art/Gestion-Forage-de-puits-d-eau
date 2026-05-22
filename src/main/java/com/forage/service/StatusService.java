package com.forage.service;

import com.forage.repository.StatusRepository;
import com.forage.entity.Status;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StatusService {
    private final StatusRepository statusRepository;

    public List<Status> getAllStatus() {
        return statusRepository.findAll();
    }

    public Status getStatusById(int id) {
        return statusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Status not found with id: " + id));
    }

    public List<Status> findAll() {
        return statusRepository.findAll();
    }

    public Status findById(int id) {
        return statusRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Status non trouvé: " + id));
    }
    
    public Status save(Status status) {
        // Vérifier doublon
        if (status.getId() == 0 && statusRepository.existsByLibelle(status.getLibelle())) {
            throw new RuntimeException("Ce libellé existe déjà");
        }
        return statusRepository.save(status);
    }
    
    public Status update(int id, Status statusDetails) {
        Status status = findById(id);
        status.setLibelle(statusDetails.getLibelle());
        return statusRepository.save(status);
    }
    
    public void deleteById(int id) {
        statusRepository.deleteById(id);
    }

    public List<Status> search(String libelle) {
        return statusRepository.findByLibelleContainingIgnoreCase(libelle);
    }

    public boolean existsByLibelle(String libelle){
        return statusRepository.existsByLibelle(libelle);
    }

    // Dans StatusService
    public String normaliserLibelle(String libelle) {
        // Convertir Devis_Etude_Cree en devis etude cree ou le libellé correspondant
        switch(libelle) {
            case "étude":
                return "Devis_Etude_Cree"; // ou le libellé correct dans votre BD
            case "forage":
                return "Devis_Forage_Cree";
            case "créé":
                return "Cree";
            default:
                return libelle.toLowerCase();
        }
    }
}
