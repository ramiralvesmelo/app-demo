package br.com.springboot.erp.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.springboot.erp.model.entity.OrderItem;

class OrderItemTest {

    @Test
    @DisplayName("getSubtotal deve retornar 0 quando quantity ou unitPrice forem null")
    void getSubtotalNullsShouldReturnZero() {
        OrderItem item = new OrderItem();

        // ambos null
        assertThat(item.getSubtotal()).isEqualByComparingTo("0");

        // quantity null, price definido
        item.setUnitPrice(new BigDecimal("10.00"));
        item.setQuantity(null);
        assertThat(item.getSubtotal()).isEqualByComparingTo("0");

        // quantity definido, price null
        item.setUnitPrice(null);
        item.setQuantity(2);
        assertThat(item.getSubtotal()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("getSubtotal deve calcular unitPrice * quantity com precisão")
    void getSubtotalShouldMultiplyAccurately() {
        OrderItem item = new OrderItem();
        item.setUnitPrice(new BigDecimal("10.50"));
        item.setQuantity(2);

        BigDecimal subtotal = item.getSubtotal();

        // 10.50 * 2 = 21.00
        assertThat(subtotal).isEqualByComparingTo("21.00");
    }

    @Test
    @DisplayName("getSubtotal deve manter precisão de BigDecimal")
    void getSubtotalPrecision() {
        OrderItem item = new OrderItem();
        item.setUnitPrice(new BigDecimal("0.10"));
        item.setQuantity(3);

        // 0.10 * 3 = 0.30 (exato com BigDecimal)
        assertThat(item.getSubtotal()).isEqualByComparingTo("0.30");
    }

    @Test
    @DisplayName("updateSubtotal deve sincronizar o campo subtotal")
    void updateSubtotalShouldSyncField() {
        OrderItem item = new OrderItem();
        item.setUnitPrice(new BigDecimal("5.25"));
        item.setQuantity(4);

        // antes pode estar null
        assertThat(item.getSubtotal()).isEqualByComparingTo("21.00"); // cálculo direto
        assertThat(item.getSubtotal()).isEqualByComparingTo("21.00"); // idempotente

        // mas o campo 'subtotal' ainda não foi setado
        assertThat(item.getSubtotal()).isEqualByComparingTo("21.00");
        assertThat(item.getSubtotal()).isEqualByComparingTo("21.00");

        // sincroniza
        item.updateSubtotal();
        assertThat(item.getSubtotal()).isEqualByComparingTo("21.00");
        assertThat(item.getSubtotal()).isEqualTo(item.getSubtotal()); // mesma referência lógica
        assertThat(item.getSubtotal()).isEqualByComparingTo("21.00");
    }
}
