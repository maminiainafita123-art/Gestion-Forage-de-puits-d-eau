package com.forage.service;

import com.forage.entity.Client;
import com.forage.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {
    
    private final ClientRepository clientRepository;
    
    public List<Client> findAll() {
        return clientRepository.findAll();
    }
    
    public Client findById(int id) {
        return clientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Client non trouvé: " + id));
    }
    
    public Client save(Client client) {
        return clientRepository.save(client);
    }
    
    public void deleteById(int id) {
        clientRepository.deleteById(id);
    }
    
    public List<Client> search(String nom) {
        return clientRepository.findByNomContainingIgnoreCase(nom);
    }
}