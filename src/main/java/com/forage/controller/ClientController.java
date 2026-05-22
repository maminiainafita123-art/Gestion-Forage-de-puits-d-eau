package com.forage.controller;

import com.forage.entity.Client;
import com.forage.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientService clientService;
    
    @GetMapping
    public String list(Model model) {
        model.addAttribute("clients", clientService.findAll());
        return "client/list";
    }
    
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("client", new Client());
        return "client/form";
    }
    
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("client", clientService.findById(id));
        return "client/form";
    }
    
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Client client,
                       BindingResult result,
                       RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "client/form";
        }
        
        clientService.save(client);
        redirectAttributes.addFlashAttribute("success", "Client enregistré avec succès");
        return "redirect:/clients";
    }
    
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Client supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Impossible de supprimer ce client");
        }
        return "redirect:/clients";
    }
    
    @GetMapping("/{id}")
    public String view(@PathVariable int id, Model model) {
        model.addAttribute("client", clientService.findById(id));
        return "client/view";
    }
}