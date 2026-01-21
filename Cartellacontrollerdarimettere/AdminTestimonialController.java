package com.hicman.CorporateSite.Controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hicman.CorporateSite.Model.Testimonial;
import com.hicman.CorporateSite.Service.FileStorageService;
import com.hicman.CorporateSite.Service.TestimonialService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/testimonials")
public class AdminTestimonialController {

    @Autowired
    private TestimonialService testimonialService;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Lista tutti i testimonial con filtro per stato
     * GET /admin/testimonials
     * 
     * @param status Filtro: published, draft, o null per tutti
     * @param model Model per Thymeleaf
     * @return Template admin/testimonials/list.html
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
        model.addAttribute("pageTitle", "Gestione Testimonianze");
        
        return "admin/testimonials/list";
    }

    /**
     * Form per creare nuovo testimonial
     * GET /admin/testimonials/new
     * 
     * @param model Model per Thymeleaf
     * @return Template admin/testimonials/form.html
     */
    @GetMapping("/new")
    public String newTestimonialForm(Model model) {
        Testimonial testimonial = new Testimonial();
        // Imposta data di pubblicazione di default
        testimonial.setPublishedDate(LocalDateTime.now());
        
        model.addAttribute("testimonial", testimonial);
        model.addAttribute("pageTitle", "Nuova Testimonianza");
        model.addAttribute("isEdit", false);
        return "admin/testimonials/form";
    }

    /**
     * Form per modificare testimonial esistente
     * GET /admin/testimonials/edit/{id}
     * 
     * @param id ID del testimonial
     * @param model Model per Thymeleaf
     * @param redirectAttributes Per messaggi flash
     * @return Template admin/testimonials/form.html o redirect se non trovato
     */
    @GetMapping("/edit/{id}")
    public String editTestimonialForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Testimonial> testimonial = testimonialService.getTestimonialById(id);
        
        if (testimonial.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Testimonianza non trovata");
            return "redirect:/admin/testimonials";
        }
        
        model.addAttribute("testimonial", testimonial.get());
        model.addAttribute("pageTitle", "Modifica Testimonianza");
        model.addAttribute("isEdit", true);
        return "admin/testimonials/form";
    }

    /**
     * Salvataggio nuovo testimonial o aggiornamento
     * POST /admin/testimonials/save
     * 
     * Gestisce:
     * - Upload logo
     * - Rimozione logo esistente
     * - Pubblicazione immediata o salvataggio come bozza
     * 
     * @param testimonial Dati del testimonial
     * @param result Risultato validazione
     * @param logoFile File logo (opzionale)
     * @param removeLogo Flag per rimuovere logo esistente
     * @param action Azione: "save" o "save_and_publish"
     * @param redirectAttributes Per messaggi flash
     * @param model Model per Thymeleaf
     * @return Redirect a edit form o ritorna al form con errori
     */
    @PostMapping("/save")
    public String saveTestimonial(
            @Valid @ModelAttribute("testimonial") Testimonial testimonial,
            BindingResult result,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestParam(value = "removeLogo", required = false) String removeLogo,
            @RequestParam(value = "action", defaultValue = "save") String action,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // Validazione
        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Correggi gli errori nel form");
            model.addAttribute("pageTitle", testimonial.getId() == null ? "Nuova Testimonianza" : "Modifica Testimonianza");
            model.addAttribute("isEdit", testimonial.getId() != null);
            return "admin/testimonials/form";
        }
        
        try {
            // ===== GESTIONE RIMOZIONE LOGO =====
            if ("true".equals(removeLogo) && testimonial.getLogoUrl() != null) {
                // Elimina il logo esistente dal filesystem
                fileStorageService.deleteFile(testimonial.getLogoUrl());
                testimonial.setLogoUrl(null);
            }
            
            // ===== GESTIONE UPLOAD NUOVO LOGO =====
            if (logoFile != null && !logoFile.isEmpty()) {
                // Valida il file
                if (!isValidImageFile(logoFile)) {
                    model.addAttribute("errorMessage", 
                        "Formato logo non valido. Sono accettati: PNG, JPG (max 10MB)");
                    model.addAttribute("pageTitle", testimonial.getId() == null ? "Nuova Testimonianza" : "Modifica Testimonianza");
                    model.addAttribute("isEdit", testimonial.getId() != null);
                    return "admin/testimonials/form";
                }
                
                // Se c'era già un logo, eliminalo prima
                if (testimonial.getLogoUrl() != null && !testimonial.getLogoUrl().isEmpty()) {
                    fileStorageService.deleteFile(testimonial.getLogoUrl());
                }
                
                // Salva il nuovo logo nella cartella "testimonials"
                String logoUrl = fileStorageService.storeFile(logoFile, "testimonials");
                testimonial.setLogoUrl(logoUrl);
            }
            
            // ===== GESTIONE PUBBLICAZIONE =====
            if ("save_and_publish".equals(action)) {
                testimonial.setPublished(true);
                if (testimonial.getPublishedDate() == null) {
                    testimonial.setPublishedDate(LocalDateTime.now());
                }
            } else {
                // Mantiene lo stato esistente se è un edit, altrimenti bozza
                if (testimonial.getId() == null) {
                    testimonial.setPublished(false);
                }
            }
            
            // Imposta data di pubblicazione se viene pubblicato
            if (testimonial.isPublished() && testimonial.getPublishedDate() == null) {
                testimonial.setPublishedDate(LocalDateTime.now());
            }
            
            // ===== SALVA IL TESTIMONIAL =====
            Testimonial savedTestimonial = testimonialService.saveTestimonial(testimonial);
            
            // Messaggio di successo
            String message = testimonial.getId() == null ? "Testimonianza creata" : "Testimonianza aggiornata";
            if ("save_and_publish".equals(action)) {
                message += " e pubblicata";
            }
            message += " con successo!";
            
            redirectAttributes.addFlashAttribute("successMessage", message);
            
            return "redirect:/admin/testimonials/edit/" + savedTestimonial.getId();
            
        } catch (IOException e) {
            model.addAttribute("errorMessage", 
                "Errore durante il salvataggio del logo: " + e.getMessage());
            model.addAttribute("pageTitle", testimonial.getId() == null ? "Nuova Testimonianza" : "Modifica Testimonianza");
            model.addAttribute("isEdit", testimonial.getId() != null);
            return "admin/testimonials/form";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", 
                "Errore durante il salvataggio: " + e.getMessage());
            model.addAttribute("pageTitle", testimonial.getId() == null ? "Nuova Testimonianza" : "Modifica Testimonianza");
            model.addAttribute("isEdit", testimonial.getId() != null);
            return "admin/testimonials/form";
        }
    }

    /**
     * Pubblicazione/Depubblicazione rapida dalla lista
     * POST /admin/testimonials/toggle-publish/{id}
     * 
     * @param id ID del testimonial
     * @param redirectAttributes Per messaggi flash
     * @return Redirect alla lista
     */
    @PostMapping("/toggle-publish/{id}")
    public String togglePublish(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Testimonial> testimonialOpt = testimonialService.getTestimonialById(id);
            
            if (testimonialOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Testimonianza non trovata");
                return "redirect:/admin/testimonials";
            }
            
            Testimonial testimonial = testimonialOpt.get();
            testimonial.setPublished(!testimonial.isPublished());
            
            // Imposta data di pubblicazione se viene pubblicato
            if (testimonial.isPublished() && testimonial.getPublishedDate() == null) {
                testimonial.setPublishedDate(LocalDateTime.now());
            }
            
            testimonialService.saveTestimonial(testimonial);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Testimonianza " + (testimonial.isPublished() ? "pubblicata" : "rimossa dalla pubblicazione") + " con successo!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Errore: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    /**
     * Cancellazione singolo testimonial
     * POST /admin/testimonials/delete/{id}
     * 
     * @param id ID del testimonial
     * @param redirectAttributes Per messaggi flash
     * @return Redirect alla lista
     */
    @PostMapping("/delete/{id}")
    public String deleteTestimonial(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Testimonial> testimonial = testimonialService.getTestimonialById(id);
            
            if (testimonial.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Testimonianza non trovata");
                return "redirect:/admin/testimonials";
            }
            
            // Elimina anche il logo associato se esiste
            if (testimonial.get().getLogoUrl() != null && !testimonial.get().getLogoUrl().isEmpty()) {
                fileStorageService.deleteFile(testimonial.get().getLogoUrl());
            }
            
            testimonialService.deleteTestimonial(id);
            redirectAttributes.addFlashAttribute("successMessage", "Testimonianza eliminata con successo!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Errore durante l'eliminazione: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    /**
     * Riordina testimonial - sposta su
     * POST /admin/testimonials/move-up/{id}
     * 
     * @param id ID del testimonial
     * @param redirectAttributes Per messaggi flash
     * @return Redirect alla lista
     */
    @PostMapping("/move-up/{id}")
    public String moveUp(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            testimonialService.moveUp(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ordine aggiornato!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Errore: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    /**
     * Riordina testimonial - sposta giù
     * POST /admin/testimonials/move-down/{id}
     * 
     * @param id ID del testimonial
     * @param redirectAttributes Per messaggi flash
     * @return Redirect alla lista
     */
    @PostMapping("/move-down/{id}")
    public String moveDown(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            testimonialService.moveDown(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ordine aggiornato!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Errore: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    /**
     * Eliminazione multipla testimonials
     * POST /admin/testimonials/delete-multiple
     * 
     * @param testimonialIds Array di ID dei testimonial da eliminare
     * @param redirectAttributes Per messaggi flash
     * @return Redirect alla lista
     */
    @PostMapping("/delete-multiple")
    public String deleteMultipleTestimonials(
            @RequestParam("testimonialIds") Long[] testimonialIds,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (testimonialIds == null || testimonialIds.length == 0) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Nessuna testimonianza selezionata");
                return "redirect:/admin/testimonials";
            }
            
            // Elimina i loghi associati
            for (Long id : testimonialIds) {
                Optional<Testimonial> testimonial = testimonialService.getTestimonialById(id);
                if (testimonial.isPresent() && testimonial.get().getLogoUrl() != null) {
                    fileStorageService.deleteFile(testimonial.get().getLogoUrl());
                }
            }
            
            int deletedCount = testimonialService.deleteTestimonials(testimonialIds);
            redirectAttributes.addFlashAttribute("successMessage", 
                deletedCount + " testimonianza/e eliminata/e con successo!");
                
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Errore durante l'eliminazione: " + e.getMessage());
        }
        
        return "redirect:/admin/testimonials";
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Valida il file logo
     * 
     * @param file File da validare
     * @return true se valido, false altrimenti
     */
    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // Verifica dimensione (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB in bytes
        if (file.getSize() > maxSize) {
            return false;
        }
        
        // Verifica tipo MIME
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/png") || 
               contentType.equals("image/jpg");
    }
}