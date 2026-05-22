package com.forage.controller;

import com.forage.entity.Demande;
import com.forage.entity.DetailsDevis;
import com.forage.entity.Devis;
import com.forage.service.DemandeService;
import com.forage.service.DevisService;
import com.forage.service.StatusService;
import com.forage.service.TypeDevisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/devis")
@RequiredArgsConstructor
public class DevisController {
    
    private final DevisService devisService;
    private final DemandeService demandeService;
    private final TypeDevisService typeDevisService;
    private final StatusService statusService;
    
    @GetMapping
    public String list(Model model) {
        model.addAttribute("devisList", devisService.findAll());
        model.addAttribute("devisTotal", devisService.motantDevisTotal());
        return "devis/list";
    }
    
    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Integer demandeId, Model model) {
        model.addAttribute("demandesSansDevis", devisService.getDemandesSansDevis());
        model.addAttribute("typesDevis", typeDevisService.findAll());
        model.addAttribute("statuses", statusService.findAll());
        model.addAttribute("selectedDemandeId", demandeId);
        return "devis/form";
    }
    
    // ==================== API AUTOCOMPLETE - INFOS COMPLÈTES ====================
    
    @GetMapping("/api/demandes")
    @ResponseBody
    public List<Map<String, Object>> searchDemandes(@RequestParam(required = false) String q) {
        List<Demande> demandes = devisService.getDemandesSansDevis();
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        for (Demande d : demandes) {
            // Filtrer par recherche
            if (q == null || q.isEmpty() || 
                d.getLieu().toLowerCase().contains(q.toLowerCase()) ||
                d.getClient().getNom().toLowerCase().contains(q.toLowerCase()) ||
                d.getDistrict().toLowerCase().contains(q.toLowerCase())) {
                
                Map<String, Object> item = new HashMap<>();
                
                // ID Demande
                item.put("id", d.getId());
                
                // Infos Demande
                item.put("dateDemande", d.getDate().format(formatter));
                item.put("lieu", d.getLieu());
                item.put("district", d.getDistrict());
                
                // Infos Client
                item.put("clientId", d.getClient().getId());
                item.put("clientNom", d.getClient().getNom());
                item.put("clientContact", d.getClient().getContact());
                
                // Statut actuel
                item.put("statut", d.getDernierStatusLibelle());
                
                // Label pour autocomplete
                item.put("label", d.getClient().getNom() + " - " + d.getLieu());
                
                result.add(item);
            }
        }
        
        return result;
    }
    
    // ==================== API - OBTENIR DÉTAILS D'UNE DEMANDE ====================
    
    @GetMapping("/api/demandes/{id}")
    @ResponseBody
    public Map<String, Object> getDemandeDetails(@PathVariable int id) {
        Demande d = demandeService.findById(id);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        Map<String, Object> result = new HashMap<>();
        
        // Infos Demande
        result.put("id", d.getId());
        result.put("dateDemande", d.getDate().format(formatter));
        result.put("lieu", d.getLieu());
        result.put("district", d.getDistrict());
        result.put("statut", d.getDernierStatusLibelle());
        
        // Infos Client
        Map<String, Object> client = new HashMap<>();
        client.put("id", d.getClient().getId());
        client.put("nom", d.getClient().getNom());
        client.put("contact", d.getClient().getContact());
        result.put("client", client);
        
        return result;
    }
    
    // ==================== ENREGISTREMENT ====================
    
    @PostMapping("/save")
    public String save(@RequestParam int demandeId,
                       @RequestParam int typeDevisId,
                       @RequestParam(required = false) Integer statusId,
                       @RequestParam(required = false) List<String> libelles,
                       @RequestParam(required = false) List<BigDecimal> prixUnitaires,
                       @RequestParam(required = false) List<Integer> quantites,
                       @RequestParam(required = false) Integer devisId,
                       RedirectAttributes redirectAttributes) {
        
        
        try {
            List<DetailsDevis> details = new ArrayList<>();
            if (libelles != null && !libelles.isEmpty()) {
                for (int i = 0; i < libelles.size(); i++) {
                    BigDecimal decimal = new BigDecimal(1000000);
                    BigDecimal de = new BigDecimal(0.1);
                    int result = prixUnitaires.get(i).compareTo(decimal);
                    BigDecimal max = prixUnitaires.get(i).multiply(de);
                    BigDecimal fin = prixUnitaires.get(i).subtract(max);
                    if (result >= 0) {
                        if (libelles.get(i) != null && !libelles.get(i).trim().isEmpty()) {
                            DetailsDevis detail = new DetailsDevis();
                            detail.setLibelle(libelles.get(i));
                            detail.setPrixUnitaire(fin);
                            detail.setQuantite(quantites.get(i));
                            details.add(detail);
                        }
                    }else if (result < 0) {
                        if (libelles.get(i) != null && !libelles.get(i).trim().isEmpty()) {
                            DetailsDevis detail = new DetailsDevis();
                            detail.setLibelle(libelles.get(i));
                            detail.setPrixUnitaire(prixUnitaires.get(i));
                            detail.setQuantite(quantites.get(i));
                            details.add(detail);
                        } 
                    }
                    
                }
            }
            
            if (devisId != null && devisId > 0) {
                devisService.updateDevis(devisId, typeDevisId, statusId, details);
                redirectAttributes.addFlashAttribute("success", "Devis mis à jour");
            } else {
                devisService.creerDevis(demandeId, typeDevisId, statusId, details);
                redirectAttributes.addFlashAttribute("success", "Devis créé avec succès");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/devis/new";
        }
        
        return "redirect:/devis";
    }
    
    @GetMapping("/{id}")
    public String view(@PathVariable int id, Model model) {
        Devis devis = devisService.findById(id);
        model.addAttribute("devis", devis);
        model.addAttribute("details", devisService.getDetails(id));
        return "devis/view";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        Devis devis = devisService.findById(id);
        model.addAttribute("devis", devis);
        model.addAttribute("details", devisService.getDetails(id));
        model.addAttribute("typesDevis", typeDevisService.findAll());
        model.addAttribute("statuses", statusService.findAll());
        return "devis/edit";
    }
    
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            devisService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Devis supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer");
        }
        return "redirect:/devis";
    }
}