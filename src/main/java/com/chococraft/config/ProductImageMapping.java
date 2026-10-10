package com.chococraft.config;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ProductImageMapping {

    public static final String DEFAULT_PLACEHOLDER = "/images/placeholder-chocolate.jpg";

    private static final Map<String, String> PRODUCT_IMAGE_MAP;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("classic-dark-chocolate", "/images/products/classic-dark-chocolate.jpg");
        map.put("milk-chocolate-bites", "/images/products/milk-chocolate-bites.jpg");
        map.put("homemade-chocolate-truffles", "/images/products/homemade-chocolate-truffles.jpg");
        map.put("almond-crunch-chocolate", "/images/products/almond-crunch-chocolate.jpg");
        map.put("hazelnut-chocolate", "/images/products/hazelnut-chocolate.jpg");
        map.put("hazelnut-chocolate-slabs", "/images/products/hazelnut-chocolate.jpg");
        map.put("white-chocolate-hearts", "/images/products/white-chocolate-hearts.jpg");
        map.put("assorted-chocolate-gift-box", "/images/products/assorted-chocolate-gift-box.jpg");
        map.put("festive-chocolate-hamper", "/images/products/festive-chocolate-hamper.jpg");
        map.put("festive-luxury-chocolate-hamper", "/images/products/festive-chocolate-hamper.jpg");
        map.put("caramel-silk-delight", "/images/products/caramel-silk-delight.jpg");
        map.put("berry-fusion-dark-bar", "/images/products/berry-fusion-dark-bar.jpg");

        PRODUCT_IMAGE_MAP = Collections.unmodifiableMap(map);
    }

    public static String toSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    public static String getImagePathForProduct(String productName) {
        if (productName == null || productName.isBlank()) {
            return DEFAULT_PLACEHOLDER;
        }
        String slug = toSlug(productName);
        if (PRODUCT_IMAGE_MAP.containsKey(slug)) {
            return PRODUCT_IMAGE_MAP.get(slug);
        }

        // Fuzzy key match fallback
        for (Map.Entry<String, String> entry : PRODUCT_IMAGE_MAP.entrySet()) {
            if (slug.contains(entry.getKey()) || entry.getKey().contains(slug)) {
                return entry.getValue();
            }
        }

        return DEFAULT_PLACEHOLDER;
    }

    public static Map<String, String> getAllMappings() {
        return PRODUCT_IMAGE_MAP;
    }
}
