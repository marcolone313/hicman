package com.osc.CorporateSite.Controller;

import com.osc.CorporateSite.Model.Testimonial;
import com.osc.CorporateSite.Service.TestimonialService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/testimonials")
public class AdminTestimonialController {

    @Autowired
    private TestimonialService testimonialService;

    /**
     * Lista tutti i testimonial
     */
    @GetMapping
    public String listTestimonials(
            @RequestParam(required = false) String status,
            Model model) {
        
        List<Testimonial> testimonials;
        
        if ("published".equals(status)) {
            testimonials = testimonialService.getPublishedTestimonials();
        } else if ("draft".equals(status)) {
            testimonials = testimonialService.getDraftTestimonials();
        } else {
            testimonials = testimonialService.getAllTestimonials();
        }
        
        model.addAttribute("testimonials", testimonials);
        model.addAttribute("status", status);
        model.addAttribute("pageTitle", "Gestione Testimonial");
        
        return "admin/testimonials/list";
    }

    /**
     * Form per creare nuovo testimonial
     */
    @GetMapping("/new")
    public String newTestimonialForm(Model model) {
        model.addAttribute("testimonial", new Testimonial());
        model.addAttribute("pageTitle", "Nuovo Testimonial");
        model.addAttribute("isEdit", false);
        return "admin/testimonials/form";
    }

    /**
     * Form per modificare testimonial esistente
     */
    @GetMapping("/edit/{id}")
    public String editTestimonialForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Testimonial> testimonial = testimonialService.getTestimonialById(id);
        
        if (testimonial.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Testimonial non trovato");
            return "redirect:/admin/testimonials";
        }
        
        model.addAttribute("testimonial", testimonial.get());
        model.addAttribute("pageTitle", "Modifica Testimonial");
        model.addAttribute("isEdit", true);
        return "admin/testimonials/form";
    }

    /**
     * Salvataggio nuovo testimonial o aggiornamento
     */
    @PostMapping("/save")
    public String saveTestimonial(
            @Valid @ModelAttribute("testimonial") Testimonial testimonial,
            BindingResult result,
            @RequestParam(defaultValue = "false") boolean publish,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", testimonial.getId() == null ? "Nuovo Testimonial" : "Modifica Testimonial");
            model.addAttribute("isEdit", testimonial.getId() != null);
            return "admin/testimonials/form";
        }
        
        try {
            testimonial.setPublished(publish);
            Testimonial savedTestimonial = testimonialService.saveTestimonial(testimonial);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Testimonial " + (testimonial.getId() == null ? "creato" : "aggiornato") + " con successo!");
            
            return "redirect:/admin/testimonials/edit/" + savedTestimonial.getId();
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante il salvataggio: " + e.getMessage());
            model.addAttribute("pageTitle", testimonial.getId() == null ? "Nuovo Testimonial" : "Modifica Testimonial");
            model.addAttribute("isEdit", testimonial.getId() != null);
            return "admin/testimonials/form";
        }
    }

    /**
     * Pubblicazione/Depubblicazione rapida
     */
    @PostMapping("/toggle-publish/{id}")
    public String togglePublish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Testimonial> testimonialOpt = testimonialService.getTestimonialById(id);
            
            if (testimonialOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Testimonial non trovato");
                return "redirect:/admin/testimonials";
            }
            
            Testimonial testimonial = testimonialOpt.get();
            testimonial.setPublished(!testimonial.isPublished());
            testimonialService.saveTestimonial(testimonial);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Testimonial " + (testimonial.isPublished() ? "pubblicato" : "rimosso dalla pubblicazione") + " con successo!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    /**
     * Cancellazione testimonial
     */
    @PostMapping("/delete/{id}")
    public String deleteTestimonial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            testimonialService.deleteTestimonial(id);
            redirectAttributes.addFlashAttribute("successMessage", "Testimonial eliminato con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore durante l'eliminazione: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    /**
     * Riordina testimonial (sposta su)
     */
    @PostMapping("/move-up/{id}")
    public String moveUp(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            testimonialService.moveUp(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ordine aggiornato!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    /**
     * Riordina testimonial (sposta giù)
     */
    @PostMapping("/move-down/{id}")
    public String moveDown(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            testimonialService.moveDown(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ordine aggiornato!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    /**
     * Eliminazione multipla
     */
    @PostMapping("/delete-multiple")
    public String deleteMultipleTestimonials(
            @RequestParam("testimonialIds") Long[] testimonialIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            int deletedCount = testimonialService.deleteTestimonials(testimonialIds);
            redirectAttributes.addFlashAttribute("successMessage", 
                deletedCount + " testimonial eliminati con successo!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Errore durante l'eliminazione: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }
}