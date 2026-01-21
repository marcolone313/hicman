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


    /*WIP controlla file controller.txt */

    /**
     * Homepage
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("pageTitle", "Hicman Capital Partner - WIP");
        return "wip";
    }


}