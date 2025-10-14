package com.hicman.CorporateSite.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * Configurazione Web per Spring MVC
 * Gestisce i18n (internazionalizzazione) e risorse statiche
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Configura il LocaleResolver per gestire la lingua
     * Usa i cookie per mantenere la preferenza dell'utente
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.ITALIAN); // Lingua di default: Italiano
        resolver.setCookieName("lang"); // Nome del cookie
        resolver.setCookieMaxAge(Duration.ofDays(365)); // Cookie valido 1 anno
        return resolver;
    }

    /**
     * Interceptor per cambiare la lingua tramite parametro URL (?lang=it)
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang"); // Parametro URL per cambiare lingua
        return interceptor;
    }

    /**
     * Registra l'interceptor per il cambio lingua
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Configura il MessageSource per i18n
     * Legge i file messages_*.properties
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = 
            new ReloadableResourceBundleMessageSource();
        
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setUseCodeAsDefaultMessage(false);
        
        // Cache refresh in development (3 secondi)
        // In produzione impostare a -1 per cache permanente
        messageSource.setCacheSeconds(3);
        
        return messageSource;
    }

    /**
     * Configura le risorse statiche (immagini caricate)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Espone la cartella uploads per le immagini caricate
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}