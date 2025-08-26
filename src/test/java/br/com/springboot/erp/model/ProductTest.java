package br.com.springboot.erp.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    @DisplayName("calculateTotalValue deve retornar 0 quando stock ou price forem null")
    void calculateTotalValueNullsShouldReturnZero() {
        Product p = new Product();

        // ambos null
        assertThat(p.calculateTotalValue()).isEqualByComparingTo("0");

        // stock null, price definido
        p.setPrice(new BigDecimal("10.00"));
        p.setStock(null);
        assertThat(p.calculateTotalValue()).isEqualByComparingTo("0");

        // stock definido, price null
        p.setPrice(null);
        p.setStock(5);
        assertThat(p.calculateTotalValue()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("calculateTotalValue deve multiplicar price * stock com precisão")
    void calculateTotalValueShouldMultiplyAccurately() {
        Product p = new Product();
        p.setPrice(new BigDecimal("10.50"));
        p.setStock(3);

        // 10.50 * 3 = 31.50
        assertThat(p.calculateTotalValue()).isEqualByComparingTo("31.50");
    }

    @Test
    @DisplayName("calculateTotalValue com stock = 0 deve resultar em 0.00")
    void calculateTotalValueWithZeroStock() {
        Product p = new Product();
        p.setPrice(new BigDecimal("7.25"));
        p.setStock(0);

        assertThat(p.calculateTotalValue()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("calculateTotalValue deve manter precisão para valores pequenos")
    void calculateTotalValuePrecisionSmall() {
        Product p = new Product();
        p.setPrice(new BigDecimal("0.10"));
        p.setStock(3);

        // 0.10 * 3 = 0.30 (exato com BigDecimal)
        assertThat(p.calculateTotalValue()).isEqualByComparingTo("0.30");
    }
}
