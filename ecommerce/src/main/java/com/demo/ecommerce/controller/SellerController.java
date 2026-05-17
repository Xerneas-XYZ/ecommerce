package com.demo.ecommerce.controller;


import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ecommerce.models.Category;
import com.demo.ecommerce.models.Product;
import com.demo.ecommerce.models.UserInfo;
import com.demo.ecommerce.repository.CategoryRepo;
import com.demo.ecommerce.repository.ProductRepo;
import com.demo.ecommerce.repository.UserInfoRepository;

// Import required Annotations and implement the  business logics
@RestController
@RequestMapping("/api/auth/seller")
public class SellerController {
@Autowired
	ProductRepo productRepo;
@Autowired
	UserInfoRepository userRepo;
@Autowired
	CategoryRepo categoryRepo;

	@PostMapping("/product")
	public ResponseEntity<Object> postProduct(Principal principal, @RequestBody Product product) {
		UserInfo seller = userRepo.findByUsername(principal.getName()).orElse(null);
		if (seller == null) {
			return ResponseEntity.badRequest().body("Seller not found");
		}

		if (product == null || product.getCategory() == null || product.getCategory().getCategoryName() == null) {
			return ResponseEntity.badRequest().body("Category information is required");
		}

		Category category = categoryRepo.findByCategoryName(product.getCategory().getCategoryName()).orElse(null);
		if (category == null) {
			return ResponseEntity.badRequest().body("No Category Found");
		}

		Product dbProduct = new Product(product.getProductName(), product.getPrice(), seller, category);
		Product savedProduct = productRepo.saveAndFlush(dbProduct);

		URI location = URI.create("/api/auth/seller/product/" + savedProduct.getProductId());
		return ResponseEntity.created(location).body(savedProduct);
	}

	@GetMapping("/product")
	public ResponseEntity<Object> getAllProducts(Principal principal) {
		UserInfo seller = userRepo.findByUsername(principal.getName()).orElse(null);
		if(seller != null){
			List<Product> prodList = productRepo.findBySellerUserId(seller.getUserId());
			if(prodList != null )
			return ResponseEntity.ok(prodList);
		}
		return ResponseEntity.badRequest().build();
	}

	@GetMapping("/product/{productId}")
	public ResponseEntity<Object> getProduct(Principal principal,@PathVariable Integer productId) {
		UserInfo seller = userRepo.findByUsername(principal.getName()).orElse(null);
		if (seller == null) {
			return ResponseEntity.badRequest().body("Seller not found");
		}

		Optional<Product> optionalProduct = productRepo.findBySellerUserIdAndProductId(seller.getUserId(), productId);
		if (optionalProduct.isEmpty()) {
			return ResponseEntity.status(404).body("Product not found");
		}

		return ResponseEntity.ok(optionalProduct.get());
	}

	@PutMapping("/product")
	public ResponseEntity<Object> putProduct(Principal principal, @RequestBody Product updatedProduct) {
		UserInfo seller = userRepo.findByUsername(principal.getName()).orElse(null);
		if (seller == null) {
			return ResponseEntity.badRequest().body("Seller not found");
		}

		if (updatedProduct == null || updatedProduct.getProductId() == 0) {
			return ResponseEntity.badRequest().body("Product id is required");
		}

		Optional<Product> optionalProduct = productRepo.findBySellerUserIdAndProductId(seller.getUserId(),
				updatedProduct.getProductId());
		if (optionalProduct.isEmpty()) {
			return ResponseEntity.status(404).body("Product not found");
		}

		Product existingProduct = optionalProduct.get();
		existingProduct.setProductName(updatedProduct.getProductName());
		existingProduct.setPrice(updatedProduct.getPrice());
		if (updatedProduct.getCategory() != null && updatedProduct.getCategory().getCategoryName() != null) {
			Category category = categoryRepo.findByCategoryName(updatedProduct.getCategory().getCategoryName())
					.orElse(null);
			if (category == null) {
				return ResponseEntity.badRequest().body("No Category Found");
			}
			existingProduct.setCategory(category);
		}

		productRepo.save(existingProduct);
		return ResponseEntity.ok(existingProduct);
	}

	@DeleteMapping("/product/{productId}")
	public ResponseEntity<Object> deleteProduct(Principal principal, @PathVariable Integer productId) {
		UserInfo seller = userRepo.findByUsername(principal.getName()).orElse(null);
		if (seller == null) {
			return ResponseEntity.badRequest().body("Seller not found");
		}

		Optional<Product> optionalProduct = productRepo.findBySellerUserIdAndProductId(seller.getUserId(), productId);
		if (optionalProduct.isEmpty()) {
			return ResponseEntity.status(404).body("Product not found");
		}

		productRepo.delete(optionalProduct.get());
		return ResponseEntity.ok().build();
	}

}