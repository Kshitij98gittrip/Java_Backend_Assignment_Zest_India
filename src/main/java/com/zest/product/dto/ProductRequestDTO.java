package com.zest.product.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductRequestDTO {

    @NotNull
    private String productName;
    private List<ItemRequestDTO> items;
}
