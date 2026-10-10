package com.chococraft.entity;

import com.chococraft.enums.BoxSize;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "custom_gift_boxes")
public class CustomGiftBox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BoxSize boxSize;

    @Column(columnDefinition = "TEXT")
    private String personalizedMessage;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @OneToMany(mappedBy = "customGiftBox", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GiftBoxItem> items = new ArrayList<>();

    public CustomGiftBox() {}

    public CustomGiftBox(BoxSize boxSize, String personalizedMessage) {
        this.boxSize = boxSize;
        this.personalizedMessage = personalizedMessage;
        this.totalPrice = BigDecimal.valueOf(boxSize.getBaseBoxPrice());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BoxSize getBoxSize() {
        return boxSize;
    }

    public void setBoxSize(BoxSize boxSize) {
        this.boxSize = boxSize;
    }

    public String getPersonalizedMessage() {
        return personalizedMessage;
    }

    public void setPersonalizedMessage(String personalizedMessage) {
        this.personalizedMessage = personalizedMessage;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<GiftBoxItem> getItems() {
        return items;
    }

    public void setItems(List<GiftBoxItem> items) {
        this.items = items;
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(GiftBoxItem::getQuantity).sum();
    }
}
