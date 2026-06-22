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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CloudinaryImageService cloudinaryImageService;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public ProductService(
            ProductRepository productRepository,
            CloudinaryImageService cloudinaryImageService) {
        this.productRepository = productRepository;
        this.cloudinaryImageService = cloudinaryImageService;
    }

    // Initialize Stripe with the API key from application.properties
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findByIsDeletedFalseWithImages();
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(UUID id) {
        return productRepository.findActiveByIdWithImages(Objects.requireNonNull(id)).orElse(null);
    }

    @Override
    public Product store(AddProductRequest addProductRequest, MultipartFile[] images) {
        Product product = new Product();
        product.setName(addProductRequest.getName());
        product.setDescription(addProductRequest.getDescription());
        product.setPrice(addProductRequest.getPrice());
        product.setStock(addProductRequest.getStock());

        List<ProductImage> productImages = new ArrayList<>();

        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryImageService.upload(image);
                String altText = image.getOriginalFilename() != null
                        ? image.getOriginalFilename()
                        : product.getName();
                ProductImage productImage = new ProductImage(imageUrl, altText);
                productImage.setProduct(product);
                productImages.add(productImage);
            }
        }

        product.setImages(productImages);

        // Create product in Stripe
        String stripeProductId = createStripeProduct(addProductRequest.getName(), addProductRequest.getDescription());
        product.setStripeProductId(stripeProductId); // Assuming you have this field in your Product entity

        // Create price in Stripe
        String stripePriceId = createStripePrice(stripeProductId, addProductRequest.getPrice());
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

    private String createStripePrice(String productId, BigDecimal amount) {
        try {
            PriceCreateParams params = PriceCreateParams.builder()
                    .setProduct(productId)
                    .setUnitAmount(toStripeCents(amount))
                    .setCurrency("sgd")
                    .build();

            Price stripePrice = Price.create(params);
            return stripePrice.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create price in Stripe", e);
        }
    }

    private static long toStripeCents(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    @Override
    @Transactional
    public Product update(UUID id, UpdateProductRequest updateProductRequest, MultipartFile[] newImages) {
        Product product = productRepository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.isDeleted()) {
            throw new IllegalStateException("Product is no longer available");
        }

        BigDecimal newPrice = updateProductRequest.getPrice();
        boolean priceChanged = product.getPrice().compareTo(newPrice) != 0;

        product.setName(updateProductRequest.getName());
        product.setDescription(updateProductRequest.getDescription());
        product.setPrice(newPrice);
        product.setStock(updateProductRequest.getStock());

        if (newImages != null && newImages.length > 0) {
            List<ProductImage> productImages = product.getImages();

            for (MultipartFile newImage : newImages) {
                if (newImage != null && !newImage.isEmpty()) {
                    String imageUrl = cloudinaryImageService.upload(newImage);
                    String altText = newImage.getOriginalFilename() != null
                            ? newImage.getOriginalFilename()
                            : product.getName();
                    ProductImage productImage = new ProductImage(imageUrl, altText);
                    productImage.setProduct(product);
                    productImages.add(productImage);
                }
            }

            product.setImages(productImages);
        }

        if (priceChanged && product.getStripeProductId() != null && !product.getStripeProductId().isBlank()) {
            product.setStripePriceId(createStripePrice(product.getStripeProductId(), newPrice));
        }

        return productRepository.save(product);
    }

    @Override
    public void delete(UUID id) {
        // Find product by ID
        productRepository.findById(Objects.requireNonNull(id)).ifPresent(product -> {
            // set isDeleted to true
            product.setDeleted(true);

            // save the product
            productRepository.save(product);
        });
    }
}
