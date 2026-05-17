package com.demo.ecommerce.controller;

import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ecommerce.models.Cart;
import com.demo.ecommerce.models.CartProduct;
import com.demo.ecommerce.models.Product;
import com.demo.ecommerce.models.UserInfo;
import com.demo.ecommerce.repository.CartProductRepo;
import com.demo.ecommerce.repository.CartRepo;
import com.demo.ecommerce.repository.ProductRepo;
import com.demo.ecommerce.repository.UserInfoRepository;

// Import required Annotations and implement the business logic
@RestController
@RequestMapping("/api/auth/consumer")
public class ConsumerController {

	@Autowired
	private ProductRepo productRepo;

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private CartProductRepo cpRepo;

	@Autowired
	private UserInfoRepository userRepo;

	@GetMapping("/cart")
	public ResponseEntity<Object> getCart(Principal principal) {
		UserInfo user = getCurrentUser(principal);
		if (user == null) {
			return ResponseEntity.badRequest().body("User not found");
		}

		Cart cart = getOrCreateCart(user);
		return ResponseEntity.ok(cart);
	}

	@PostMapping("/cart")
	public ResponseEntity<Object> postCart(Principal principal, @RequestBody Product product) {
		UserInfo user = getCurrentUser(principal);
		if (user == null) {
			return ResponseEntity.badRequest().body("User not found");
		}

		// if (product == null || product.getProductId() == 0) {
		// 	return ResponseEntity.badRequest().body("Product information is required");
		// }

		Optional<Product> optionalProduct = productRepo.findById(product.getProductId());
		if (optionalProduct.isEmpty()) {
			return ResponseEntity.badRequest().body("Product not found");
		}

		Cart cart = getOrCreateCart(user);
		Optional<CartProduct> existingCartProduct = cpRepo.findByCartUserUserIdAndProductProductId(user.getUserId(),
				product.getProductId());
		if (existingCartProduct.isPresent()) {
			return ResponseEntity.status(409).body("Product already exists in cart");
		}

		CartProduct newCartProduct = new CartProduct(cart, optionalProduct.get(), 1);
		cpRepo.save(newCartProduct);
		return ResponseEntity.ok(getOrCreateCart(user));
	}

	@PutMapping("/cart")
	public ResponseEntity<Object> putCart(Principal principal, @RequestBody CartProduct cp) {
		UserInfo user = getCurrentUser(principal);
		if (user == null) {
			return ResponseEntity.badRequest().body("User not found");
		}

		// if (cp == null || cp.getProduct() == null || cp.getProduct().getProductId() == 0
		// 		|| cp.getQuantity() == null) {
		// 	return ResponseEntity.badRequest().body("Product and quantity are required");
		// }

		int productId = cp.getProduct().getProductId();
		Optional<CartProduct> optionalCartProduct = cpRepo.findByCartUserUserIdAndProductProductId(user.getUserId(),
				productId);
		if (optionalCartProduct.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		CartProduct existingCartProduct = optionalCartProduct.get();
		if (cp.getQuantity() <= 0) {
			cpRepo.delete(existingCartProduct);
			return ResponseEntity.ok(getOrCreateCart(user));
		}

		existingCartProduct.setQuantity(cp.getQuantity());
		cpRepo.save(existingCartProduct);
		return ResponseEntity.ok(getOrCreateCart(user));
	}

	@DeleteMapping("/cart")
	public ResponseEntity<Object> deleteCart(Principal principal, @RequestBody Product product) {
		UserInfo user = getCurrentUser(principal);
		if (user == null) {
			return ResponseEntity.badRequest().body("User not found");
		}

		if (product == null || product.getProductId() == 0) {
			return ResponseEntity.badRequest().body("Product information is required");
		}

		Optional<CartProduct> optionalCartProduct = cpRepo.findByCartUserUserIdAndProductProductId(user.getUserId(),
				product.getProductId());
		if (optionalCartProduct.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		cpRepo.delete(optionalCartProduct.get());
		return ResponseEntity.ok(getOrCreateCart(user));
	}

	private UserInfo getCurrentUser(Principal principal) {
		if (principal == null || principal.getName() == null) {
			return null;
		}
		return userRepo.findByUsername(principal.getName()).orElse(null);
	}

	private Cart getOrCreateCart(UserInfo user) {
		Optional<Cart> optionalCart = cartRepo.findByUserUsername(user.getUsername());
		if (optionalCart.isPresent()) {
			return optionalCart.get();
		}

		Cart cart = new Cart(0.0, user);
		return cartRepo.save(cart);
	}
}
