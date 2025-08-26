package br.com.springboot.erp.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.springboot.erp.model.Product;
import br.com.springboot.erp.repository.ProductRepository;

/**
 * Implementação do serviço para gerenciamento de produtos.
 */
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Product saveProduct(Product product) {
        if (product.getPrice() != null && product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }    	
        return productRepository.save(product);
    }

    @Override
    public Optional<Product> findProductById(Long id) {	
        return productRepository.findById(id);
    }

    @Override
    public Optional<Product> findProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceGreaterThan(minPrice);
    }

    @Override
    public void updateProductStock(Long productId, Integer newStock) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(newStock);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public void updateProductPrice(Long productId, BigDecimal newPrice) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setPrice(newPrice);
            productRepository.save(product);
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public BigDecimal calculateInventoryValue() {
        List<Product> products = productRepository.findAll();
        
        return products.stream()
                .map(Product::calculateTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Product> findProductsWithLowStock() {
        return productRepository.findProductsWithLowStock(10);
    }
}