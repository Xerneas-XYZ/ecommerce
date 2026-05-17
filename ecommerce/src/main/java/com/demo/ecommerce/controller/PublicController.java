package com.demo.ecommerce.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ecommerce.models.Product;
import com.demo.ecommerce.repository.ProductRepo;

// Import required Annotations and implement the  business logics
@RestController
@RequestMapping("/api/public")
public class PublicController {
@Autowired
	ProductRepo productRepo;
// @Autowired
// 	UserInfoRepository userRepo;
// @Autowired
// 	private AuthenticationManager authenticationManager;

	@GetMapping("/product/search")
	public List<Product> getProducts(@RequestParam String keyword) {
		return productRepo.findByProductNameContainingIgnoreCaseOrCategoryCategoryNameContainingIgnoreCase(keyword, keyword);
	}
}
