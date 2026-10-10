package com.chococraft.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "gift_box_items")
public class GiftBoxItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_box_id", nullable = false)
    private CustomGiftBox customGiftBox;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public GiftBoxItem() {}

    public GiftBoxItem(CustomGiftBox customGiftBox, Product product, Integer quantity) {
        this.customGiftBox = customGiftBox;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CustomGiftBox getCustomGiftBox() {
        return customGiftBox;
    }

    public void setCustomGiftBox(CustomGiftBox customGiftBox) {
        this.customGiftBox = customGiftBox;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
