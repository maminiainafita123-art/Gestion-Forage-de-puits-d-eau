package com.forage.service;

import com.forage.entity.TypeDevis;
import com.forage.repository.TypeDevisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TypeDevisService {
    
    private final TypeDevisRepository typeDevisRepository;
    
    public List<TypeDevis> findAll() {
        return typeDevisRepository.findAll();
    }
    
    public TypeDevis findById(int id) {
        return typeDevisRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Type devis non trouvé: " + id));
    }
    
    public TypeDevis save(TypeDevis typeDevis) {
        return typeDevisRepository.save(typeDevis);
    }
    
    public void deleteById(int id) {
        typeDevisRepository.deleteById(id);
    }
}