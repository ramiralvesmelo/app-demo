package br.com.springboot.erp.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Entidade que representa um item de pedido.
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    private Product product;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "subtotal")
    private BigDecimal subtotal;

    public BigDecimal getSubtotal() {
        if (quantity == null || unitPrice == null) {
            return BigDecimal.ZERO;
        }

        return unitPrice.multiply(new BigDecimal(quantity));
    }

    public void updateSubtotal() {
        this.subtotal = getSubtotal();
    }
}
