package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired  // This is field injection
    private CategoryService categoryService;

    // or , we can make use of constructor here for bean creation.

////    @GetMapping("/echo")
////    public ResponseEntity<String> echoMessage (@RequestParam(name ="message", required = false, defaultValue = "Namastey") String message) { // Request param reads the query paramters from the url.
////        return new  ResponseEntity<>("Echo message : " +message, HttpStatus.OK);
//    }
    @GetMapping("/public/categories")
    //@RequestMapping(value = "/api/public/categories", method = RequestMethod.GET)  -----> Both Lines are same.
    public ResponseEntity<CategoryResponse> getCategories(@RequestParam (name = "pageNumber",defaultValue =AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                          @RequestParam(name = "pageSize",defaultValue =AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                          @RequestParam(name ="sortBy", defaultValue = AppConstants.SORT_BY_CATEGORY_ID,required = false) String sortBy,
                                                          @RequestParam(name ="sortOrder", defaultValue = AppConstants.SORT_ORDER_TYPE,required = false) String sortOrder) {
        CategoryResponse categoryResponse = categoryService.getCategories(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> postCategories( @Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO categoryDTO1= categoryService.postCategories(categoryDTO);
        return new ResponseEntity<>( categoryDTO1, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {

            CategoryDTO categoryDTO = categoryService.deleteCategory(categoryId);
            return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
//         catch ( ResponseStatusException e) {
//
//            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
//            // other way
//            // return ResponseEntity.ok(status); same as above
//            // return ResponseEntity.status(HttpStatus.OK).body(Status); same;
//        }

    }

    @PutMapping("/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> updateCategory(@Valid @PathVariable Long categoryId,  @RequestBody CategoryDTO categoryDTO) {

            CategoryDTO savedCategoryDTO = categoryService.updateCategory(categoryId, categoryDTO);
            return new ResponseEntity<>(savedCategoryDTO, HttpStatus.OK);

//        catch ( ResponseStatusException e) {
//            return new ResponseEntity<>(e.getReason(), e.getStatusCode());
//        }
    }
}
