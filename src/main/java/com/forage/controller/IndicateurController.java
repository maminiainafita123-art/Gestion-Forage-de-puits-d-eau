package com.forage.controller;

import com.forage.service.IndicateurService;
import com.forage.service.StatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/indicateurs")
@RequiredArgsConstructor
public class IndicateurController {
    
    private final IndicateurService indicateurService;
    private final StatusService statusService;
    
    @GetMapping
    public String list(Model model) {
        model.addAttribute("indicateurs", indicateurService.findAll());
        return "indicateur/list";
    }
    
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("statuses", statusService.findAll());
        return "indicateur/form";
    }
    
    @PostMapping("/save")
    public String save(@RequestParam int statusDebutId,
                       @RequestParam int statusFinId,
                       @RequestParam int heure1,
                       @RequestParam int heure2,
                       @RequestParam String level,
                       RedirectAttributes redirectAttributes) {
        try {
            indicateurService.save(statusDebutId, statusFinId, heure1, heure2, level);
            redirectAttributes.addFlashAttribute("success", "Indicateur créé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/indicateurs";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("indicateur", indicateurService.findById(id));
        model.addAttribute("statuses", statusService.findAll());
        return "indicateur/edit";
    }
    
    @PostMapping("/update")
    public String update(@RequestParam int indicateurId,
                         @RequestParam int statusDebutId,
                         @RequestParam int statusFinId,
                         @RequestParam int heure1,
                         @RequestParam int heure2,
                         @RequestParam String level,
                         RedirectAttributes redirectAttributes) {
        try {
            indicateurService.update(indicateurId, statusDebutId, statusFinId, heure1, heure2, level);
            redirectAttributes.addFlashAttribute("success", "Indicateur modifié avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/indicateurs";
    }
    
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            indicateurService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Indicateur supprimé");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer");
        }
        return "redirect:/indicateurs";
    }
}