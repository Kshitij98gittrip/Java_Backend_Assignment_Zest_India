package com.zest.product.service;

import com.zest.product.dto.CustomPageResponse;
import com.zest.product.dto.ItemResponseDTO;
import com.zest.product.dto.ProductRequestDTO;
import com.zest.product.dto.ProductResponseDTO;
import com.zest.product.entity.Item;
import com.zest.product.entity.Product;
import com.zest.product.exception.ResourceNotFoundException;
import com.zest.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ModelMapper modelMapper;

    @Override
    public CustomPageResponse<ProductResponseDTO> getAll(Pageable pageable) {
        Page<Product> productPage = repository.findAll(pageable);
        Page<ProductResponseDTO> dtoPage = productPage.map(product ->
                modelMapper.map(product, ProductResponseDTO.class));
        return new CustomPageResponse<>(dtoPage);
    }

    @Override
    public ProductResponseDTO getById(Integer id) { // Changed to Integer
        Product product = repository.findById(Long.valueOf(id))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));
        return mapToDTO(product);
    }

    // ✅ CREATE
    @Override
    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product product = modelMapper.map(dto, Product.class);
        product.setCreatedBy("system");
        if (dto.getItems() != null) {
            List<Item> items = dto.getItems().stream()
                    .map(itemDto -> {
                        Item item = modelMapper.map(itemDto, Item.class);
                        item.setProduct(product);
                        return item;
                    })
                    .toList();
            product.setItems(items);
        }
        return mapToDTO(repository.save(product));
    }

    @Override
    public ProductResponseDTO update(Integer id, ProductRequestDTO dto) {
        Product product = repository.findById(Long.valueOf(id))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + id));
        modelMapper.map(dto, product);
        product.setModifiedBy("system");
        return mapToDTO(repository.save(product));
    }

    @Override
    public void delete(Integer id) {
        if (!repository.existsById(Long.valueOf(id))) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        repository.deleteById(Long.valueOf(id));
    }

    @Override
    public List<ItemResponseDTO> getItemsByProductId(Integer productId) {
        Product product = repository.findById(Long.valueOf(productId))
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found with id: " + productId));
        return product.getItems()
                .stream()
                .map(item -> modelMapper.map(item, ItemResponseDTO.class))
                .toList();
    }

    private ProductResponseDTO mapToDTO(Product p) {
        return modelMapper.map(p, ProductResponseDTO.class);
    }
}