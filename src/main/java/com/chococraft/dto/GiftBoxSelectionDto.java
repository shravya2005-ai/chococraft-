package com.chococraft.dto;

import com.chococraft.enums.BoxSize;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class GiftBoxSelectionDto {

    @NotNull(message = "Box size must be selected")
    private BoxSize boxSize;

    private String personalizedMessage;

    private List<GiftBoxItemSelectionDto> items = new ArrayList<>();

    public GiftBoxSelectionDto() {}

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

    public List<GiftBoxItemSelectionDto> getItems() {
        return items;
    }

    public void setItems(List<GiftBoxItemSelectionDto> items) {
        this.items = items;
    }

    public static class GiftBoxItemSelectionDto {
        private Long productId;
        private Integer quantity;

        public GiftBoxItemSelectionDto() {}

        public GiftBoxItemSelectionDto(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
