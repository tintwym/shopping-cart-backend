package com.shopping.cart.service;

import com.shopping.cart.dto.request.AddProductRequest;
import com.shopping.cart.dto.request.UpdateProductRequest;
import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.ProductImage;
import com.shopping.cart.interfaces.IProductService;
import com.shopping.cart.mapper.ProductMapper;
import com.shopping.cart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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

        // Handle image saving
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                try {
                    // Save the file to the products folder
                    Path uploadPath = Paths.get(uploadDir + fileName);
                    Files.createDirectories(uploadPath.getParent()); // Ensure directory exists
                    Files.copy(image.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

                    // Save image details in the database
                    ProductImage productImage = new ProductImage(fileName, "Alt text for " + fileName);
                    productImage.setProduct(product);
                    productImages.add(productImage);

                } catch (IOException e) {
                    throw new RuntimeException("Failed to save image: " + fileName, e);
                }
            }
        }

        product.setImages(productImages);
        return productRepository.save(product);
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
                        // Save the file to the products folder
                        Path uploadPath = Paths.get(uploadDir + fileName);
                        Files.createDirectories(uploadPath.getParent()); // Ensure directory exists
                        Files.copy(newImage.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

                        // Save image details in the database
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
