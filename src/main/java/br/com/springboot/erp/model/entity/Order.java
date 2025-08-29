package br.com.springboot.erp.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidade que representa um pedido.
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true)
    private String orderNumber;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @ManyToOne
    @JsonBackReference
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDENTE;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    public BigDecimal calculateTotal() {
        // Não considera descontos ou impostos
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getSubtotal());
        }
        return total;
    }

    public void updateTotalAmount() {
        this.totalAmount = calculateTotal();
    }

    public void cancelOrder() {
        this.status = Status.CANCELADO;
    }

    public Status getStatus() {
        return status;
    }
}