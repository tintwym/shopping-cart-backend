package com.shopping.cart.config;

import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.ProductImage;
import com.shopping.cart.entity.Role;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.RoleRepository;
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

    public DataInitializer(RoleRepository roleRepository, ProductRepository productRepository) {
        this.roleRepository = roleRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRole("User");
        seedRole("Admin");
        seedSampleProducts();
    }

    private void seedRole(String name) {
        if (roleRepository.findByName(name) == null) {
            roleRepository.save(new Role(name));
        }
    }

    private void seedSampleProducts() {
        if (productRepository.count() > 0) {
            return;
        }
        saveProduct("Wireless Headphones",
                "Noise-cancelling over-ear headphones with 30-hour battery life.",
                "129.00", 25, "seed-headphones.jpg");
        saveProduct("Smart Watch",
                "Track fitness, sleep, and notifications on your wrist.",
                "249.00", 15, "seed-watch.jpg");
        saveProduct("USB-C Hub",
                "7-in-1 adapter with HDMI, USB 3.0, and SD card reader.",
                "49.00", 40, "seed-hub.jpg");
        saveProduct("Mechanical Keyboard",
                "Compact layout with hot-swappable switches.",
                "89.00", 20, "seed-keyboard.jpg");
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
}
