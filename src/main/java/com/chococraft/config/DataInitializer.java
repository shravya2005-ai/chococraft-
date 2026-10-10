package com.chococraft.config;

import com.chococraft.entity.Category;
import com.chococraft.entity.Product;
import com.chococraft.entity.User;
import com.chococraft.enums.Role;
import com.chococraft.repository.CategoryRepository;
import com.chococraft.repository.ProductRepository;
import com.chococraft.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing ChocoCraft demonstration sample data...");

        // 1. Seed Users (Admin & Customer)
        if (!userRepository.existsByEmail("admin@chococraft.com")) {
            User admin = new User("ChocoCraft Admin", "admin@chococraft.com", passwordEncoder.encode("Admin@123"), "+91 98765 00000", Role.ROLE_ADMIN);
            userRepository.save(admin);
            logger.info("Demo Admin Created: admin@chococraft.com / Admin@123");
        }

        if (!userRepository.existsByEmail("customer@chococraft.com")) {
            User customer = new User("Priya Sharma", "customer@chococraft.com", passwordEncoder.encode("Customer@123"), "+91 98765 11111", Role.ROLE_CUSTOMER);
            userRepository.save(customer);
            logger.info("Demo Customer Created: customer@chococraft.com / Customer@123");
        }

        // 2. Seed Categories (100% Chocolate imagery only)
        if (categoryRepository.count() == 0) {
            Category dark = categoryRepository.save(new Category("Dark Chocolate", "dark-chocolate", "Rich 70%+ Cocoa artisan dark chocolates crafted for true connoisseurs.", "https://images.unsplash.com/photo-1548907040-4baa42d10919?auto=format&fit=crop&w=600&q=80"));
            Category milk = categoryRepository.save(new Category("Milk Chocolate", "milk-chocolate", "Smooth, creamy, melt-in-the-mouth homemade milk chocolates.", "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?auto=format&fit=crop&w=600&q=80"));
            Category white = categoryRepository.save(new Category("White Chocolate", "white-chocolate", "Velvety white chocolates infused with natural vanilla bean.", "https://images.unsplash.com/photo-1581798459219-318e76aecc7b?auto=format&fit=crop&w=600&q=80"));
            Category truffles = categoryRepository.save(new Category("Truffles", "truffles", "Decadent handmade chocolate truffles dusted with gourmet cocoa powder.", "https://images.unsplash.com/photo-1549007994-cb92caebd54b?auto=format&fit=crop&w=600&q=80"));
            Category giftBoxes = categoryRepository.save(new Category("Gift Boxes", "gift-boxes", "Handcrafted customizable luxury gift boxes for special occasions.", "https://images.unsplash.com/photo-1481391319762-47dff72954d9?auto=format&fit=crop&w=600&q=80"));
            Category hampers = categoryRepository.save(new Category("Hampers", "hampers", "Grand festive hampers filled with artisanal chocolates and nuts.", "https://images.unsplash.com/photo-1526080652727-5b77f74eacd2?auto=format&fit=crop&w=600&q=80"));

            // 3. Seed Products (Verified chocolate photos only)
            createProduct("Classic Dark Chocolate",
                    "Pure 72% Belgian dark cocoa handcrafted into sleek chocolate slabs with rich antioxidant goodness.",
                    new BigDecimal("199.00"), "150g", "https://images.unsplash.com/photo-1548907040-4baa42d10919?auto=format&fit=crop&w=600&q=80",
                    "Cocoa Mass, Cocoa Butter, Organic Cane Sugar, Vanilla Extract",
                    "Contains Soy Lecithin. Produced in a facility handling tree nuts.",
                    "Store in a cool, dry place between 15°C - 18°C.", 40, dark, true, true, 4.9);

            createProduct("Milk Chocolate Bites",
                    "Bite-sized creamy milk chocolates made with fresh dairy milk and pure natural cocoa butter.",
                    new BigDecimal("249.00"), "200g (16 Pieces)", "https://images.unsplash.com/photo-1541781774459-bb2af2f05b55?auto=format&fit=crop&w=600&q=80",
                    "Whole Milk Solids, Sugar, Cocoa Butter, Cocoa Mass",
                    "Contains Milk, Soy. May contain traces of peanut.",
                    "Keep refrigerated or store in cool room below 20°C.", 35, milk, true, true, 4.8);

            createProduct("Homemade Chocolate Truffles",
                    "Hand-rolled rich ganache truffles coated in velvety dark cocoa and toasted hazelnut crumbs.",
                    new BigDecimal("399.00"), "250g (12 Pieces)", "https://images.unsplash.com/photo-1549007994-cb92caebd54b?auto=format&fit=crop&w=600&q=80",
                    "Dark Chocolate, Heavy Cream, Butter, Cocoa Powder, Hazelnut Crumb",
                    "Contains Dairy, Tree Nuts (Hazelnut).",
                    "Best consumed within 14 days of opening. Keep chilled.", 25, truffles, true, true, 5.0);

            createProduct("Almond Crunch Chocolate",
                    "Slow-roasted California almonds smothered in smooth dark chocolate with a hint of sea salt.",
                    new BigDecimal("299.00"), "180g", "https://images.unsplash.com/photo-1565071783280-719b01b29912?auto=format&fit=crop&w=600&q=80",
                    "Roasted Almonds, Cocoa Mass, Sugar, Sea Salt, Cocoa Butter",
                    "Contains Tree Nuts (Almonds), Soy.",
                    "Store away from direct sunlight.", 30, dark, true, false, 4.7);

            createProduct("Hazelnut Chocolate Slabs",
                    "Piedmont hazelnuts layered over rich milk chocolate for a crunch in every single bite.",
                    new BigDecimal("349.00"), "200g", "https://images.unsplash.com/photo-1511381939415-e44015466834?auto=format&fit=crop&w=600&q=80",
                    "Milk Chocolate, Whole Toasted Hazelnuts, Cocoa Butter",
                    "Contains Milk, Tree Nuts (Hazelnuts).",
                    "Keep in cool, dry place.", 18, milk, false, true, 4.9);

            createProduct("White Chocolate Hearts",
                    "Adorable heart-shaped white chocolate pralines infused with wild strawberry essence.",
                    new BigDecimal("279.00"), "150g (12 Hearts)", "https://images.unsplash.com/photo-1581798459219-318e76aecc7b?auto=format&fit=crop&w=600&q=80",
                    "Cocoa Butter, Sugar, Whole Milk Powder, Natural Strawberry Flavor",
                    "Contains Milk, Soy.",
                    "Store below 18°C.", 22, white, true, false, 4.8);

            createProduct("Assorted Chocolate Gift Box",
                    "Curated selection of 16 handcrafted chocolates including truffles, nut crunches, and praline bites.",
                    new BigDecimal("599.00"), "350g (16 Pieces)", "https://images.unsplash.com/photo-1481391319762-47dff72954d9?auto=format&fit=crop&w=600&q=80",
                    "Assorted Cocoa Products, Dairy Solids, Roasted Almonds, Hazelnuts, Berries",
                    "Contains Dairy, Tree Nuts, Soy.",
                    "Keep in a cool place away from heat sources.", 15, giftBoxes, true, true, 4.9);

            createProduct("Festive Luxury Chocolate Hamper",
                    "Premium wooden gift hamper box featuring dark bars, truffle jar, almond clusters, and hot cocoa mix.",
                    new BigDecimal("999.00"), "750g Box", "https://images.unsplash.com/photo-1526080652727-5b77f74eacd2?auto=format&fit=crop&w=600&q=80",
                    "Dark Chocolate, Truffles, Almond Cluster, Gourmet Hot Chocolate Powder",
                    "Contains Dairy, Nuts, Soy.",
                    "Store in cool ambient temperature.", 8, hampers, true, true, 5.0);

            createProduct("Caramel Silk Delight",
                    "Soft buttery caramel oozing out of silky milk chocolate shells with sea salt crystals.",
                    new BigDecimal("229.00"), "160g (10 Pieces)", "https://images.unsplash.com/photo-1605807646983-377bc5a76493?auto=format&fit=crop&w=600&q=80",
                    "Milk Chocolate, Liquid Caramel, Butter, Condensed Milk, Sea Salt",
                    "Contains Dairy, Soy.",
                    "Store below 20°C.", 28, milk, false, false, 4.6);

            createProduct("Berry Fusion Dark Bar",
                    "70% dark chocolate bar studded with freeze-dried raspberries, cranberries, and chia seeds.",
                    new BigDecimal("319.00"), "150g", "https://images.unsplash.com/photo-1623341214825-9f4f963727da?auto=format&fit=crop&w=600&q=80",
                    "Dark Cocoa Mass, Dried Raspberries, Dried Cranberries, Chia Seeds, Organic Sugar",
                    "May contain traces of nuts and milk.",
                    "Store in cool, dry place.", 20, dark, false, true, 4.8);

            logger.info("Sample chocolate products initialized successfully!");
        }
    }

    private void createProduct(String name, String desc, BigDecimal price, String weight, String img,
                               String ingredients, String allergen, String storage, int stock,
                               Category category, boolean featured, boolean bestseller, double rating) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setWeight(weight);
        p.setImageUrl(img);
        p.setIngredients(ingredients);
        p.setAllergenInformation(allergen);
        p.setStorageInstructions(storage);
        p.setStockQuantity(stock);
        p.setCategory(category);
        p.setActive(true);
        p.setFeatured(featured);
        p.setBestseller(bestseller);
        p.setRating(rating);
        productRepository.save(p);
    }
}
