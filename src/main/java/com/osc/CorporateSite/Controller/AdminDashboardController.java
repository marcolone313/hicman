package com.osc.CorporateSite.Controller;

import com.osc.CorporateSite.Service.BlogService;
import com.osc.CorporateSite.Service.TestimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private TestimonialService testimonialService;

    /**
     * Dashboard principale admin
     */
    @GetMapping
    public String dashboard(Model model) {
        // Statistiche Blog
        long totalPosts = blogService.countAllPosts();
        long publishedPosts = blogService.countPublishedPosts();
        long draftPosts = blogService.countDraftPosts();
        
        // Statistiche Testimonial
        long totalTestimonials = testimonialService.countAllTestimonials();
        long publishedTestimonials = testimonialService.countPublishedTestimonials();
        
        // Ultimi post
        model.addAttribute("recentPosts", blogService.getLatestPosts(5));
        
        // Ultimi testimonial
        model.addAttribute("recentTestimonials", testimonialService.getLatestTestimonials(5));
        
        // Statistiche
        model.addAttribute("totalPosts", totalPosts);
        model.addAttribute("publishedPosts", publishedPosts);
        model.addAttribute("draftPosts", draftPosts);
        model.addAttribute("totalTestimonials", totalTestimonials);
        model.addAttribute("publishedTestimonials", publishedTestimonials);
        model.addAttribute("pageTitle", "Dashboard");
        
        return "admin/dashboard";
    }

    /**
     * Pagina di login
     */
    @GetMapping("/login")
    public String login() {
        return "admin/login";
    }
}