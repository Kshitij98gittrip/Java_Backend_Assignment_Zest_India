package com.zest.product.service;

import com.zest.product.dto.*;
import com.zest.product.entity.Product;
import com.zest.product.exception.ResourceNotFoundException;
import com.zest.product.repository.ProductRepository;
import com.zest.product.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductResponseDTO responseDTO;
    private final Integer id = 1;
    private final Long longId = 1L;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1);
        product.setProductName("Laptop");

        responseDTO = new ProductResponseDTO();
        responseDTO.setId(1);
        responseDTO.setProductName("Laptop");
    }

    @Test
    void getAll_ShouldReturnCustomPageResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = List.of(product);
        Page<Product> productPage = new PageImpl<>(productList, pageable, 1);
        when(repository.findAll(pageable)).thenReturn(productPage);
        when(modelMapper.map(any(Product.class), eq(ProductResponseDTO.class))).thenReturn(responseDTO);
        CustomPageResponse<ProductResponseDTO> result = productService.getAll(pageable);
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findAll(pageable);
    }

    @Test
    void getById_ShouldReturnProduct_WhenIdExists() {
        when(repository.findById(longId)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductResponseDTO.class)).thenReturn(responseDTO);
        ProductResponseDTO result = productService.getById(id);
        assertNotNull(result);
        assertEquals("Laptop", result.getProductName());
    }

    @Test
    void getById_ShouldThrowException_WhenIdDoesNotExist() {
        when(repository.findById(longId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getById(id));
    }

    @Test
    void create_ShouldReturnSavedProductResponse() {
        ProductRequestDTO requestDTO = new ProductRequestDTO();
        requestDTO.setProductName("New Product");
        when(modelMapper.map(requestDTO, Product.class)).thenReturn(product);
        when(repository.save(any(Product.class))).thenReturn(product);
        when(modelMapper.map(product, ProductResponseDTO.class)).thenReturn(responseDTO);
        ProductResponseDTO result = productService.create(requestDTO);
        assertNotNull(result);
        verify(repository).save(any(Product.class));
        verify(modelMapper).map(product, ProductResponseDTO.class);
    }

    @Test
    void update_ShouldReturnUpdatedProductResponse() {
        ProductRequestDTO updateDTO = new ProductRequestDTO();
        updateDTO.setProductName("Updated Name");
        when(repository.findById(longId)).thenReturn(Optional.of(product));
        doNothing().when(modelMapper).map(updateDTO, product);
        when(repository.save(product)).thenReturn(product);
        when(modelMapper.map(product, ProductResponseDTO.class)).thenReturn(responseDTO);
        ProductResponseDTO result = productService.update(id, updateDTO);
        assertNotNull(result);
        verify(modelMapper).map(updateDTO, product);
        verify(repository).save(product);
    }

    @Test
    void delete_ShouldCallRepository_WhenIdExists() {
        when(repository.existsById(longId)).thenReturn(true);
        productService.delete(id);
        verify(repository).deleteById(longId);
    }

    @Test
    void getItemsByProductId_ShouldReturnItemList() {
        product.setItems(new ArrayList<>());
        when(repository.findById(longId)).thenReturn(Optional.of(product));
        List<ItemResponseDTO> result = productService.getItemsByProductId(id);
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repository).findById(longId);
    }
}