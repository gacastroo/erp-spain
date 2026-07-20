package com.ivan.erp.auth.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    private final boolean rememberMeEnabled;

    public AuthController(@Value("${app.security.remember-me.enabled:false}") boolean rememberMeEnabled) {
        this.rememberMeEnabled = rememberMeEnabled;
    }

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (loggedIn) {
            return "redirect:/dashboard";
        }

        model.addAttribute("rememberMeEnabled", rememberMeEnabled);
        return "auth/login";
    }
}
