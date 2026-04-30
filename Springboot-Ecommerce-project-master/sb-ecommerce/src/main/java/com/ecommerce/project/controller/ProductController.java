package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping("/admin/categories/{categoryId}/products")
    public ResponseEntity<ProductDTO> addProducts(@Valid @PathVariable Long categoryId, @RequestBody ProductDTO productDTO) {

        ProductDTO savedproductDTO= productService.addProducts(categoryId, productDTO);
        return new ResponseEntity<>(savedproductDTO, HttpStatus.CREATED);
    }

    @GetMapping("/public/products")
    public ResponseEntity<ProductResponse> getAllProducts( @RequestParam(name = "pageNumber", required = false, defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                           @RequestParam(name = "pageSize", required = false, defaultValue =AppConstants.PAGE_SIZE ) Integer pageSize,
                                                           @RequestParam(name = "sortBy", required = false, defaultValue =AppConstants.SORT_BY_PRODUCT_ID ) String sortBy,
                                                           @RequestParam(name = "sortOrder", required = false, defaultValue = AppConstants.SORT_ORDER_TYPE) String sortOrder
    ) {

        ProductResponse productResponse = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getAllProductsByCategory(@PathVariable Long categoryId,
                                                                    @RequestParam(name = "pageNumber", required = false, defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                    @RequestParam(name = "pageSize", required = false, defaultValue =AppConstants.PAGE_SIZE ) Integer pageSize,
                                                                    @RequestParam(name = "sortBy", required = false, defaultValue =AppConstants.SORT_BY_PRODUCT_ID ) String sortBy,
                                                                    @RequestParam(name = "sortOrder", required = false, defaultValue = AppConstants.SORT_ORDER_TYPE) String sortOrder) {

        ProductResponse productResponse = productService.getAllProductsByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("/public/products/keywords/{keyword}")
    public ResponseEntity<ProductResponse> getAllProductsByKeyword(@PathVariable String keyword,
                                                                   @RequestParam(name = "pageNumber", required = false, defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                   @RequestParam(name = "pageSize", required = false, defaultValue =AppConstants.PAGE_SIZE ) Integer pageSize,
                                                                   @RequestParam(name = "sortBy", required = false, defaultValue =AppConstants.SORT_BY_PRODUCT_ID ) String sortBy,
                                                                   @RequestParam(name = "sortOrder", required = false, defaultValue = AppConstants.SORT_ORDER_TYPE) String sortOrder) {

        ProductResponse productResponse = productService.getAllProductsByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @PathVariable Long productId, @RequestBody ProductDTO productDTO) {

        ProductDTO updateProductDTO = productService.updateProduct(productId, productDTO);

        return new ResponseEntity<>(updateProductDTO, HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId) {

        ProductDTO productDTO = productService.deleteProduct(productId);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> updateProductImage(@PathVariable Long productId, @RequestParam("image") MultipartFile image) throws IOException {

        ProductDTO updatedProductImageDTO = productService.updateProductImage(productId, image);

        return new ResponseEntity<>(updatedProductImageDTO, HttpStatus.OK);
    }

}
