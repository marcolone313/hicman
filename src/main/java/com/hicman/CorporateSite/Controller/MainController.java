package com.hicman.CorporateSite.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hicman.CorporateSite.Model.Contact;
import com.hicman.CorporateSite.Service.ContactService;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private ContactService contactService;

    @Autowired
    private MessageSource messageSource;

    /**
     * ModelAttribute globale per assicurare che l'oggetto contact sia sempre presente
     */
    @ModelAttribute("contact")
    public Contact getContactObject() {
        return new Contact();
    }

    /**
     * Homepage
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Hicman Capital Partner - Finanza Strutturata e Speciale");
        return "index";
    }

    @GetMapping("/2")
    public String index2(Model model) {
        model.addAttribute("pageTitle", "Hicman Capital Partner - Finanza Strutturata e Speciale");
        return "index2";
    }

    @GetMapping("/3")
    public String index3(Model model) {
        model.addAttribute("pageTitle", "Hicman Capital Partner - Finanza Strutturata e Speciale");
        return "index3";
    }

    /**
     * Gestione invio form contatti dalla homepage
     */
    @PostMapping("/")
    public String processContactFromHome(@Valid @ModelAttribute("contact") Contact contact,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Verifica honeypot - se compilato, è probabilmente un bot
        if (StringUtils.hasLength(contact.getWebsite())) {
            logger.info("Rilevato tentativo di spam tramite honeypot dalla homepage");
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("contact.form.success", null, LocaleContextHolder.getLocale()));
            return "redirect:/#contact";
        }

        if (result.hasErrors()) {
            // In caso di errori, resta sulla stessa pagina senza redirect
            model.addAttribute("errorMessage",
                    messageSource.getMessage("contact.form.error", null, LocaleContextHolder.getLocale()));
            model.addAttribute("pageTitle", "Hicman Capital Partner - Finanza Strutturata e Speciale");
            return "index";
        }

        try {
            contactService.processContact(contact);

            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("contact.form.success", null, LocaleContextHolder.getLocale()));

            return "redirect:/#contact";
        } catch (Exception e) {
            logger.error("Errore durante l'invio dell'email dalla homepage: ", e);

            // Se c'è un errore, mostra la pagina con l'errore invece del redirect
            model.addAttribute("errorMessage",
                    messageSource.getMessage("contact.form.error", null, LocaleContextHolder.getLocale()));
            model.addAttribute("pageTitle", "Hicman Capital Partner - Finanza Strutturata e Speciale");
            return "index";
        }
    }

    /**
     * Privacy Policy - redirect esterno
     */
    @GetMapping("/privacy")
    public String privacy() {
        return "redirect:https://www.iubenda.com/privacy-policy/XXXXXX"; // Da configurare con ID reale
    }

    /**
     * Termini e Condizioni - redirect esterno
     */
    @GetMapping("/terms")
    public String terms() {
        return "redirect:https://www.iubenda.com/termini-e-condizioni/XXXXXX"; // Da configurare con ID reale
    }
}