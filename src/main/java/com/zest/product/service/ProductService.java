package com.zest.product.service;

import com.zest.product.dto.CustomPageResponse;
import com.zest.product.dto.ItemResponseDTO;
import com.zest.product.dto.ProductRequestDTO;
import com.zest.product.dto.ProductResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    CustomPageResponse<ProductResponseDTO> getAll(Pageable pageable);

    ProductResponseDTO getById(Integer id);

    ProductResponseDTO create(ProductRequestDTO dto);

    ProductResponseDTO update(Integer id, ProductRequestDTO dto);

    void delete(Integer id);

    List<ItemResponseDTO> getItemsByProductId(Integer productId);
}