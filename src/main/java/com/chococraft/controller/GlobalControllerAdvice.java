package com.chococraft.controller;

import com.chococraft.entity.Cart;
import com.chococraft.entity.User;
import com.chococraft.security.CustomUserDetails;
import com.chococraft.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.UUID;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @Value("${chococraft.store.name:ChocoCraft}")
    private String storeName;

    @Value("${chococraft.store.tagline:Handmade with Love, Delivered with Happiness.}")
    private String storeTagline;

    @Value("${chococraft.store.contact-email:support@chococraft.com}")
    private String storeEmail;

    @Value("${chococraft.store.contact-phone:+91 98765 43210}")
    private String storePhone;

    @Value("${chococraft.store.address:124 Chocolate Avenue, Sweet Delight Quarter, Bengaluru, Karnataka - 560001}")
    private String storeAddress;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session) {
        model.addAttribute("storeName", storeName);
        model.addAttribute("storeTagline", storeTagline);
        model.addAttribute("storeEmail", storeEmail);
        model.addAttribute("storePhone", storePhone);
        model.addAttribute("storeAddress", storeAddress);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = null;
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            currentUser = userDetails.getUser();
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isAdmin", currentUser.getRole().name().equals("ROLE_ADMIN"));
        } else {
            model.addAttribute("currentUser", null);
            model.addAttribute("isAdmin", false);
        }

        String sessionId = (String) session.getAttribute("SESSION_CART_ID");
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("SESSION_CART_ID", sessionId);
        }

        try {
            Cart cart = cartService.getOrCreateCart(currentUser, sessionId);
            model.addAttribute("cartItemCount", cart != null ? cart.getTotalItemCount() : 0);
        } catch (Exception e) {
            model.addAttribute("cartItemCount", 0);
        }
    }
}
