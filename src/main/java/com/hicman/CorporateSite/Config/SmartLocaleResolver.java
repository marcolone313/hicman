package com.hicman.CorporateSite.Config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.LocaleResolver;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

public class SmartLocaleResolver implements LocaleResolver {
    
    private static final String LOCALE_SESSION_ATTRIBUTE_NAME = "MY_LOCALE";
    private static final ConcurrentHashMap<String, Locale> localeCache = new ConcurrentHashMap<>();
    
    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
        new Locale("it"),
        new Locale("en")
    );

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        // Check session cache first
        if (session != null) {
            Locale sessionLocale = (Locale) session.getAttribute(LOCALE_SESSION_ATTRIBUTE_NAME);
            if (sessionLocale != null) {
                return sessionLocale;
            }
        }

        // Check browser locale
        String browserLang = request.getLocale().getLanguage();
        return localeCache.computeIfAbsent(browserLang, lang -> 
            SUPPORTED_LOCALES.stream()
                .filter(locale -> locale.getLanguage().equals(lang))
                .findFirst()
                .orElse(SUPPORTED_LOCALES.get(0))
        );
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        request.getSession().setAttribute(LOCALE_SESSION_ATTRIBUTE_NAME, locale);
    }
}