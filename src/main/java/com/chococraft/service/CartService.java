package com.chococraft.service;

import com.chococraft.dto.GiftBoxSelectionDto;
import com.chococraft.entity.*;
import com.chococraft.exception.BadRequestException;
import com.chococraft.exception.InsufficientStockException;
import com.chococraft.exception.ResourceNotFoundException;
import com.chococraft.repository.CartItemRepository;
import com.chococraft.repository.CartRepository;
import com.chococraft.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private GiftBoxService giftBoxService;

    @Transactional
    public Cart getOrCreateCart(User user, String sessionId) {
        if (user != null) {
            Optional<Cart> userCart = cartRepository.findByUser(user);
            if (userCart.isPresent()) {
                return userCart.get();
            }
            // Check if user had a session cart to associate
            if (sessionId != null) {
                Optional<Cart> sessionCart = cartRepository.findBySessionId(sessionId);
                if (sessionCart.isPresent()) {
                    Cart cart = sessionCart.get();
                    cart.setUser(user);
                    cart.setSessionId(null);
                    return cartRepository.save(cart);
                }
            }
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        } else if (sessionId != null) {
            return cartRepository.findBySessionId(sessionId)
                    .orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setSessionId(sessionId);
                        return cartRepository.save(cart);
                    });
        }
        throw new BadRequestException("Neither user nor session ID provided for cart access.");
    }

    @Transactional
    public Cart addProductToCart(User user, String sessionId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (!product.isActive()) {
            throw new BadRequestException("Product '" + product.getName() + "' is unavailable.");
        }

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Only " + product.getStockQuantity() + " units of " + product.getName() + " available in stock.");
        }

        Cart cart = getOrCreateCart(user, sessionId);

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + quantity;
            if (product.getStockQuantity() < newQty) {
                throw new InsufficientStockException("Cannot add " + quantity + " more. Stock limit of " + product.getStockQuantity() + " reached.");
            }
            item.setQuantity(newQty);
            item.setUnitPrice(product.getPrice());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(quantity);
            item.setUnitPrice(product.getPrice());
            cart.getItems().add(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart addGiftBoxToCart(User user, String sessionId, GiftBoxSelectionDto giftBoxDto) {
        CustomGiftBox giftBox = giftBoxService.createGiftBox(giftBoxDto);
        Cart cart = getOrCreateCart(user, sessionId);

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setCustomGiftBox(giftBox);
        item.setQuantity(1);
        item.setUnitPrice(giftBox.getTotalPrice());

        cart.getItems().add(item);
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(User user, String sessionId, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(user, sessionId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if (item.getProduct() != null) {
                if (item.getProduct().getStockQuantity() < quantity) {
                    throw new InsufficientStockException("Only " + item.getProduct().getStockQuantity() + " units available.");
                }
            }
            item.setQuantity(quantity);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(User user, String sessionId, Long cartItemId) {
        Cart cart = getOrCreateCart(user, sessionId);

        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
