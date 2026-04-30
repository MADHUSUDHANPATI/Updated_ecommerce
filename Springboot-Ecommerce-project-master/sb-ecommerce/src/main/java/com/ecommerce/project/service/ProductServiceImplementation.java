    package com.ecommerce.project.service;


    import com.ecommerce.project.exceptions.APIException;
    import com.ecommerce.project.exceptions.ResourceNotFoundException;
    import com.ecommerce.project.model.Cart;
    import com.ecommerce.project.model.Category;
    import com.ecommerce.project.model.Product;
    import com.ecommerce.project.payload.CartDTO;
    import com.ecommerce.project.payload.ProductDTO;
    import com.ecommerce.project.payload.ProductResponse;
    import com.ecommerce.project.repository.CartRepository;
    import com.ecommerce.project.repository.CategoryRepository;
    import com.ecommerce.project.repository.ProductRepository;
    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.modelmapper.ModelMapper;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.domain.Sort;
    import org.springframework.stereotype.Service;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.util.List;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Service
    public class ProductServiceImplementation implements ProductService {

        @Value("${project.image}")
        String path;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private ModelMapper modelMapper;
        
        @Autowired
        private FileService fileService;

        @Autowired
        CartRepository cartRepository;

        @Autowired
        CartService cartService;

        @Override
        public ProductDTO addProducts(Long categoryId, ProductDTO productDTO) {

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(()-> new ResourceNotFoundException("Category", "categoryId",categoryId));


            //check if the product already present or not;

            boolean isProductPresent = true;
            List<Product> products= category.getProducts();
            for(Product value : products) {
                if(value.getProductName().equals(productDTO.getProductName())) {
                    isProductPresent = false;
                    break;
                }
            }

            if( isProductPresent) {

                Product product = modelMapper.map(productDTO, Product.class);

                product.setCategory(category);
                product.setImage("image.png");
                double specialPrice = product.getPrice() - (((product.getDiscount() / 100)) * product.getPrice());

                product.setSpecialPrice(specialPrice);

                Product savedProduct = productRepository.save(product);
                return modelMapper.map(savedProduct, ProductDTO.class);
            } else  {
                throw new APIException("Product already with " + productDTO.getProductName());
            }
        }

        @Override
        public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

            Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

            Page<Product> pageDetails = productRepository.findAll(pageable);
            List<Product> products =pageDetails.getContent();

            //Check the data null is not
            if(products.isEmpty()) {
                throw new APIException("No Data found to fetch");
            }
            List<ProductDTO> productDTOList=products.stream()
                    .map(product -> modelMapper.map(product, ProductDTO.class)).toList();

            ProductResponse productResponse= new ProductResponse();
            productResponse.setContent(productDTOList);
            productResponse.setPageNumber(pageDetails.getNumber());
            productResponse.setPageSize(pageDetails.getSize());
            productResponse.setTotalElements(pageDetails.getTotalElements());
            productResponse.setTotalPages(pageDetails.getTotalPages());
            productResponse.setLastPage(pageDetails.isLast());
            return productResponse;


        }

        public ProductResponse getAllProductsByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(()-> new ResourceNotFoundException("Category ", "categoryId", categoryId));

            Sort sortByAndOrder = sortOrder.equalsIgnoreCase("ase") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable =PageRequest.of(pageNumber, pageSize, sortByAndOrder);

            Page<Product> pageDetails = productRepository.findByCategoryOrderByPriceAsc(category, pageable);

            List<Product> products= pageDetails.getContent();

            if(products.isEmpty()) {
                throw new APIException("No Data found to fetch");
            }
            List<ProductDTO> productDTOList = products.stream()
                    .map(product-> modelMapper.map(product, ProductDTO.class)).toList();

            ProductResponse productResponse = new ProductResponse();
            productResponse.setContent(productDTOList);
            productResponse.setPageNumber(pageDetails.getNumber());
            productResponse.setPageSize(pageDetails.getSize());
            productResponse.setTotalElements(pageDetails.getTotalElements());
            productResponse.setTotalPages(pageDetails.getTotalPages());
            productResponse.setLastPage(pageDetails.isLast());
            return productResponse;
        }

        @Override
        public ProductResponse getAllProductsByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

            Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

            Page<Product> pageDetails = productRepository.findByProductNameLikeIgnoreCase('%' + keyword+'%',pageable);
            List<Product> products =pageDetails.getContent();

            if(products.isEmpty()) {
                throw new APIException("No Data found to fetch");
            }

            List<ProductDTO> productDTOList = products.stream()
                    .map(product-> modelMapper.map(product, ProductDTO.class)).toList();

            ProductResponse productResponse = new ProductResponse();
            productResponse.setContent(productDTOList);
            productResponse.setPageNumber(pageDetails.getNumber());
            productResponse.setPageSize(pageDetails.getSize());
            productResponse.setTotalElements(pageDetails.getTotalElements());
            productResponse.setTotalPages(pageDetails.getTotalPages());
            productResponse.setLastPage(pageDetails.isLast());
            return productResponse;
        }

        @Override
        public ProductDTO updateProduct( Long productId, ProductDTO productDTO) {

            Product needProduct= modelMapper.map(productDTO, Product.class);
            Product product = productRepository.findById(productId)
                    .orElseThrow(()->new  ResourceNotFoundException("Product", "ProductId", productId));

            product.setProductName(needProduct.getProductName());
            product.setProductDescription(needProduct.getProductDescription());
            product.setQuantity(needProduct.getQuantity());
            product.setPrice(needProduct.getPrice());
            product.setDiscount(needProduct.getDiscount());
            double specialPrice = product.getPrice() - ((product.getDiscount()/100) *product.getPrice());
            product.setSpecialPrice(specialPrice);

              //Same as above

//            Product product = productRepository.findById(productId)
//                    .orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
//
//            // Map DTO onto the existing product entity
//            modelMapper.map(productDTO, product);

            List<Cart> carts = cartRepository.findCartsByProductId(productId);

            List<CartDTO> cartDTOS = carts.stream().map(cart-> {

                CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                List<ProductDTO> products = cart.getCartItems().stream()
                        .map(p-> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();

                cartDTO.setProducts(products);
                return cartDTO;

            }).toList();

            cartDTOS.forEach(cart-> cartService.updateProductInCarts(cart.getCartId(), productId));

            Product savedProduct = productRepository.save(product);

            return modelMapper.map(savedProduct, ProductDTO.class);
        }

        @Override
        public ProductDTO deleteProduct(Long productId) {

            Product deletingProduct = productRepository.findById(productId)
                    .orElseThrow(()-> new ResourceNotFoundException("Product", "productId",productId));

            List<Cart> carts = cartRepository.findCartsByProductId(productId);
            carts.forEach(cart-> cartService.deleteProductFromCart(cart.getCartId(),productId));

            productRepository.delete(deletingProduct);

            return modelMapper.map(deletingProduct, ProductDTO.class);
        }

        @Override
        public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {

            // Get the product with product ID
            Product product = productRepository.findById(productId)
                    .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

            //Upload the image to the server
            //Get the file Name
            String fileName = fileService.uploadImage(path, image);

            //save the updated product
            product.setImage(fileName);
            Product savedProduct = productRepository.save(product);

            // return DTO after model mapper converts
            return modelMapper.map(savedProduct, ProductDTO.class);
        }
        
    }
