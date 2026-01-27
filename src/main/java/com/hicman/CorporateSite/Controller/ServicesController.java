package com.hicman.CorporateSite.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller per la pagina Servizi
 */
@Controller
public class ServicesController {

    /**
     * Pagina Servizi principale
     */
    @GetMapping("/servizi")
    public String servizi(Model model) {
        model.addAttribute("pageTitle", "Hicman Capital Partner - I Nostri Servizi");
        return "servizi";
    }

    /**
     * Link diretti alle singole sezioni servizi (con anchor)
     */
    @GetMapping("/servizi/finanza-strutturata")
    public String finanzaStrutturata() {
        return "redirect:/servizi#finanza-strutturata";
    }

    @GetMapping("/servizi/finanza-speciale")
    public String finanzaSpeciale() {
        return "redirect:/servizi#finanza-speciale";
    }

    @GetMapping("/servizi/asset-alternativi")
    public String assetAlternativi() {
        return "redirect:/servizi#asset-alternativi";
    }
}