package com.chococraft.controller.api;

import com.chococraft.dto.ApiResponse;
import com.chococraft.entity.Cart;
import com.chococraft.entity.User;
import com.chococraft.security.CustomUserDetails;
import com.chococraft.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartRestController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<Cart>> getCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                     HttpSession session) {
        User user = userDetails != null ? userDetails.getUser() : null;
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");
        Cart cart = cartService.getOrCreateCart(user, sessionId);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cart));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Cart>> addToCart(@RequestParam("productId") Long productId,
                                                        @RequestParam(name = "quantity", defaultValue = "1") Integer quantity,
                                                        @AuthenticationPrincipal CustomUserDetails userDetails,
                                                        HttpSession session) {
        User user = userDetails != null ? userDetails.getUser() : null;
        String sessionId = (String) session.getAttribute("SESSION_CART_ID");
        Cart cart = cartService.addProductToCart(user, sessionId, productId, quantity);
        return ResponseEntity.ok(ApiResponse.success("Product added to cart", cart));
    }
}
