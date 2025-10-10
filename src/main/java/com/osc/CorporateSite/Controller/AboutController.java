package com.osc.CorporateSite.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller per la pagina Chi Siamo
 */
@Controller
public class AboutController {

    /**
     * Pagina Chi Siamo
     */
    @GetMapping("/chi-siamo")
    public String chiSiamo(Model model) {
        model.addAttribute("pageTitle", "Hicman Capital Partner - Chi Siamo");
        return "chi-siamo";
    }

    /**
     * Redirect per compatibilità con vecchio URL
     */
    @GetMapping("/about")
    public String aboutRedirect() {
        return "redirect:/chi-siamo";
    }
}