package com.shopping.cart.service;

import com.shopping.cart.dto.request.AddProductRequest;
import com.shopping.cart.dto.request.UpdateProductRequest;
import com.shopping.cart.entity.ProductImage;
import com.shopping.cart.entity.Product;
import com.shopping.cart.interfaces.IProductService;
import com.shopping.cart.repository.ProductRepository;
import com.stripe.Stripe;
import com.stripe.model.Price;
import com.stripe.param.PriceCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final String uploadDir = "src/main/resources/static/images/products/";

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Initialize Stripe with the API key from application.properties
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public List<Product> getAllProducts() {
        // Return all products from database with isDeleted column false
        return productRepository.findByIsDeletedFalse();
    }

    @Override
    public Product getProductById(UUID id) {
        // Find product by ID and return it if found, otherwise return null
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public Product store(AddProductRequest addProductRequest, MultipartFile[] images) {
        Product product = new Product();
        product.setName(addProductRequest.getName());
        product.setDescription(addProductRequest.getDescription());
        product.setPrice(addProductRequest.getPrice());
        product.setStock(addProductRequest.getStock());

        List<ProductImage> productImages = new ArrayList<>();

        // Handle image saving (local storage)
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                try {
                    Path uploadPath = Paths.get(uploadDir + fileName);
                    Files.createDirectories(uploadPath.getParent());
                    Files.copy(image.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

                    ProductImage productImage = new ProductImage(fileName, "Alt text for " + fileName);
                    productImage.setProduct(product);
                    productImages.add(productImage);

                } catch (IOException e) {
                    throw new RuntimeException("Failed to save image: " + fileName, e);
                }
            }
        }

        product.setImages(productImages);

        // Create product in Stripe
        String stripeProductId = createStripeProduct(addProductRequest.getName(), addProductRequest.getDescription());
        product.setStripeProductId(stripeProductId); // Assuming you have this field in your Product entity

        // Create price in Stripe
        String stripePriceId = createStripePrice(stripeProductId, addProductRequest.getPrice().doubleValue());
        product.setStripePriceId(stripePriceId); // Assuming you have this field in your Product entity

        // Save product in database
        return productRepository.save(product);
    }

    private String createStripeProduct(String name, String description) {
        try {
            // Fully qualify the correct class for ProductCreateParams
            com.stripe.param.ProductCreateParams params = com.stripe.param.ProductCreateParams.builder()
                    .setName(name)
                    .setDescription(description)
                    .build();

            // Fully qualify the Stripe Product class to avoid conflict with your entity
            com.stripe.model.Product stripeProduct = com.stripe.model.Product.create(params);
            return stripeProduct.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create product in Stripe", e);
        }
    }

    private String createStripePrice(String productId, Double amount) {
        try {
            PriceCreateParams params = PriceCreateParams.builder()
                    .setProduct(productId)
                    .setUnitAmount((long) (amount * 100)) // Amount in cents
                    .setCurrency("sgd")
                    .build();

            Price stripePrice = Price.create(params);
            return stripePrice.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create price in Stripe", e);
        }
    }

    @Override
    public Product update(UUID id, UpdateProductRequest updateProductRequest, MultipartFile[] newImages) {
        // Find the product by ID
        Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));

        // Update the product details from the request
        product.setName(updateProductRequest.getName());
        product.setDescription(updateProductRequest.getDescription());
        product.setPrice(updateProductRequest.getPrice());
        product.setStock(updateProductRequest.getStock());

        // If there are new images, handle image saving
        if (newImages != null && newImages.length > 0) {
            List<ProductImage> productImages = product.getImages();

            // Save new images
            for (MultipartFile newImage : newImages) {
                if (!newImage.isEmpty()) {
                    String fileName = UUID.randomUUID() + "_" + newImage.getOriginalFilename();
                    try {
                        Path uploadPath = Paths.get(uploadDir + fileName);
                        Files.createDirectories(uploadPath.getParent());
                        Files.copy(newImage.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

                        ProductImage productImage = new ProductImage(fileName, "Alt text for " + fileName);
                        productImage.setProduct(product);
                        productImages.add(productImage);

                    } catch (IOException e) {
                        throw new RuntimeException("Failed to save image: " + fileName, e);
                    }
                }
            }

            // Update product's image list
            product.setImages(productImages);
        }

        // Save updated product to the database and return the saved product
        return productRepository.save(product);
    }

    @Override
    public void delete(UUID id) {
        // Find product by ID
        productRepository.findById(id).ifPresent(product -> {
            // set isDeleted to true
            product.setDeleted(true);

            // save the product
            productRepository.save(product);
        });
    }
}
