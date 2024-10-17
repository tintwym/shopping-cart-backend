package com.shopping.cart.service;

import com.shopping.cart.entity.Cart;
import com.shopping.cart.entity.CartItem;
import com.shopping.cart.entity.Product;
import com.shopping.cart.entity.User;
import com.shopping.cart.interfaces.ICartService;
import com.shopping.cart.repository.CartItemRepository;
import com.shopping.cart.repository.CartRepository;
import com.shopping.cart.repository.ProductRepository;
import com.shopping.cart.repository.UserRepository;
import com.shopping.cart.utility.JwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService implements ICartService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final UserService userService;

    @Autowired
    public CartService(ProductRepository productRepository, CartRepository cartRepository, CartItemRepository cartItemRepository, UserRepository userRepository, JwtUtility jwtUtility, UserService userService) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
        this.userService = userService;
    }

    @Override
    public int getCartItemCount(String token) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        // Retrieve the cart by user
        Cart cart = cartRepository.findByUser(user);

        if (cart == null || cart.getCartItems().isEmpty()) {
            return 0; // Return 0 if no cart or cart is empty
        }

        // Sum the quantity of all items in the cart
        return cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity) // Get quantity of each item
                .sum(); // Sum all quantities
    }

    @Override
    public Cart getCartByUser(String token) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        return cartRepository.findByUser(user);
    }

    @Override
    public void addProductToCart(String token, UUID productId, int quantity) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        // Retrieve the cart by user. If no cart exists, create a new one and save it.
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            cart = new Cart(BigDecimal.ZERO, user);
            cart = cartRepository.save(cart); // Save the new cart before adding items
        }

        // Retrieve the product by UUID. If the product is not found, throw an exception.
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if the product already exists in the cart
        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            // If the product exists in the cart, update the quantity
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity); // Update the quantity

            // Update the item's total price based on new quantity
            cartItem.setPrice(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        } else {
            // Create a new cart item if the product doesn't already exist in the cart
            CartItem cartItem = new CartItem(cart, product, quantity, product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            cart.getCartItems().add(cartItem);
        }

        // Recalculate the cart's total price by summing the total price of all items
        BigDecimal updatedTotalPrice = cart.getCartItems().stream()
                .map(CartItem::getPrice) // Get total price of each item
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum the total prices

        cart.setTotalPrice(updatedTotalPrice); // Set the new total price for the cart

        // Save the updated cart and its items
        cartRepository.save(cart);
    }


    @Override
    public void updateProductInCart(String token, UUID productId, int quantity) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        // Retrieve the cart by user. If no cart exists, return.
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            return;
        }

        // Retrieve the product by UUID. If product is not found, return.
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            return;
        }

        // Find the cart item in the cart
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (cartItem == null) {
            return;
        }

        // Update logic based on the provided quantity
        if (quantity == 1) {
            // If the quantity is 1, increase the quantity by 1
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else if (quantity > 1) {
            // If the quantity is greater than 1, set the new quantity
            cartItem.setQuantity(quantity);
        }

        // Update the price of the cart item
        cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));

        // Update the total price of the cart
        cart.setTotalPrice(cart.getCartItems().stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

        // Save the cart
        cartRepository.save(cart);
    }

    @Override
    public void deleteProductFromCart(String token, UUID productId) {
        // Get the user from the token
        User user = userService.getUserFromToken(token);

        // Retrieve the cart by user. If no cart exists, return.
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            return;
        }

        // Retrieve the product by UUID. If product is not found, return.
        Product product = productRepository.findById(productId).orElse(null);

        if (product == null) {
            return;
        }

        // Find the cart item in the cart
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (cartItem == null) {
            return;
        }

        // Update the total price of the cart
        cart.setTotalPrice(cart.getTotalPrice().subtract(cartItem.getPrice()));

        // Remove the cart item from the cart
        cart.getCartItems().remove(cartItem);

        // Save the cart
        cartRepository.save(cart);
    }

    private boolean existProductInCart(UUID productId, User user) {
        Cart cart = cartRepository.findByUser(user);
        if (cart == null) {
            return false;
        }

        return cart.getCartItems().stream()
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }
}
