package br.com.springboot.erp.repository;

import br.com.springboot.erp.Application;
import br.com.springboot.erp.config.TestConfig;
import br.com.springboot.erp.model.entity.Product;
import br.com.springboot.erp.repository.ProductRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Testes de integração (JPA/Repository) para {@link Product}.
 *
 * 🎯 Objetivo - Validar consultas e operações básicas do
 * {@link ProductRepository} em um contexto real de persistência (H2 em
 * memória), cobrindo buscas, filtros e regras simples de estoque/preço.
 *
 * 🧪 Contexto - Runner: {@link SpringRunner} - App: {@link Application} -
 * Config: {@link TestConfig} (DataSource H2, JPA/Hibernate, transações) -
 * Perfil: {@code test} - Transações: {@link Transactional} garante
 * isolamento/rollback por teste
 *
 * 🔍 Cobertura - findBySku / not found - findByPriceGreaterThan - searchByName
 * (robustez contra injeção de SQL) - findProductsWithLowStock (regra de estoque
 * baixo)
 *
 * ⚠️ Observações e melhorias sugeridas (sem alterar o código) - Para busca por
 * nome, garanta que a query use parâmetros nomeados (JPQL) ou Criteria para
 * evitar qualquer risco de injeção. - Em regras de estoque, documente
 * claramente o critério (ex.: {@code stock < limite}) e considere índices nessa
 * coluna. - Em valores monetários, assegure {@link BigDecimal} com scale e
 * mapeamento adequados (precisão/scale na coluna).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
@Transactional
public class ProductRepositoryTest {

	@Autowired
	private ProductRepository productRepository;

	@PersistenceContext
	private EntityManager entityManager;

	private Product product1;
	private Product product2;
	private Product product3;

	@Before
	public void setUp() {
		// Massa de teste: 3 produtos com preços/estoques distintos
		product1 = new Product();
		product1.setName("Produto 1");
		product1.setDescription("Descrição do Produto 1");
		product1.setPrice(new BigDecimal("10.00"));
		product1.setStock(100);
		product1.setSku("SKU001");
		entityManager.persist(product1);

		product2 = new Product();
		product2.setName("Produto 2");
		product2.setDescription("Descrição do Produto 2");
		product2.setPrice(new BigDecimal("20.00"));
		product2.setStock(5);
		product2.setSku("SKU002");
		entityManager.persist(product2);

		product3 = new Product();
		product3.setName("Produto 3");
		product3.setDescription("Descrição do Produto 3");
		product3.setPrice(new BigDecimal("30.00"));
		product3.setStock(0);
		product3.setSku("SKU003");
		entityManager.persist(product3);

		entityManager.flush();
	}

	@Test
	public void testFindBySku() {
		// Deve localizar por SKU existente
		Optional<Product> foundProduct = productRepository.findBySku("SKU001");
		assertTrue("Produto deveria ser encontrado pelo SKU", foundProduct.isPresent());
		assertEquals("SKU001", foundProduct.get().getSku());
	}

	@Test
	public void testFindBySkuNotFound() {
		// SKU inexistente → Optional.empty
		Optional<Product> foundProduct = productRepository.findBySku("SKU999");
		assertFalse("Produto não deveria ser encontrado", foundProduct.isPresent());
	}

	@Test
	public void testFindByPriceGreaterThan() {
		// Filtra produtos com preço > 15.00 → espera SKU002 e SKU003
		List<Product> products = productRepository.findByPriceGreaterThan(new BigDecimal("15.00"));
		assertEquals("Deveria encontrar 2 produtos com preço maior que 15.00", 2, products.size());
		assertTrue("Deveria conter o produto 2", products.stream().anyMatch(p -> p.getSku().equals("SKU002")));
		assertTrue("Deveria conter o produto 3", products.stream().anyMatch(p -> p.getSku().equals("SKU003")));
	}

	@Test
	public void testSearchByName() {
		// Busca por fragmento "Produto" → deve trazer os 3
		List<Product> products = productRepository.searchByName("Produto");
		assertEquals("Deveria encontrar 3 produtos com 'Produto' no nome", 3, products.size());
	}

	@Test
	public void testSearchByNameWithSQLInjection() {
		// Entrada maliciosa não deve retornar itens (query deve ser
		// parametrizada/segura)
		List<Product> products = productRepository.searchByName("' OR '1'='1");
		assertTrue("A consulta não deveria ser vulnerável a injeção de SQL", products.size() == 0);
	}

	// FIXME: Erro na Query
	// SUGESTÃO: Verificar a condição do filtro (ex.: stock < :limite) e se há
	// índice/estatísticas no banco.
	@Test
	public void testFindProductsWithLowStock() {
		List<Product> products = productRepository.findProductsWithLowStock(10);

		// Deve retornar apenas os produtos com estoque abaixo de 10 (SKU002=5 e
		// SKU003=0)
		assertFalse("Não deveria conter o produto 1 (estoque alto)",
				products.stream().anyMatch(p -> p.getSku().equals("SKU001")));
		assertTrue("Deveria conter o produto 2 (estoque baixo)",
				products.stream().anyMatch(p -> p.getSku().equals("SKU002")));
		assertTrue("Deveria conter o produto 3 (estoque baixo)",
				products.stream().anyMatch(p -> p.getSku().equals("SKU003")));
	}
}
