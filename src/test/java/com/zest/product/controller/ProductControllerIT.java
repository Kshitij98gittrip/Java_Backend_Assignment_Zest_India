package com.zest.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zest.product.dto.ProductRequestDTO;
import com.zest.product.entity.Product;
import com.zest.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequestDTO request = new ProductRequestDTO();
        request.setProductName("Gaming Laptop");
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.productName").value("Gaming Laptop"));
    }

    @Test
    void shouldGetAllProductsPaginated() throws Exception {
        createAndSaveProduct("Phone");
        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldGetProductById() throws Exception {
        Product saved = createAndSaveProduct("Tablet");
        mockMvc.perform(get("/api/v1/products/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.productName").value("Tablet"));
    }

    @Test
    void shouldUpdateProduct() throws Exception {
        Product saved = createAndSaveProduct("Old Name");
        ProductRequestDTO updateRequest = new ProductRequestDTO();
        updateRequest.setProductName("New Name");
        mockMvc.perform(put("/api/v1/products/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("New Name"));
    }

    @Test
    void shouldDeleteProduct() throws Exception {
        Product saved = createAndSaveProduct("To Be Deleted");
        mockMvc.perform(delete("/api/v1/products/" + saved.getId()))
                .andExpect(status().isNoContent());
        assertFalse(repository.existsById(Long.valueOf(saved.getId())));
    }

    @Test
    void shouldGetItemsByProductId() throws Exception {
        Product saved = createAndSaveProduct("Product with Items");
        mockMvc.perform(get("/api/v1/products/" + saved.getId() + "/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReturn404WhenProductNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound());
    }

    private Product createAndSaveProduct(String name) {
        Product p = new Product();
        p.setProductName(name);
        p.setCreatedBy("test-user");
        p.setModifiedBy("test-user");
        p.setCreatedOn(LocalDateTime.now());
        p.setModifiedOn(LocalDateTime.now());
        return repository.save(p);
    }
}