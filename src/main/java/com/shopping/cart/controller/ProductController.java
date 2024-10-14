package com.shopping.cart.controller;

import com.shopping.cart.dto.request.AddProductRequest;
import com.shopping.cart.dto.request.UpdateProductRequest;
import com.shopping.cart.entity.Product;
import com.shopping.cart.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/index")
    public String getAllProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product/index";
    }


    @GetMapping("/show/{id}")
    public Product getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping("/create")
    public String createProduct() {
        return "product/create";
    }

    @PostMapping(value = "/store", consumes = { "multipart/form-data" })
    public String addProduct(@ModelAttribute AddProductRequest addProductRequest,
                               @RequestParam("images") MultipartFile[] images) {
        productService.store(addProductRequest, images);
        return "redirect:/dashboard";
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable UUID id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/edit";
    }

    @PostMapping(value = "/update/{id}", consumes = { "multipart/form-data" })
    public String updateProduct(@PathVariable UUID id, @ModelAttribute UpdateProductRequest updateProductRequest,
                                @RequestParam("images") MultipartFile[] newImages) {
        productService.update(id, updateProductRequest, newImages);
        return "redirect:/dashboard";
    }


    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
        return "redirect:/dashboard";
    }
}
