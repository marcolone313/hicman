package com.osc.CorporateSite.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;

import com.osc.CorporateSite.Model.Contact;
import com.osc.CorporateSite.Service.ContactService;

import jakarta.validation.Valid;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @Autowired
    private ContactService contactService;

    @Autowired
    private MessageSource messageSource;

    // ModelAttribute globale per assicurare che l'oggetto contact sia sempre presente
    @ModelAttribute("contact")
    public Contact getContactObject() {
        return new Contact();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Home");
        return "index";
    }

    // Metodo POST per gestire l'invio del form dalla homepage
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
            model.addAttribute("pageTitle", "OSC Innovation - Home");
            return "index";
        }

        try {
            contactService.processContact(contact);

            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("contact.form.success", null, LocaleContextHolder.getLocale()));
            redirectAttributes.addFlashAttribute("gtmTrackingSuccess", true);

            return "redirect:/#contact";
        } catch (Exception e) {
            logger.error("Errore durante l'invio dell'email dalla homepage: ", e);

            // Se c'è un errore, mostra la pagina con l'errore invece del redirect
            model.addAttribute("errorMessage",
                    messageSource.getMessage("contact.form.error", null, LocaleContextHolder.getLocale()));
            model.addAttribute("pageTitle", "OSC Innovation - Home");
            return "index";
        }
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Chi Siamo");
        return "about";
    }

    @GetMapping("/privacy/")
    public String privacy() {
        return "redirect:https://www.iubenda.com/privacy-policy/94177263";
    }

    @GetMapping("/terms/")
    public String terms() {
        return "redirect:https://www.iubenda.com/termini-e-condizioni/94177263";
    }

    @GetMapping("/privacy")
    public String privacy1() {
        return "redirect:https://www.iubenda.com/privacy-policy/94177263";
    }

    @GetMapping("/terms")
    public String terms1() {
        return "redirect:https://www.iubenda.com/termini-e-condizioni/94177263";
    }

    @GetMapping("/playstation")
    public String playstation(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Case Playstation");
        return "playstation";
    }

    @GetMapping("/massenzio")
    public String massenzio(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Case Basilica di Massenzio");
        return "massenzio";
    }

    @GetMapping("/it/contatti/")
    public String contatti_it(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Contatti");
        return "contact";
    }

    @GetMapping("/en/contacts/")
    public String contatti_en(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Contatti");
        return "contact";
    }

    @GetMapping("/it/*/")
    public String it(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Home");
        return "index";
    }


    @GetMapping("/en/*/")
    public String en(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Home");
        return "index";
    }

    @GetMapping("/it/")
    public String it_plain(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Home");
        return "index";
    }

    @GetMapping("/en/")
    public String en_plain(Model model) {
        model.addAttribute("pageTitle", "OSC Innovation - Home");
        return "index";
    }

    // Endpoint per intercettare la richiesta all'URL esterno e servire l'immagine locale
    @GetMapping("/wp/wp-content/uploads/2024/09/Logo-black-1.jpg")
    @ResponseBody
    public ResponseEntity<byte[]> serveLogoImage() throws IOException {
        ClassPathResource imgFile = new ClassPathResource("static/images/Logo_Black.png");
        byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/png"));
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/wp/wp-content/uploads/2020/09/Icon_Ins-.png")
    @ResponseBody
    public ResponseEntity<byte[]> serveInstaImage() throws IOException {
        ClassPathResource imgFile = new ClassPathResource("static/images/instagram.png");
        byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/png"));
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/wp/wp-content/uploads/2020/09/Icon_Fb-.png")
    @ResponseBody
    public ResponseEntity<byte[]> serveFBImage() throws IOException {
        ClassPathResource imgFile = new ClassPathResource("static/images/facebook.png");
        byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/png"));
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/wp/wp-content/uploads/2020/09/Icon_In-.png")
    @ResponseBody
    public ResponseEntity<byte[]> serveLinkedinImage() throws IOException {
        ClassPathResource imgFile = new ClassPathResource("static/images/linkedin.png");
        byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/png"));
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/wp/wp-content/uploads/2020/09/Icon_YT-.png")
    @ResponseBody
    public ResponseEntity<byte[]> serveYTImage() throws IOException {
        ClassPathResource imgFile = new ClassPathResource("static/images/youtube.png");
        byte[] bytes = StreamUtils.copyToByteArray(imgFile.getInputStream());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/png"));
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}