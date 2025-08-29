package br.com.springboot.erp.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import br.com.springboot.erp.model.entity.Product;

public record ProductDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String sku
) implements Serializable {

    public static ProductDto from(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getSku()
        );
    }
}
