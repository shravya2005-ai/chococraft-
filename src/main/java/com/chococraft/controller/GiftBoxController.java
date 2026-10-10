package com.chococraft.controller;

import com.chococraft.dto.GiftBoxSelectionDto;
import com.chococraft.entity.User;
import com.chococraft.security.CustomUserDetails;
import com.chococraft.service.CartService;
import com.chococraft.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class GiftBoxController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @GetMapping("/gift-box")
    public String customGiftBoxBuilder(Model model) {
        model.addAttribute("availableChocolates", productService.getAllActiveProducts());
        if (!model.containsAttribute("giftBoxForm")) {
            model.addAttribute("giftBoxForm", new GiftBoxSelectionDto());
        }
        return "gift-box";
    }

    @PostMapping("/gift-box/add-to-cart")
    public String addCustomGiftBoxToCart(@ModelAttribute("giftBoxForm") GiftBoxSelectionDto dto,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        User user = userDetails != null ? userDetails.getUser() : null;
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");

        try {
            cartService.addGiftBoxToCart(user, sessionId, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Your customized chocolate gift box has been added to the cart!");
            return "redirect:/cart";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("giftBoxForm", dto);
            return "redirect:/gift-box";
        }
    }
}
