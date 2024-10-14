package com.shopping.cart.controller.api;

import com.shopping.cart.dto.request.AddProductRequest;
import com.shopping.cart.dto.request.UpdateProductRequest;
import com.shopping.cart.entity.Product;
import com.shopping.cart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductApiController {
    private final ProductService productService;

    @Autowired
    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/index")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/show/{id}")
    public Product getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

//    @PostMapping("/store")
//    public Product addProduct(@RequestBody AddProductRequest addProductRequest) {
//        return productService.store(addProductRequest);
//    }

//    @PutMapping("/update/{id}")
//    public Product updateProduct(@PathVariable UUID id, @RequestBody UpdateProductRequest updateProductRequest) {
//        return productService.update(id, updateProductRequest);
//    }

    @DeleteMapping("/delete/{id}")
    public void deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
    }
}
