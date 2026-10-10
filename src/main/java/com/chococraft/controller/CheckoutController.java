package com.chococraft.controller;

import com.chococraft.dto.CheckoutRequestDto;
import com.chococraft.entity.Cart;
import com.chococraft.entity.CustomerAddress;
import com.chococraft.entity.CustomerOrder;
import com.chococraft.entity.User;
import com.chococraft.repository.CustomerAddressRepository;
import com.chococraft.security.CustomUserDetails;
import com.chococraft.service.CartService;
import com.chococraft.service.OrderService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class CheckoutController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerAddressRepository addressRepository;

    @Value("${chococraft.store.free-delivery-threshold:499.00}")
    private BigDecimal freeDeliveryThreshold;

    @Value("${chococraft.store.flat-delivery-charge:50.00}")
    private BigDecimal flatDeliveryCharge;

    @GetMapping("/checkout")
    public String checkoutPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpSession session,
                               Model model) {
        User user = userDetails.getUser();
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");

        Cart cart = cartService.getOrCreateCart(user, sessionId);
        if (cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }

        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal deliveryCharge = subtotal.compareTo(freeDeliveryThreshold) >= 0 ? BigDecimal.ZERO : flatDeliveryCharge;
        BigDecimal grandTotal = subtotal.add(deliveryCharge);

        CheckoutRequestDto checkoutForm = new CheckoutRequestDto();
        checkoutForm.setFullName(user.getFullName());
        checkoutForm.setPhone(user.getPhone());

        // Pre-fill last saved address if present
        List<CustomerAddress> savedAddresses = addressRepository.findByUser(user);
        if (!savedAddresses.isEmpty()) {
            CustomerAddress last = savedAddresses.get(savedAddresses.size() - 1);
            checkoutForm.setAddressLine(last.getAddressLine());
            checkoutForm.setCity(last.getCity());
            checkoutForm.setState(last.getState());
            checkoutForm.setPincode(last.getPincode());
        }

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("deliveryCharge", deliveryCharge);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("checkoutForm", checkoutForm);
        model.addAttribute("savedAddresses", savedAddresses);

        return "checkout";
    }

    @PostMapping("/checkout/place")
    public String placeOrder(@Valid @ModelAttribute("checkoutForm") CheckoutRequestDto dto,
                             BindingResult bindingResult,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpSession session,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = userDetails.getUser();
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");
        Cart cart = cartService.getOrCreateCart(user, sessionId);

        if (bindingResult.hasErrors()) {
            BigDecimal subtotal = cart.getSubtotal();
            BigDecimal deliveryCharge = subtotal.compareTo(freeDeliveryThreshold) >= 0 ? BigDecimal.ZERO : flatDeliveryCharge;
            model.addAttribute("cart", cart);
            model.addAttribute("subtotal", subtotal);
            model.addAttribute("deliveryCharge", deliveryCharge);
            model.addAttribute("grandTotal", subtotal.add(deliveryCharge));
            return "checkout";
        }

        try {
            CustomerOrder order = orderService.createOrder(user, cart, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Order #" + order.getOrderNumber() + " placed successfully!");
            return "redirect:/orders/" + order.getId();
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/checkout";
        }
    }
}
