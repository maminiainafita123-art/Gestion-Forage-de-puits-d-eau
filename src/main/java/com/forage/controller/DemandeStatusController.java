package com.forage.controller;

import com.forage.entity.DemandeStatus;
import com.forage.entity.DemandeStatusAvecNiveau;
import com.forage.entity.Indicateur;
import com.forage.entity.Status;
import com.forage.repository.IndicateurRepository;
import com.forage.service.DemandeService;
import com.forage.service.DemandeStatusService;
import com.forage.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/demande-status")
@RequiredArgsConstructor
public class DemandeStatusController {
    
    private final DemandeStatusService demandeStatusService;
    private final DemandeService demandeService;
    private final StatusService statusService;
    private final IndicateurRepository indicateurRepository;
    
    @GetMapping
public String list(Model model) {
    // ✅ On récupère tous les DemandeStatus avec leurs niveaux
    List<DemandeStatus> allStatus = demandeStatusService.findAll();
    List<Indicateur> indicateurs = indicateurRepository.findAll();
    
    // Calculer le niveau pour chaque DemandeStatus
    List<DemandeStatusAvecNiveau> statusAvecNiveaux = new ArrayList<>();
    
    for (DemandeStatus ds : allStatus) {
        List<DemandeStatusAvecNiveau> historiqueNiveau = demandeStatusService
            .getHistoriqueAvecNiveau(ds.getDemande().getId());
        
        // Trouver le bon dans la liste
        for (DemandeStatusAvecNiveau dsn : historiqueNiveau) {
            if (dsn.getId() == ds.getId()) {
                statusAvecNiveaux.add(dsn);
                break;
            }
        }
    }
    
    model.addAttribute("demandeStatuses", statusAvecNiveaux);
    return "demande-status/list";
}
    
    @GetMapping("/new")
    public String createForm(@RequestParam(required = false) Integer demandeId, 
                             Model model) {
        model.addAttribute("demandes", demandeService.findAll());
        model.addAttribute("statuses", statusService.findAll());
        model.addAttribute("selectedDemandeId", demandeId);
        return "demande-status/form";
    }
    
    @PostMapping("/save")
    public String save(@RequestParam int demandeId,
                       @RequestParam int statusId,
                       @RequestParam(required = false) String commentaire,
                       RedirectAttributes redirectAttributes) {
        try {
            demandeStatusService.save(demandeId, statusId, commentaire);
            redirectAttributes.addFlashAttribute("success", "Statut ajouté avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/demande-status";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("demandeStatus", demandeStatusService.findById(id));
        return "demande-status/edit";
    }
    
    // ✅ Mise à jour commentaire + date
    @PostMapping("/update")
    public String update(@RequestParam int demandeStatusId,
                         @RequestParam String dateStatus,
                         @RequestParam(required = false) String commentaire,
                         RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime date = LocalDateTime.parse(dateStatus);
            demandeStatusService.updateCommentaireEtDate(demandeStatusId, commentaire, date);
            redirectAttributes.addFlashAttribute("success", "Modifié avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        return "redirect:/demande-status";
    }
    
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id, 
                         RedirectAttributes redirectAttributes) {
        try {
            demandeStatusService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer");
        }
        return "redirect:/demande-status";
    }

    // Dans le contrôleur
    @GetMapping("/{text}")
    public String StatusChoisie(@PathVariable String text, Model model) {
        String normalizedText = statusService.normaliserLibelle(text);
        
        List<DemandeStatus> demandesStatus = demandeStatusService.trouverTousLesStatus(normalizedText);
        model.addAttribute("statuslister", demandesStatus);
        model.addAttribute("libelleStatus", normalizedText);
        
        return "demande-status/statussortie";
    }
        
}