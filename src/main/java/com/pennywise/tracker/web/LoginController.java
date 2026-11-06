package com.pennywise.tracker.web;

import com.pennywise.tracker.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {
    private final AccountService accountService;

    public LoginController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) return "register";
        try {
            accountService.createAccount(form.getUsername(), form.getPassword());
            return "redirect:/login?created";
        } catch (AccountService.AccountAlreadyExistsException ex) {
            model.addAttribute("accountExists", true);
            return "register";
        }
    }
}
