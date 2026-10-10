package com.chococraft.service;

import com.chococraft.dto.CheckoutRequestDto;
import com.chococraft.entity.*;
import com.chococraft.enums.OrderStatus;
import com.chococraft.enums.PaymentStatus;
import com.chococraft.exception.BadRequestException;
import com.chococraft.exception.InsufficientStockException;
import com.chococraft.exception.ResourceNotFoundException;
import com.chococraft.repository.CustomerAddressRepository;
import com.chococraft.repository.CustomerOrderRepository;
import com.chococraft.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private CustomerOrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerAddressRepository addressRepository;

    @Autowired
    private CartService cartService;

    @Value("${chococraft.store.free-delivery-threshold:499.00}")
    private BigDecimal freeDeliveryThreshold;

    @Value("${chococraft.store.flat-delivery-charge:50.00}")
    private BigDecimal flatDeliveryCharge;

    @Transactional
    public CustomerOrder createOrder(User user, Cart cart, CheckoutRequestDto dto) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Your cart is empty. Cannot place an order.");
        }

        // Validate stock for all products in cart first
        for (CartItem cartItem : cart.getItems()) {
            if (cartItem.getProduct() != null) {
                Product product = cartItem.getProduct();
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    throw new InsufficientStockException("Insufficient stock for '" + product.getName() + "'. Available: " + product.getStockQuantity());
                }
            } else if (cartItem.getCustomGiftBox() != null) {
                CustomGiftBox giftBox = cartItem.getCustomGiftBox();
                for (GiftBoxItem boxItem : giftBox.getItems()) {
                    Product product = boxItem.getProduct();
                    if (product.getStockQuantity() < boxItem.getQuantity()) {
                        throw new InsufficientStockException("Insufficient stock for custom gift box item '" + product.getName() + "'. Available: " + product.getStockQuantity());
                    }
                }
            }
        }

        // Recalculate subtotal on backend
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setOrderNumber("CC-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setOrderStatus(OrderStatus.PLACED);
        order.setOrderNotes(dto.getOrderNotes());
        order.setRecipientName(dto.getFullName());
        order.setRecipientPhone(dto.getPhone());

        String addressFormatted = String.format("%s, %s, %s - %s",
                dto.getAddressLine(), dto.getCity(), dto.getState(), dto.getPincode());
        order.setDeliveryAddress(addressFormatted);

        // Save address in user profile addresses for convenience
        CustomerAddress address = new CustomerAddress();
        address.setUser(user);
        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setAddressLine(dto.getAddressLine());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPincode(dto.getPincode());
        addressRepository.save(address);

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setQuantity(cartItem.getQuantity());

            if (cartItem.getProduct() != null) {
                Product product = cartItem.getProduct();
                orderItem.setProduct(product);
                orderItem.setProductNameSnapshot(product.getName());
                orderItem.setUnitPriceSnapshot(product.getPrice());

                BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                orderItem.setItemTotal(lineTotal);
                subtotal = subtotal.add(lineTotal);

                // Decrement stock safely
                product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
                productRepository.save(product);
            } else if (cartItem.getCustomGiftBox() != null) {
                CustomGiftBox giftBox = cartItem.getCustomGiftBox();
                orderItem.setProductNameSnapshot("Custom Chocolate Gift Box (" + giftBox.getBoxSize().getDisplayName() + ")");
                orderItem.setUnitPriceSnapshot(giftBox.getTotalPrice());

                BigDecimal lineTotal = giftBox.getTotalPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                orderItem.setItemTotal(lineTotal);
                subtotal = subtotal.add(lineTotal);

                // Build JSON/Text description of gift box contents for history
                StringBuilder details = new StringBuilder();
                details.append("Box Size: ").append(giftBox.getBoxSize().getDisplayName()).append("\n");
                if (giftBox.getPersonalizedMessage() != null && !giftBox.getPersonalizedMessage().isBlank()) {
                    details.append("Gift Message: ").append(giftBox.getPersonalizedMessage()).append("\n");
                }
                details.append("Contents:\n");
                for (GiftBoxItem boxItem : giftBox.getItems()) {
                    Product product = boxItem.getProduct();
                    details.append("- ").append(product.getName()).append(" x").append(boxItem.getQuantity()).append("\n");
                    // Decrement stock for chocolates inside custom box
                    product.setStockQuantity(product.getStockQuantity() - (boxItem.getQuantity() * cartItem.getQuantity()));
                    productRepository.save(product);
                }
                orderItem.setGiftBoxDetailsJson(details.toString());
            }
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setSubtotal(subtotal);

        // Delivery calculation
        BigDecimal deliveryCharge = subtotal.compareTo(freeDeliveryThreshold) >= 0 ? BigDecimal.ZERO : flatDeliveryCharge;
        order.setDeliveryCharge(deliveryCharge);
        order.setTotalAmount(subtotal.add(deliveryCharge));

        // Payment status based on method
        if (dto.getPaymentMethod() == com.chococraft.enums.PaymentMethod.MOCK_ONLINE) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        } else {
            order.setPaymentStatus(PaymentStatus.PENDING);
        }

        CustomerOrder savedOrder = orderRepository.save(order);

        // Clear cart after order creation
        cartService.clearCart(cart);

        return savedOrder;
    }

    public List<CustomerOrder> getOrdersForUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public CustomerOrder getOrderByIdAndUser(Long orderId, User user) {
        return orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found or access denied."));
    }

    public CustomerOrder getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    public Page<CustomerOrder> getAllOrdersForAdmin(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (status != null) {
            return orderRepository.findByOrderStatusOrderByOrderDateDesc(status, pageable);
        }
        return orderRepository.findAllByOrderByOrderDateDesc(pageable);
    }

    @Transactional
    public CustomerOrder updateOrderStatus(Long orderId, OrderStatus newStatus) {
        CustomerOrder order = getOrderById(orderId);
        OrderStatus currentStatus = order.getOrderStatus();

        if (currentStatus == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot change status of a cancelled order.");
        }

        if (newStatus == OrderStatus.CANCELLED) {
            return cancelOrder(orderId, order.getUser(), "Cancelled by Administrator");
        }

        order.setOrderStatus(newStatus);
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentMethod() == com.chococraft.enums.PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }

        return orderRepository.save(order);
    }

    @Transactional
    public CustomerOrder cancelOrder(Long orderId, User requestingUser, String reason) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.isCancellable()) {
            throw new BadRequestException("Order cannot be cancelled in status: " + order.getOrderStatus().getLabel());
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancellationReason(reason != null ? reason : "Cancelled by customer");
        if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        // Restore inventory for all items
        for (OrderItem item : order.getOrderItems()) {
            if (item.getProduct() != null) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        return orderRepository.save(order);
    }

    public long getPendingOrdersCount() {
        return orderRepository.countByOrderStatus(OrderStatus.PLACED) + orderRepository.countByOrderStatus(OrderStatus.CONFIRMED);
    }

    public long getCompletedOrdersCount() {
        return orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
    }

    public BigDecimal getTotalSalesRevenue() {
        BigDecimal total = orderRepository.calculateTotalSales();
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<CustomerOrder> getRecentOrders() {
        return orderRepository.findTop5ByOrderByOrderDateDesc();
    }
}
