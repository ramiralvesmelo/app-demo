package br.com.springboot.erp.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Entidade que representa um produto.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    @DecimalMin("0.01")
    private BigDecimal price;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "sku", unique = true)
    private String sku;

    public BigDecimal calculateTotalValue() {
        if (stock == null || price == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(new BigDecimal(stock));
    }
}
