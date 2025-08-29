package br.com.springboot.erp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import br.com.springboot.erp.model.entity.Product;

/**
 * Serviço para gerenciamento de produtos.
 */
public interface ProductService {
    
    Product saveProduct(Product product);
    
    Optional<Product> findProductById(Long id);
    
    Optional<Product> findProductBySku(String sku);
    
    List<Product> findAllProducts();
    
    List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    void updateProductStock(Long productId, Integer newStock);
    
    void updateProductPrice(Long productId, BigDecimal newPrice);
    
    void deleteProduct(Long productId);
    
    BigDecimal calculateInventoryValue();
    
    List<Product> findProductsWithLowStock();
}