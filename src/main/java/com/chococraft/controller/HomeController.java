package com.chococraft.controller;

import com.chococraft.entity.ContactMessage;
import com.chococraft.service.CategoryService;
import com.chococraft.service.ContactService;
import com.chococraft.service.ProductService;
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
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ContactService contactService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getAllActiveCategories());
        model.addAttribute("featuredProducts", productService.getFeaturedProducts());
        model.addAttribute("bestsellers", productService.getBestsellerProducts());
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        if (!model.containsAttribute("contactMessage")) {
            model.addAttribute("contactMessage", new ContactMessage());
        }
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContact(@Valid @ModelAttribute("contactMessage") ContactMessage message,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "contact";
        }
        contactService.saveMessage(message);
        redirectAttributes.addFlashAttribute("successMessage", "Thank you for reaching out! We will get back to you shortly.");
        return "redirect:/contact";
    }
}
