package br.com.springboot.erp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import br.com.springboot.erp.TesteApplication;
import br.com.springboot.erp.config.TestConfig;
import br.com.springboot.erp.model.Customer;
import br.com.springboot.erp.model.Order;
import br.com.springboot.erp.model.OrderItem;
import br.com.springboot.erp.model.Product;
import br.com.springboot.erp.model.Status;
import br.com.springboot.erp.repository.ProductRepository;
import br.com.springboot.erp.service.OrderService;

/**
 * Testes de integração do {@link OrderService}.
 *
 * 🎯 Objetivo - Validar as regras de negócio de pedidos ponta a ponta com
 * JPA/Hibernate (H2 em memória), incluindo criação/atualização de itens,
 * cálculo de total, finalização/cancelamento e efeitos em estoque.
 *
 * 🧪 Contexto - Runner: {@link SpringRunner} - App: {@link TesteApplication} -
 * Config: {@link TestConfig} (DataSource H2, JPA/Hibernate, transações) -
 * Perfil: {@code test} - Transações: {@link Transactional} garante rollback e
 * isolamento por teste
 *
 * 🔍 Cobertura - createOrder / findOrderById / findOrderByNumber /
 * findAllOrders / findOrdersByCustomerId - addItemToOrder / removeItemFromOrder
 * / updateOrderItem - calculateOrderTotal / finalizeOrder / cancelOrder -
 * Regras de erro: id inválido, número único de pedido, quantidade negativa
 *
 * ⚠️ Pontos de atenção e melhorias sugeridas - Propagação transacional:
 * preferir {@code REQUIRED} em métodos do serviço que interagem entre si. -
 * Consistência de modelo: manter listas bidirecionais em memória (ex.:
 * {@code order.getItems().add(item)}). - Estoque: decrementar ao finalizar e
 * restaurar ao cancelar; persistir com merge/save conforme estratégia. -
 * Geração de orderNumber: evitar colisão (usar timestamp + sufixo
 * aleatório/sequence). - Status: garantir transição correta (ex.:
 * {@link Status#CANCELADO} ao cancelar).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TesteApplication.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class OrderServiceIntegrationTest {

	// @Autowired
	// private CustomerRepository customerRepository;

	@Autowired
	private ProductRepository productRepository;

	// @Autowired
	// private ProductService productService;

	@Autowired
	private OrderService orderService;

	@PersistenceContext
	private EntityManager entityManager;

	private Customer customer;
	private Product product;
	private Order order;
	private OrderItem orderItem;

	@Before
	public void setUp() {
		// Limpa massa anterior para garantir isolamento
		entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.createQuery("DELETE FROM Product").executeUpdate();
		entityManager.createQuery("DELETE FROM Customer").executeUpdate();

		// Cliente base
		customer = new Customer();
		customer.setName("João Silva");
		customer.setEmail("joao.silva@example.com");
		customer.setPhone("(11) 99999-1111");
		customer.setOrders(new ArrayList<>());
		entityManager.persist(customer);

		// Produto base
		product = new Product();
		product.setName("Produto 1");
		product.setDescription("Descrição do Produto 1");
		product.setPrice(new BigDecimal("10.00"));
		product.setStock(100);
		product.setSku("SKU001");
		entityManager.persist(product);

		// Pedido base
		order = new Order();
		order.setOrderNumber("ORD-001");
		order.setOrderDate(LocalDateTime.now());
		order.setCustomer(customer);
		order.setItems(new ArrayList<>());
		order.setTotalAmount(BigDecimal.ZERO);
		entityManager.persist(order);

		// Relaciona pedido ao cliente
		List<Order> orders = new ArrayList<>();
		orders.add(order);
		customer.setOrders(orders);
		entityManager.persist(customer);

		// Item base (2 x 10.00 = 20.00)
		orderItem = new OrderItem();
		orderItem.setOrder(order);
		orderItem.setProduct(product);
		orderItem.setQuantity(2);
		orderItem.setUnitPrice(new BigDecimal("10.00"));
		orderItem.setSubtotal(new BigDecimal("20.00"));
		entityManager.persist(orderItem);

		// Mantém a relação em memória (evita inconsistências)
		order.getItems().add(orderItem);
		entityManager.persist(order);

		entityManager.flush();
	}

	// FIXME: Itens não vinculados em memória na criação
	// SUGESTÃO: Na implementação, além de persistir, fazer
	// order.getItems().add(item) para refletir no objeto retornado
	@Test
	public void testCreateOrder() {
		List<OrderItem> items = new ArrayList<>();

		OrderItem newItem = new OrderItem();
		newItem.setProduct(product);
		newItem.setQuantity(3);
		newItem.setUnitPrice(new BigDecimal("10.00"));
		items.add(newItem);

		Order createdOrder = orderService.createOrder(customer.getId(), items);

		assertNotNull("Pedido criado não deveria ser nulo", createdOrder);
		assertNotNull("Pedido criado deveria ter um ID", createdOrder.getId());
		assertEquals("Pedido deveria ter o cliente correto", customer.getId(), createdOrder.getCustomer().getId());
		assertEquals("Pedido deveria ter 1 item", 1, createdOrder.getItems().size());

		Optional<Order> foundOrder = orderService.findOrderById(createdOrder.getId());
		assertTrue("Pedido deveria ser encontrado no banco de dados", foundOrder.isPresent());
	}

	@Test
	public void testFindOrderById() {
		Optional<Order> foundOrder = orderService.findOrderById(order.getId());

		assertTrue("Pedido deveria ser encontrado", foundOrder.isPresent());
		assertEquals("Pedido encontrado deveria ter o ID correto", order.getId(), foundOrder.get().getId());
		assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", foundOrder.get().getOrderNumber());
	}

	@Test
	public void testFindOrderByNumber() {
		Optional<Order> foundOrder = orderService.findOrderByNumber("ORD-001");

		assertTrue("Pedido deveria ser encontrado", foundOrder.isPresent());
		assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", foundOrder.get().getOrderNumber());
	}

	@Test
	public void testFindAllOrders() {
		List<Order> orders = orderService.findAllOrders();

		assertEquals("Deveria encontrar 1 pedido", 1, orders.size());
		assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", orders.get(0).getOrderNumber());
	}

	@Test
	public void testFindOrdersByCustomerId() {
		List<Order> orders = orderService.findOrdersByCustomerId(customer.getId());

		assertEquals("Deveria encontrar 1 pedido", 1, orders.size());
		assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", orders.get(0).getOrderNumber());
	}

	// FIXME: addItemToOrder com REQUIRES_NEW isola a transação e não vê dados do
	// @Before
	// SUGESTÃO: usar Propagation.REQUIRED para compartilhar a transação do teste
	//
	// FIXME: após persistir o item, refletir na coleção em memória do pedido
	@Test
	public void testAddItemToOrder() {
		OrderItem newItem = new OrderItem();
		newItem.setProduct(product);
		newItem.setQuantity(1);
		newItem.setUnitPrice(new BigDecimal("10.00"));

		orderService.addItemToOrder(order.getId(), newItem);

		Optional<Order> updatedOrder = orderService.findOrderById(order.getId());
		assertTrue("Pedido deveria ser encontrado", updatedOrder.isPresent());
		assertEquals("Pedido deveria ter 2 itens", 2, updatedOrder.get().getItems().size());
	}

	// FIXME: Remover item da lista em memória também (evitar referência "fantasma")
	@Test
	public void testRemoveItemFromOrder() {
		orderService.removeItemFromOrder(order.getId(), orderItem.getId());

		Optional<Order> updatedOrder = orderService.findOrderById(order.getId());
		assertTrue("Pedido deveria ser encontrado", updatedOrder.isPresent());
		assertEquals("Pedido não deveria ter itens", 0, updatedOrder.get().getItems().size());
	}

	@Test
	public void testUpdateOrderItem() {
		orderItem.setQuantity(5);
		orderItem.setUnitPrice(new BigDecimal("12.00"));

		orderService.updateOrderItem(order.getId(), orderItem);

		Optional<Order> updatedOrder = orderService.findOrderById(order.getId());
		assertTrue("Pedido deveria ser encontrado", updatedOrder.isPresent());
		assertEquals("Item deveria ter a quantidade atualizada", 5,
				(int) updatedOrder.get().getItems().get(0).getQuantity());
		assertEquals("Item deveria ter o preço unitário atualizado", new BigDecimal("12.00"),
				updatedOrder.get().getItems().get(0).getUnitPrice());
	}

	@Test
	public void testCalculateOrderTotal() {
		BigDecimal total = orderService.calculateOrderTotal(order.getId());
		assertEquals("Valor total deveria ser 20.00", new BigDecimal("20.00"), total);
	}

	@Test
	public void testFinalizeOrder() {
		orderService.finalizeOrder(order.getId());

		Optional<Order> finalizedOrder = orderService.findOrderById(order.getId());
		assertTrue("Pedido deveria ser encontrado", finalizedOrder.isPresent());
		assertEquals("Valor total do pedido deveria ser 20.00", new BigDecimal("20.00"),
				finalizedOrder.get().getTotalAmount());

		Optional<Product> updatedProduct = productRepository.findById(product.getId());
		assertTrue("Produto deveria ser encontrado", updatedProduct.isPresent());
		assertEquals("Estoque do produto deveria ser atualizado", 98, updatedProduct.get().getStock().intValue());
	}

	// FIXME: finalizeOrder deve iterar itens, decrementar estoque e persistir
	// alterações (product.setStock(...))
	// SUGESTÃO: após decrementar, salvar/merge no repositório

	// FIXME: cancelOrder deve setar Status.CANCELADO e restaurar estoque
	@Test
	public void testCancelOrder() {
		orderService.cancelOrder(order.getId());

		Optional<Order> canceledOrder = orderService.findOrderById(order.getId());
		assertTrue("Pedido deveria ser encontrado", canceledOrder.isPresent());
		assertEquals("Status do pedido deveria ser CANCELADO", "CANCELADO", canceledOrder.get().getStatus().name());

		Optional<Product> updatedProduct = productRepository.findById(product.getId());
		assertTrue("Produto deveria ser encontrado", updatedProduct.isPresent());
		assertEquals("Estoque do produto deveria ser restaurado", 100, (int) updatedProduct.get().getStock());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateOrderWithInvalidCustomerId() {
		// Cliente inexistente → deve lançar IllegalArgumentException
		orderService.createOrder(999L, new ArrayList<>());
	}

	// FIXME: geração de número de ordem não pode colidir
	// SUGESTÃO: use timestamp + sufixo aleatório/sequence (ex.:
	// yyyyMMddHHmmss-XXXX)
	@Test
	public void testOrderNumberUniqueness() {
		Order order1 = orderService.createOrder(customer.getId(), new ArrayList<>());
		Order order2 = orderService.createOrder(customer.getId(), new ArrayList<>());

		assertNotEquals("Os números dos pedidos deveriam ser diferentes", order1.getOrderNumber(),
				order2.getOrderNumber());
	}

	// FIXME: validar quantidade > 0 ao adicionar/atualizar item
	@Test(expected = IllegalArgumentException.class)
	public void testNegativeQuantityInOrderItem() {
		OrderItem negativeItem = new OrderItem();
		negativeItem.setProduct(product);
		negativeItem.setQuantity(-5);
		negativeItem.setUnitPrice(new BigDecimal("10.00"));

		orderService.addItemToOrder(order.getId(), negativeItem);
	}
}
