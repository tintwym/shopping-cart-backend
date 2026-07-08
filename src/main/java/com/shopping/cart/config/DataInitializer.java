package com.shopping.cart.config;

import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.ProductImage;
import com.shopping.cart.entity.Role;
import com.shopping.cart.entity.User;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.RoleRepository;
import com.shopping.cart.repository.UserRepository;
import com.shopping.cart.utility.PasswordHashingUtility;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Value("${app.admin.seed-username:}")
    private String adminSeedUsername;

    @Value("${app.admin.seed-password:}")
    private String adminSeedPassword;

    public DataInitializer(
            RoleRepository roleRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRole("User");
        seedRole("Admin");
        seedSampleProducts();
        seedAdminUser();
    }

    private void seedRole(String name) {
        if (roleRepository.findByName(name) == null) {
            roleRepository.save(new Role(name));
        }
    }

    private void seedSampleProducts() {
        seedProductIfMissing("Wireless Headphones",
                "Noise-cancelling over-ear headphones with 30-hour battery life.",
                "129.00", 25, "https://picsum.photos/seed/headphones/600/600");
        seedProductIfMissing("Smart Watch",
                "Track fitness, sleep, and notifications on your wrist.",
                "249.00", 15, "https://picsum.photos/seed/watch/600/600");
        seedProductIfMissing("USB-C Hub",
                "7-in-1 adapter with HDMI, USB 3.0, and SD card reader.",
                "49.00", 40, "https://picsum.photos/seed/hub/600/600");
        seedProductIfMissing("Mechanical Keyboard",
                "Compact layout with hot-swappable switches.",
                "89.00", 20, "https://picsum.photos/seed/keyboard/600/600");
        seedProductIfMissing("Wireless Mouse",
                "Ergonomic silent-click mouse with multi-device pairing.",
                "39.00", 35, "https://picsum.photos/seed/mouse/600/600");
        seedProductIfMissing("Portable SSD 1TB",
                "USB 3.2 external drive for fast backups and travel.",
                "119.00", 18, "https://picsum.photos/seed/ssd/600/600");
        seedProductIfMissing("4K Webcam",
                "Auto-focus camera with built-in mic and privacy shutter.",
                "79.00", 22, "https://picsum.photos/seed/webcam/600/600");
        seedProductIfMissing("Bluetooth Speaker",
                "Water-resistant speaker with 12-hour playtime.",
                "59.00", 30, "https://picsum.photos/seed/speaker/600/600");
        seedProductIfMissing("27\" Gaming Monitor",
                "144Hz IPS panel with low-latency mode.",
                "329.00", 12, "https://picsum.photos/seed/monitor/600/600");
        seedProductIfMissing("Laptop Stand",
                "Aluminium riser with adjustable height and cable slot.",
                "45.00", 28, "https://picsum.photos/seed/stand/600/600");
        seedProductIfMissing("65W GaN Charger",
                "Compact dual-port USB-C charger for phone and laptop.",
                "34.00", 50, "https://picsum.photos/seed/charger/600/600");
        seedProductIfMissing("10\" Tablet",
                "Lightweight tablet for reading, notes, and streaming.",
                "199.00", 14, "https://picsum.photos/seed/tablet/600/600");
        seedProductIfMissing("Wireless Earbuds",
                "In-ear buds with active noise cancellation.",
                "99.00", 32, "https://picsum.photos/seed/earbuds/600/600");
        seedProductIfMissing("Smart Home Hub",
                "Control lights, sensors, and routines from one app.",
                "69.00", 16, "https://picsum.photos/seed/smarthub/600/600");
        seedProductIfMissing("Desk Ring Light",
                "Adjustable LED ring light for video calls and streaming.",
                "42.00", 24, "https://picsum.photos/seed/ringlight/600/600");
        seedProductIfMissing("Power Bank 20000mAh",
                "High-capacity battery with USB-C PD fast charging.",
                "55.00", 38, "https://picsum.photos/seed/powerbank/600/600");
    }

    private void seedProductIfMissing(
            String name, String description, String price, int stock, String imagePath) {
        if (productRepository.findByNameIgnoreCase(name).isPresent()) {
            return;
        }
        saveProduct(name, description, price, stock, imagePath);
    }

    private void saveProduct(String name, String description, String price, int stock, String imagePath) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setStock(stock);

        ProductImage image = new ProductImage(imagePath, name);
        image.setProduct(product);
        List<ProductImage> images = new ArrayList<>();
        images.add(image);
        product.setImages(images);

        productRepository.save(product);
    }

    private void seedAdminUser() {
        if (adminSeedUsername == null || adminSeedUsername.isBlank()
                || adminSeedPassword == null || adminSeedPassword.isBlank()) {
            return;
        }
        if (userRepository.findByUsername(adminSeedUsername) != null) {
            return;
        }
        Role adminRole = roleRepository.findByName("Admin");
        if (adminRole == null) {
            return;
        }
        User admin = new User();
        admin.setFirstName("Store");
        admin.setLastName("Admin");
        admin.setUsername(adminSeedUsername);
        admin.setEmail(adminSeedUsername + "@pixeltech.local");
        admin.setPassword(PasswordHashingUtility.hashPassword(adminSeedPassword));
        admin.setRole(adminRole);
        userRepository.save(admin);
    }
}
