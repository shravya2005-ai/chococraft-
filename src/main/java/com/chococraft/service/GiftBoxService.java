package com.chococraft.service;

import com.chococraft.dto.GiftBoxSelectionDto;
import com.chococraft.entity.CustomGiftBox;
import com.chococraft.entity.GiftBoxItem;
import com.chococraft.entity.Product;
import com.chococraft.enums.BoxSize;
import com.chococraft.exception.BadRequestException;
import com.chococraft.exception.ResourceNotFoundException;
import com.chococraft.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class GiftBoxService {

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public CustomGiftBox createGiftBox(GiftBoxSelectionDto dto) {
        if (dto.getBoxSize() == null) {
            throw new BadRequestException("Please select a box size.");
        }

        BoxSize boxSize = dto.getBoxSize();
        int capacity = boxSize.getCapacity();

        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BadRequestException("Please select chocolates for your gift box.");
        }

        int totalSelected = dto.getItems().stream()
                .filter(i -> i.getQuantity() != null && i.getQuantity() > 0)
                .mapToInt(GiftBoxSelectionDto.GiftBoxItemSelectionDto::getQuantity)
                .sum();

        if (totalSelected != capacity) {
            throw new BadRequestException(String.format("You selected %d chocolates, but a %s requires exactly %d chocolates.",
                    totalSelected, boxSize.getDisplayName(), capacity));
        }

        CustomGiftBox giftBox = new CustomGiftBox(boxSize, dto.getPersonalizedMessage());
        BigDecimal calculatedTotal = BigDecimal.valueOf(boxSize.getBaseBoxPrice());

        List<GiftBoxItem> boxItems = new ArrayList<>();

        for (GiftBoxSelectionDto.GiftBoxItemSelectionDto itemDto : dto.getItems()) {
            if (itemDto.getQuantity() != null && itemDto.getQuantity() > 0) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemDto.getProductId()));

                if (!product.isActive()) {
                    throw new BadRequestException("Product '" + product.getName() + "' is currently inactive.");
                }

                if (product.getStockQuantity() < itemDto.getQuantity()) {
                    throw new BadRequestException("Not enough stock for '" + product.getName() + "'. Available: " + product.getStockQuantity());
                }

                GiftBoxItem boxItem = new GiftBoxItem(giftBox, product, itemDto.getQuantity());
                boxItems.add(boxItem);

                BigDecimal itemCost = product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
                calculatedTotal = calculatedTotal.add(itemCost);
            }
        }

        giftBox.setItems(boxItems);
        giftBox.setTotalPrice(calculatedTotal);

        return giftBox;
    }
}
