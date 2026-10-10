package com.chococraft.controller;

import com.chococraft.dto.UserRegistrationDto;
import com.chococraft.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserRegistrationDto());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userForm") UserRegistrationDto dto,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Account created successfully! Please sign in with your email and password.");
            return "redirect:/login";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }
}
