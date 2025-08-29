package br.com.springboot.erp.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.springboot.erp.model.entity.Order;
import br.com.springboot.erp.model.entity.OrderItem;
import br.com.springboot.erp.model.entity.Status;

public class OrderTest {

    @Test
    @DisplayName("Status padrão deve ser PENDENTE")
    void defaultStatusShouldBePending() {
        Order order = new Order();
        assertThat(order.getStatus()).isEqualTo(Status.PENDENTE);
    }

    @Test
    @DisplayName("calculateTotal deve somar os subtotais dos itens")
    void calculateTotalShouldSumItems() {
        // mocks de OrderItem retornando subtotais
        OrderItem i1 = mock(OrderItem.class);
        when(i1.getSubtotal()).thenReturn(new BigDecimal("100.00"));

        OrderItem i2 = mock(OrderItem.class);
        when(i2.getSubtotal()).thenReturn(new BigDecimal("50.50"));

        Order order = new Order();
        order.setItems(Arrays.asList(i1, i2));

        BigDecimal total = order.calculateTotal();

        assertThat(total).isEqualByComparingTo("150.50");
    }

    @Test
    @DisplayName("calculateTotal com lista vazia deve retornar 0.00")
    void calculateTotalEmptyShouldBeZero() {
        Order order = new Order();
        order.setItems(Collections.emptyList());

        BigDecimal total = order.calculateTotal();

        assertThat(total).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("updateTotalAmount deve sincronizar o campo totalAmount com o cálculo")
    void updateTotalAmountShouldSyncField() {
        OrderItem i1 = mock(OrderItem.class);
        when(i1.getSubtotal()).thenReturn(new BigDecimal("10.10"));

        OrderItem i2 = mock(OrderItem.class);
        when(i2.getSubtotal()).thenReturn(new BigDecimal("5.90"));

        Order order = new Order();
        order.setItems(Arrays.asList(i1, i2));

        // antes, pode estar null
        assertThat(order.getTotalAmount()).isNull();

        order.updateTotalAmount();

        assertThat(order.getTotalAmount()).isEqualByComparingTo("16.00");
    }

    @Test
    @DisplayName("cancelOrder deve alterar status para CANCELADO")
    void cancelOrderShouldSetCanceled() {
        Order order = new Order();
        assertThat(order.getStatus()).isEqualTo(Status.PENDENTE);

        order.cancelOrder();

        assertThat(order.getStatus()).isEqualTo(Status.CANCELADO);
    }

    @Test
    @DisplayName("calculateTotal deve manter precisão de BigDecimal (sem perdas de escala)")
    void calculateTotalPrecision() {
        OrderItem i1 = mock(OrderItem.class);
        when(i1.getSubtotal()).thenReturn(new BigDecimal("0.10"));

        OrderItem i2 = mock(OrderItem.class);
        when(i2.getSubtotal()).thenReturn(new BigDecimal("0.20"));

        Order order = new Order();
        order.setItems(Arrays.asList(i1, i2));

        BigDecimal total = order.calculateTotal();

        // 0.10 + 0.20 = 0.30 (exato com BigDecimal)
        assertThat(total).isEqualByComparingTo("0.30");
    }
}
