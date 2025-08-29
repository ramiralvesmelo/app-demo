package br.com.springboot.erp.repository;

import org.springframework.stereotype.Repository;

import br.com.springboot.erp.model.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para a entidade Product.
 */
@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    List<Product> findByPriceGreaterThan(BigDecimal minPrice);

    List<Product> searchByName(String name);

    List<Product> findProductsWithLowStock(Integer minStock);

}
