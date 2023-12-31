package com.letsanjoy.xsonic.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductSearchRequest {
    private List<String> brands;
    private List<String> categories;
    private List<Integer> prices;
    private Boolean sortByPrice;
    private String brand;
    private String category;
}
