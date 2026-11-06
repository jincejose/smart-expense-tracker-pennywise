package com.pennywise.tracker.web;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UserModelAdvice {
    @ModelAttribute
    public void addCurrentUser(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("currentUser", authentication.getName());
        }
    }
}
