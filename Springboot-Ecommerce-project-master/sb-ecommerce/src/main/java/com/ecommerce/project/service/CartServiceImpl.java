package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.utils.AuthUtils;
import jakarta.transaction.Transactional;
import lombok.Lombok;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    AuthUtils authUtils;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {

        // Find existing cart or create one
        Cart cart = createCart();

        //Retrieve product details
        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new  ResourceNotFoundException("product", "productId", productId));

        //Perform validations
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId( cart.getCartId(), productId);

        if(cartItem != null) {
            throw new APIException("product" +product.getProductName() + " Already exist in the cart" );
        }

        if(product.getQuantity() ==0) {
            throw new APIException("Product " + product.getProductName() + " not available");
        }

        if(product.getQuantity() < quantity) {
            throw new APIException("Please make an order of the " + product.getProductName() + " less than or equal to quantity " + product.getQuantity() + " .");
        }

        //Create cart item
        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        //Save cart Item
        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity());  // if we want we can subtract quantity , means, when product added into cart , quantity will reduce.
        cart.setTotalPrice(cart.getTotalPrice()+ (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        // return updated cart item
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems= cart.getCartItems();
        Stream<ProductDTO> productStream = cartItems.stream().map( item -> {

            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {

        // find all the carts
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()) {
            throw new APIException("No cart found");
        }

        List<CartDTO> cartDTOList = carts.stream().map(cart -> {

            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
//            List<ProductDTO> products = cart.getCartItems().stream()
//                    .map(p-> modelMapper.map(p, ProductDTO.class)).toList();

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(item -> {
                        ProductDTO dto = modelMapper.map(item.getProduct(), ProductDTO.class);
                        dto.setQuantity(item.getQuantity()); // important
                        return dto;
                    })
                    .toList();
            cartDTO.setProducts(products);
            return cartDTO;
        }).toList();

        return cartDTOList;
    }

    @Override
    public CartDTO getCart(String email, Long cartId) {

        Cart cart = cartRepository.findCartByEmailAndCartId(email, cartId);
        if(cart==null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        cart.getCartItems().forEach(c-> c.getProduct().setQuantity(c.getQuantity()));
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> productDTOList = cart.getCartItems().stream().map(
                p-> modelMapper.map(p.getProduct(), ProductDTO.class)
        ).toList();
        cartDTO.setProducts(productDTOList);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        String email = authUtils.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart", "CartId",cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new  ResourceNotFoundException("product", "productId", productId));

        if(product.getQuantity() ==0) {
            throw new APIException("Product " + product.getProductName() + " not available");
        }

        if(product.getQuantity() < quantity) {
            throw new APIException("Please make an order of the " + product.getProductName() + " less than or equal to quantity " + product.getQuantity() + " .");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if(cartItem == null) {
            throw new APIException("product " + product.getProductName() + " not available in the cart");
        }

        int newQuantity = cartItem.getQuantity()+quantity;

        if(newQuantity < 0) {
            throw new APIException("Quantity should not be in the negative");
        }

        if(newQuantity ==0) {
            deleteProductFromCart(cartId, productId);
        } else {
//            cartItem.setProductPrice(product.getSpecialPrice());
//            cartItem.setQuantity(cartItem.getQuantity()+quantity);
//            cartItem.setDiscount(product.getDiscount());
//            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
//            cartRepository.save(cart);

            // remove old total contribution
            cart.setTotalPrice(
                    cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity())
            );

             // update item
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());

            // add new total contribution
            cart.setTotalPrice(
                    cart.getTotalPrice() + (cartItem.getProductPrice() * cartItem.getQuantity())
            );

            cartRepository.save(cart);
        }
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        if(updatedCartItem.getQuantity() ==0) {
            cartItemRepository.deleteById(updatedCartItem.getCartItemId());
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item-> {

            ProductDTO productDTO = modelMapper.map(item.getProduct(), ProductDTO.class);
            productDTO.setQuantity(item.getQuantity());
            return productDTO;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

//    @Transactional
//    @Override
//    public String deleteProductFromCart(Long cartId, Long productId) {
//
//        Cart cart = cartRepository.findById(cartId)
//                .orElseThrow(()-> new ResourceNotFoundException("Cart", "cartId", cartId));
//
//        CartItem cartItem= cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
//        if(cartItem == null) {
//            throw new ResourceNotFoundException("Product", "productId", productId);
//        }
//
//        cart.setTotalPrice((cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity())));
//        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
//        return "Product " + cartItem.getProduct().getProductName() + " has deleted successfully";
//    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        // ❌ remove old wrong subtraction logic

        // ✅ delete item first
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        // ✅ recalculate total properly
        double total = cart.getCartItems().stream()
                .filter(item -> !item.getProduct().getProductId().equals(productId))
                .mapToDouble(item -> item.getProductPrice() * item.getQuantity())
                .sum();

        cart.setTotalPrice(total);
        cartRepository.save(cart);

        return "Product " + cartItem.getProduct().getProductName() + " has deleted successfully";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart", "cartId",cartId));

        Product product= productRepository.findById(productId)
                .orElseThrow(()-> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem= cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if(cartItem== null) {
            throw new APIException("Product "+ product.getProductName() + " not available in the cart");
        }

        double cartPrice = (cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice +  (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.save(cartItem);
    }



    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtils.loggedInEmail());
        if(userCart != null) {
            return userCart;
        }

        Cart cart= new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtils.loggedInUser());
        Cart newCart = cartRepository.save(cart);
        return newCart;
    }
}
