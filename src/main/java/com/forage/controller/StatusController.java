package com.forage.controller;

import com.forage.entity.Status;
import com.forage.service.StatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/status")
@RequiredArgsConstructor
public class StatusController {
    
    private final StatusService statusService;
    
    // ==================== LISTE ====================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("statuses", statusService.findAll());
        return "status/list";
    }
    
    // ==================== CRÉATION ====================
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("status", new Status());
        model.addAttribute("isNew", true);
        return "status/form";
    }
    
    // ==================== MODIFICATION ====================
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("status", statusService.findById(id));
        model.addAttribute("isNew", false);
        return "status/form";
    }
    
    // ==================== ENREGISTREMENT ====================
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Status status,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            model.addAttribute("isNew", status.getId() == 0);
            return "status/form";
        }
        
        try {
            if (status.getId() == 0) {
                statusService.save(status);
                redirectAttributes.addFlashAttribute("success", "Status créé avec succès");
            } else {
                statusService.update(status.getId(), status);
                redirectAttributes.addFlashAttribute("success", "Status mis à jour");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }
        
        return "redirect:/status";
    }
    
    // ==================== SUPPRESSION ====================
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            statusService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Status supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Impossible de supprimer (utilisé par des demandes)");
        }
        return "redirect:/status";
    }
}