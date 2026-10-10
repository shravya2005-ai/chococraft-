package com.chococraft.service;

import com.chococraft.dto.CheckoutRequestDto;
import com.chococraft.dto.GiftBoxSelectionDto;
import com.chococraft.entity.*;
import com.chococraft.enums.BoxSize;
import com.chococraft.enums.OrderStatus;
import com.chococraft.enums.PaymentMethod;
import com.chococraft.enums.Role;
import com.chococraft.exception.InsufficientStockException;
import com.chococraft.repository.ProductRepository;
import com.chococraft.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CartAndOrderServiceTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(new User("Cart Tester", "carttester@example.com", "pass", "9999999999", Role.ROLE_CUSTOMER));
        
        testProduct = new Product();
        testProduct.setName("Test Dark Bar");
        testProduct.setDescription("Delicious test dark chocolate");
        testProduct.setPrice(new BigDecimal("200.00"));
        testProduct.setStockQuantity(20);
        testProduct.setActive(true);
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("Should add product to cart and update totals")
    void testAddToCart() {
        Cart cart = cartService.addProductToCart(testUser, null, testProduct.getId(), 2);
        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getTotalItemCount());
        assertEquals(new BigDecimal("400.00"), cart.getSubtotal());
    }

    @Test
    @DisplayName("Should fail adding quantity greater than available stock")
    void testAddExcessiveQuantityToCart() {
        assertThrows(InsufficientStockException.class, () -> 
            cartService.addProductToCart(testUser, null, testProduct.getId(), 50)
        );
    }

    @Test
    @DisplayName("Should create order, decrement stock, and restore stock on cancellation")
    void testOrderPlacementAndCancellation() {
        Cart cart = cartService.addProductToCart(testUser, null, testProduct.getId(), 5);
        int initialStock = testProduct.getStockQuantity(); // 20

        CheckoutRequestDto checkout = new CheckoutRequestDto();
        checkout.setFullName("Cart Tester");
        checkout.setPhone("9999999999");
        checkout.setAddressLine("123 Test Street");
        checkout.setCity("Bengaluru");
        checkout.setState("Karnataka");
        checkout.setPincode("560001");
        checkout.setPaymentMethod(PaymentMethod.COD);

        CustomerOrder order = orderService.createOrder(testUser, cart, checkout);
        assertNotNull(order.getId());
        assertEquals(OrderStatus.PLACED, order.getOrderStatus());
        
        // Stock must be decremented by 5 (20 - 5 = 15)
        Product updatedProduct = productRepository.findById(testProduct.getId()).get();
        assertEquals(initialStock - 5, updatedProduct.getStockQuantity());

        // Cancel order and verify stock restoration
        orderService.cancelOrder(order.getId(), testUser, "Test Cancellation");
        Product restoredProduct = productRepository.findById(testProduct.getId()).get();
        assertEquals(initialStock, restoredProduct.getStockQuantity()); // Back to 20
    }
}
