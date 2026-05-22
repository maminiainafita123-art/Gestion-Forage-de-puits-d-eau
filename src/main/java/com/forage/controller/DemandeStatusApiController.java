package com.forage.controller;

import com.forage.entity.DemandeStatus;
import com.forage.service.DemandeStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demande-status")
@RequiredArgsConstructor
public class DemandeStatusApiController {
    
    private final DemandeStatusService demandeStatusService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @GetMapping("/{demandeId}")
    public List<Map<String, Object>> getHistorique(@PathVariable int demandeId) {
        List<DemandeStatus> list = demandeStatusService.findByDemande(demandeId);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (DemandeStatus ds : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", ds.getId());
            item.put("statusLibelle", ds.getStatus().getLibelle());
            item.put("dateStatus", ds.getDateStatus().format(formatter));
            item.put("commentaire", ds.getCommentaire());
            result.add(item);
        }
        
        return result;
    }
}