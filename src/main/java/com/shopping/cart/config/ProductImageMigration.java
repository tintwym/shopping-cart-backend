package com.shopping.cart.config;

import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.ProductImage;
import com.shopping.cart.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Upgrades legacy local image filenames (e.g. seed-headphones.jpg) to HTTPS URLs.
 */
@Component
@Order(2)
public class ProductImageMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductImageMigration.class);

    private final ProductRepository productRepository;

    public ProductImageMigration(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int updated = 0;
        for (Product product : productRepository.findAllWithImages()) {
            if (product.getImages() == null || product.getImages().isEmpty()) {
                continue;
            }
            boolean changed = false;
            for (ProductImage image : product.getImages()) {
                String path = image.getPath();
                if (path == null || path.startsWith("http://") || path.startsWith("https://")) {
                    continue;
                }
                image.setPath(toPlaceholderUrl(path, product.getName()));
                changed = true;
                updated++;
            }
            if (changed) {
                productRepository.save(product);
            }
        }
        if (updated > 0) {
            log.info("Migrated {} product image path(s) to HTTPS placeholder URLs", updated);
        }
    }

    private String toPlaceholderUrl(String legacyPath, String productName) {
        String seed = legacyPath
                .replace("seed-", "")
                .replaceAll("\\.[^.]+$", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-");
        if (seed.isBlank()) {
            seed = productName == null ? "product" : productName.toLowerCase(Locale.ROOT)
                    .replaceAll("[^a-z0-9]+", "-");
        }
        return "https://picsum.photos/seed/" + seed + "/600/600";
    }
}
