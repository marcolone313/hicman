package com.hicman.CorporateSite.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hicman.CorporateSite.Model.Contact;
import com.hicman.CorporateSite.Service.ContactService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

@Controller
@RequestMapping("/contact")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    @Autowired
    private ContactService contactService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String showContactForm(Model model) {
        // Assicurati sempre che ci sia un oggetto contact
        if (!model.containsAttribute("contact")) {
            model.addAttribute("contact", new Contact());
        }
        model.addAttribute("pageTitle", "OSC Innovation - Contatti");
        return "contact";
    }

    @PostMapping
    public String processContact(@Valid @ModelAttribute("contact") Contact contact,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        // Verifica honeypot - se compilato, è probabilmente un bot
        if (StringUtils.hasLength(contact.getWebsite())) {
            logger.info("Rilevato tentativo di spam tramite honeypot");
            // Reindirizza alla pagina di contatto senza mostrare errori
            // Simula una risposta di successo per non far capire al bot che è stato bloccato
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("contact.form.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/contact";
        }
        
        if (result.hasErrors()) {
            // Quando ci sono errori, non fare redirect ma mostra direttamente la pagina
            model.addAttribute("contact", contact);
            model.addAttribute("errorMessage", 
                messageSource.getMessage("contact.form.error", null, LocaleContextHolder.getLocale()));
            model.addAttribute("pageTitle", "OSC Innovation - Contatti");
            
            // Aggiungi i risultati di validazione al model
            model.addAttribute("org.springframework.validation.BindingResult.contact", result);
            
            return "contact"; // Ritorna direttamente la vista
        }

        try {
            contactService.processContact(contact);
            redirectAttributes.addFlashAttribute("successMessage", 
                messageSource.getMessage("contact.form.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/contact";
        } catch (Exception e) {
            logger.error("Errore durante l'invio dell'email: ", e);
            
            // In caso di errore, mostra la pagina con l'errore
            model.addAttribute("contact", contact);
            model.addAttribute("errorMessage", 
                messageSource.getMessage("contact.form.error", null, LocaleContextHolder.getLocale()));
            model.addAttribute("pageTitle", "OSC Innovation - Contatti");
            return "contact";
        }
    }
}