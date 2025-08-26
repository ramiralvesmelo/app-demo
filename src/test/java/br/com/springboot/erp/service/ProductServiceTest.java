package br.com.springboot.erp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import br.com.springboot.erp.model.Product;
import br.com.springboot.erp.repository.ProductRepository;
import br.com.springboot.erp.service.ProductServiceImpl;

/**
 * Testes unitários do {@link ProductServiceImpl}.
 *
 * 🎯 Objetivo - Validar regras do serviço de produtos de forma isolada,
 * mockando o {@link ProductRepository}. - Verificar chamadas ao repositório e o
 * comportamento para casos válidos/ inválidos.
 *
 * 🔍 Cobertura - saveProduct (válido, preço negativo, preço zero) -
 * findProductById / findProductBySku / findAllProducts -
 * findProductsByPriceRange - updateProductStock / updateProductPrice -
 * deleteProduct - calculateInventoryValue (incluindo estoque nulo) -
 * findProductsWithLowStock
 *
 * ⚠️ Notas - Alguns testes possuem FIXME sugerindo ajustes de regra/asserções;
 * comentários explicam a intenção.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private ProductServiceImpl productService;

	private Product product1;
	private Product product2;
	private Product product3;

	@Before
	public void setUp() {
		// Massa de teste: 3 produtos com características distintas
		product1 = new Product();
		product1.setId(1L);
		product1.setName("Produto 1");
		product1.setDescription("Descrição do Produto 1");
		product1.setPrice(new BigDecimal("10.00"));
		product1.setStock(100);
		product1.setSku("SKU001");

		product2 = new Product();
		product2.setId(2L);
		product2.setName("Produto 2");
		product2.setDescription("Descrição do Produto 2");
		product2.setPrice(new BigDecimal("20.00"));
		product2.setStock(5);
		product2.setSku("SKU002");

		product3 = new Product();
		product3.setId(3L);
		product3.setName("Produto 3");
		product3.setDescription("Descrição do Produto 3");
		product3.setPrice(new BigDecimal("30.00"));
		product3.setStock(0);
		product3.setSku("SKU003");
	}

	@Test
	public void testSaveProduct() {
		// arrange
		when(productRepository.save(any(Product.class))).thenReturn(product1);

		// act
		Product savedProduct = productService.saveProduct(product1);

		// assert
		assertNotNull("Produto salvo não deveria ser nulo", savedProduct);
		assertEquals("Produto salvo deveria ter o mesmo ID", product1.getId(), savedProduct.getId());
		verify(productRepository, times(1)).save(product1);
	}

	// FIXME: Falha de regra de negócio documentada:
	// Intenção: não permitir preços negativos e lançar IllegalArgumentException.
	// SUGESTÃO: Implementar validação no service e manter o expected abaixo.
	@Test(expected = IllegalArgumentException.class)
	public void testSaveProductWithNegativePrice() {
		// arrange: produto inválido (preço negativo)
		Product invalidProduct = new Product();
		invalidProduct.setName("Produto Inválido");
		invalidProduct.setPrice(new BigDecimal("-10.00"));

		// Atenção: lenient() evita conflito com o expected; o método idealmente nem
		// deveria chamar save()
		lenient().when(productRepository.save(any(Product.class))).thenReturn(invalidProduct);

		// act
		Product savedProduct = productService.saveProduct(invalidProduct);

		// assert (não deveria chegar aqui se a exceção for lançada antes)
		assertNull("Produto com preço negativo foi salvo", savedProduct);
		verify(productRepository, times(0)).save(invalidProduct);
	}

	@Test
	public void testFindProductById() {
		// arrange
		when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

		// act
		Optional<Product> foundProduct = productService.findProductById(1L);

		// assert
		assertTrue("Produto deveria ser encontrado", foundProduct.isPresent());
		assertEquals("ID deveria ser 1L", 1L, foundProduct.get().getId().longValue());
		verify(productRepository, times(1)).findById(1L);
	}

	@Test
	public void testFindProductBySku() {
		// arrange
		when(productRepository.findBySku("SKU001")).thenReturn(Optional.of(product1));

		// act
		Optional<Product> foundProduct = productService.findProductBySku("SKU001");

		// assert
		assertTrue("Produto deveria ser encontrado", foundProduct.isPresent());
		assertEquals("SKU deveria ser SKU001", "SKU001", foundProduct.get().getSku());
		verify(productRepository, times(1)).findBySku("SKU001");
	}

	@Test
	public void testFindAllProducts() {
		// arrange
		when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));

		// act
		List<Product> products = productService.findAllProducts();

		// assert
		assertEquals("Deveria encontrar 3 produtos", 3, products.size());
		verify(productRepository, times(1)).findAll();
	}

	@Test
	public void testFindProductsByPriceRange() {
		// arrange: range 15–25 deve usar estratégia atual (consulta > min e filtra <=
		// max no service)
		when(productRepository.findByPriceGreaterThan(new BigDecimal("15.00")))
				.thenReturn(Arrays.asList(product2, product3));

		// act
		List<Product> products = productService.findProductsByPriceRange(new BigDecimal("15.00"),
				new BigDecimal("25.00"));

		// assert
		assertEquals("Deveria encontrar 2 produtos", 2, products.size());
		verify(productRepository, times(1)).findByPriceGreaterThan(new BigDecimal("15.00"));
	}

	@Test
	public void testUpdateProductStock() {
		// arrange
		when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
		when(productRepository.save(any(Product.class))).thenReturn(product1);

		// act
		productService.updateProductStock(1L, 50);

		// assert (interações)
		verify(productRepository, times(1)).findById(1L);
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	public void testUpdateProductPrice() {
		// arrange
		when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
		when(productRepository.save(any(Product.class))).thenReturn(product1);

		// act
		productService.updateProductPrice(1L, new BigDecimal("15.00"));

		// assert (interações)
		verify(productRepository, times(1)).findById(1L);
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	public void testDeleteProduct() {
		// arrange
		doNothing().when(productRepository).deleteById(anyLong());

		// act
		productService.deleteProduct(1L);

		// assert
		verify(productRepository, times(1)).deleteById(1L);
	}

	@Test
	public void testCalculateInventoryValue() {
		// arrange
		when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3));

		// act
		BigDecimal totalValue = productService.calculateInventoryValue();

		// assert
		// product1: 10 * 100 = 1000
		// product2: 20 * 5 = 100
		// product3: 30 * 0 = 0
		assertEquals("Valor total deveria ser 1100.00", new BigDecimal("1100.00"), totalValue);
		verify(productRepository, times(1)).findAll();
	}

	@Test
	public void testFindProductsWithLowStock() {
		// arrange
		when(productRepository.findProductsWithLowStock(10)).thenReturn(Arrays.asList(product2, product3));

		// act
		List<Product> products = productService.findProductsWithLowStock();

		// assert
		assertEquals("Deveria encontrar 2 produtos com estoque baixo", 2, products.size());
		verify(productRepository, times(1)).findProductsWithLowStock(10);
	}

	@Test
	public void testCalculateInventoryValueWithNullStock() {
		// arrange: produto com estoque nulo deve ser tratado como zero
		Product productWithNullStock = new Product();
		productWithNullStock.setId(4L);
		productWithNullStock.setName("Produto 4");
		productWithNullStock.setPrice(new BigDecimal("40.00"));
		productWithNullStock.setStock(null);

		when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2, product3, productWithNullStock));

		// act
		BigDecimal totalValue = productService.calculateInventoryValue();

		// assert
		assertEquals("Valor total deveria ser 1100.00", new BigDecimal("1100.00"), totalValue);
		verify(productRepository, times(1)).findAll();
	}

	// FIXME: Não aceitar preço zero (<= 0). Intenção: lançar
	// IllegalArgumentException.
	// SUGESTÃO: validar no service e manter expected.
	@Test(expected = IllegalArgumentException.class)
	public void testSaveProductWithZeroPrice() {
		// arrange
		Product invalidProduct = new Product();
		invalidProduct.setName("Produto Inválido");
		invalidProduct.setPrice(BigDecimal.ZERO);

		lenient().when(productRepository.save(any(Product.class))).thenReturn(invalidProduct);

		// act
		Product savedProduct = productService.saveProduct(invalidProduct);

		// assert (não deveria chegar aqui)
		assertNull("Produto com preço zero foi salvo", savedProduct);
		verify(productRepository, times(1)).save(invalidProduct);
	}

	// FIXME: Ajustar validação do cenário de estoque negativo conforme regra de
	// negócio.
	// SUGESTÃO: se a regra proibir, lançar exceção; se permitir normalização,
	// garantir persistência do ajuste.
	@Test()
	public void testUpdateProductStockWithNegativeValue() {
		// arrange
		lenient().when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
		lenient().when(productRepository.save(any(Product.class))).thenReturn(product1);

		// act
		productService.updateProductStock(1L, -10);

		// assert (atual: apenas verifica interação; ajuste conforme sua regra)
		verify(productRepository, times(1)).findById(1L);
		verify(productRepository, times(1)).save(any(Product.class));
	}
}
