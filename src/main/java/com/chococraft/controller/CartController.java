package com.chococraft.controller;

import com.chococraft.entity.Cart;
import com.chococraft.entity.User;
import com.chococraft.security.CustomUserDetails;
import com.chococraft.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @Value("${chococraft.store.free-delivery-threshold:499.00}")
    private BigDecimal freeDeliveryThreshold;

    @Value("${chococraft.store.flat-delivery-charge:50.00}")
    private BigDecimal flatDeliveryCharge;

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                           HttpSession session,
                           Model model) {
        User user = userDetails != null ? userDetails.getUser() : null;
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");

        Cart cart = cartService.getOrCreateCart(user, sessionId);
        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal deliveryCharge = subtotal.compareTo(freeDeliveryThreshold) >= 0 || subtotal.compareTo(BigDecimal.ZERO) == 0 
                ? BigDecimal.ZERO : flatDeliveryCharge;
        BigDecimal grandTotal = subtotal.add(deliveryCharge);

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("deliveryCharge", deliveryCharge);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("freeDeliveryThreshold", freeDeliveryThreshold);
        model.addAttribute("amountForFreeDelivery", freeDeliveryThreshold.subtract(subtotal));

        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam(name = "quantity", defaultValue = "1") Integer quantity,
                            @AuthenticationPrincipal CustomUserDetails userDetails,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        User user = userDetails != null ? userDetails.getUser() : null;
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");

        try {
            cartService.addProductToCart(user, sessionId, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to your cart successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateQuantity(@RequestParam("cartItemId") Long cartItemId,
                                 @RequestParam("quantity") Integer quantity,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = userDetails != null ? userDetails.getUser() : null;
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");

        try {
            cartService.updateItemQuantity(user, sessionId, cartItemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeItem(@RequestParam("cartItemId") Long cartItemId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = userDetails != null ? userDetails.getUser() : null;
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");

        cartService.removeItemFromCart(user, sessionId, cartItemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");

        return "redirect:/cart";
    }
}
