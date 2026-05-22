package com.forage.service;

import com.forage.entity.DetailsDevis;
import com.forage.repository.DetailsDevisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DetailsDevisService {
    private final DetailsDevisRepository detailsDevisRepository;

    public DetailsDevis findById(int id) {
        return detailsDevisRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("DetailsDevis non trouvé: " + id));
    }

}
