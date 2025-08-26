package br.com.springboot.erp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import br.com.springboot.erp.model.Customer;
import br.com.springboot.erp.model.Order;
import br.com.springboot.erp.model.OrderItem;
import br.com.springboot.erp.model.Product;
import br.com.springboot.erp.repository.CustomerRepository;
import br.com.springboot.erp.repository.ProductRepository;
import br.com.springboot.erp.service.OrderServiceImpl;
import br.com.springboot.erp.service.ProductService;

/**
 * Testes unitários para o {@link OrderServiceImpl}.
 *
 * 🎯 Objetivo
 *  - Validar a lógica do serviço de pedidos de forma isolada, mockando todas as dependências externas
 *    (repositories e EntityManager).
 *  - Garantir chamadas corretas ao EntityManager e repositórios, além de estados esperados em operações.
 *
 * 🧪 Estratégia
 *  - Usa {@link MockitoJUnitRunner} para injeção de mocks.
 *  - Repositórios, serviços auxiliares e {@link EntityManager} são simulados via Mockito.
 *  - Operações CRUD são verificadas por meio de `verify(...)`.
 *
 * 🔍 Cobertura
 *  - createOrder / findOrderById / findOrderByNumber / findAllOrders / findOrdersByCustomerId
 *  - addItemToOrder / removeItemFromOrder / updateOrderItem
 *  - calculateOrderTotal / finalizeOrder / cancelOrder
 *  - Cenário de erro: createOrder com cliente inválido
 *
 * ⚠️ Observações
 *  - Esse teste valida apenas comportamento e interação com dependências (não persiste dados reais).
 *  - Para validar integração com banco (queries JPQL, constraints), há testes de integração específicos.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Order> typedQuery;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer customer;
    private Product product;
    private Order order;
    private OrderItem orderItem;

    @Before
    public void setUp() throws Exception {
        // Inicializa o EntityManager com um mock
        entityManager = mock(EntityManager.class);

        // Cria cliente para testes
        customer = new Customer();
        customer.setId(1L);
        customer.setName("João Silva");
        customer.setEmail("joao.silva@example.com");
        customer.setOrders(new ArrayList<>());

        // Cria produto para testes
        product = new Product();
        product.setId(1L);
        product.setName("Produto 1");
        product.setPrice(new BigDecimal("10.00"));
        product.setStock(100);
        product.setSku("SKU001");

        // Cria pedido para testes
        order = new Order();
        order.setId(1L);
        order.setOrderNumber("ORD-001");
        order.setOrderDate(LocalDateTime.now());
        order.setCustomer(customer);
        order.setItems(new ArrayList<>());
        order.setTotalAmount(BigDecimal.ZERO);

        // Cria item de pedido para testes
        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(new BigDecimal("10.00"));
        orderItem.setSubtotal(new BigDecimal("20.00"));

        // Adiciona o item ao pedido
        order.getItems().add(orderItem);

        // Configura o mock do EntityManager para retornar o pedido quando find for chamado
        when(entityManager.find(eq(Order.class), eq(1L))).thenReturn(order);

        // Configura o mock do TypedQuery
        when(entityManager.createQuery(anyString(), eq(Order.class))).thenReturn(typedQuery);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(order);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(order));

        // Injeta o EntityManager no OrderServiceImpl usando reflection
        Field entityManagerField = OrderServiceImpl.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        entityManagerField.set(orderService, entityManager);
    }

    @Test
    public void testCreateOrder() {
        // Configura os mocks
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        doNothing().when(entityManager).persist(any(Order.class));
        doNothing().when(entityManager).persist(any(OrderItem.class));

        // Cria lista de itens para o pedido
        List<OrderItem> items = new ArrayList<>();
        items.add(orderItem);

        // Executa o método
        Order createdOrder = orderService.createOrder(1L, items);

        // Verifica o resultado
        assertNotNull("Pedido criado não deveria ser nulo", createdOrder);
        assertEquals("Pedido deveria ter o cliente correto", customer, createdOrder.getCustomer());

        // Verifica se os métodos foram chamados
        verify(customerRepository, times(1)).findById(1L);
        // Verifica que persist foi chamado pelo menos uma vez para Order e uma vez para OrderItem
        // Não verificamos o número exato de chamadas porque isso pode variar dependendo da implementação
        verify(entityManager, atLeastOnce()).persist(any(Order.class));
        verify(entityManager, atLeastOnce()).persist(any(OrderItem.class));

    }

    @Test
    public void testFindOrderById() {
        // Executa o método
        Optional<Order> foundOrder = orderService.findOrderById(1L);

        // Verifica o resultado
        assertTrue("Pedido deveria ser encontrado", foundOrder.isPresent());
        assertEquals("Pedido encontrado deveria ter o ID correto", 1L, foundOrder.get().getId().longValue());

        // Verifica se o método foi chamado
        verify(entityManager, times(1)).find(Order.class, 1L);
    }

    @Test
    public void testFindOrderByNumber() {
        // Configura o mock
        when(typedQuery.getSingleResult()).thenReturn(order);

        // Executa o método
        Optional<Order> foundOrder = orderService.findOrderByNumber("ORD-001");

        // Verifica o resultado
        assertTrue("Pedido deveria ser encontrado", foundOrder.isPresent());
        assertEquals("Pedido encontrado deveria ter o número correto", "ORD-001", foundOrder.get().getOrderNumber());

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).createQuery(anyString(), eq(Order.class));
        verify(typedQuery, times(1)).setParameter("orderNumber", "ORD-001");
        verify(typedQuery, times(1)).getSingleResult();
    }

    @Test
    public void testFindAllOrders() {
        // Configura o mock
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(order));

        // Executa o método
        List<Order> orders = orderService.findAllOrders();

        // Verifica o resultado
        assertEquals("Deveria encontrar 1 pedido", 1, orders.size());

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).createQuery(anyString(), eq(Order.class));
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    public void testFindOrdersByCustomerId() {
        // Configura o mock
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(order));

        // Executa o método
        List<Order> orders = orderService.findOrdersByCustomerId(1L);

        // Verifica o resultado
        assertEquals("Deveria encontrar 1 pedido", 1, orders.size());

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).createQuery(anyString(), eq(Order.class));
        verify(typedQuery, times(1)).setParameter("customerId", 1L);
        verify(typedQuery, times(1)).getResultList();
    }

    @Test
    public void testAddItemToOrder() {
        // Configura os mocks
        doNothing().when(entityManager).persist(any(OrderItem.class));

        // Cria um novo item para adicionar
        OrderItem newItem = new OrderItem();
        newItem.setProduct(product);
        newItem.setQuantity(1);
        newItem.setUnitPrice(new BigDecimal("10.00"));

        // Executa o método
        orderService.addItemToOrder(1L, newItem);

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).find(Order.class, 1L);
        verify(entityManager, times(1)).persist(any(OrderItem.class));

    }

    @Test
    public void testRemoveItemFromOrder() {
        // Configura os mocks
        doNothing().when(entityManager).remove(any(OrderItem.class));

        // Executa o método
        orderService.removeItemFromOrder(1L, 1L);

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).find(Order.class, 1L);
        verify(entityManager, times(1)).remove(any(OrderItem.class));

    }

    @Test
    public void testUpdateOrderItem() {
        // Configura os mocks
        when(entityManager.merge(any(OrderItem.class))).thenReturn(orderItem);

        // Executa o método
        orderService.updateOrderItem(1L, orderItem);

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).find(Order.class, 1L);
        verify(entityManager, times(1)).merge(any(OrderItem.class));

    }

    @Test
    public void testCalculateOrderTotal() {
        // Cria um mock de Order para poder mockar o método calculateTotal
        Order mockOrder = mock(Order.class);
        when(mockOrder.calculateTotal()).thenReturn(new BigDecimal("20.00"));

        // Configura o entityManager para retornar o mock de Order
        when(entityManager.find(eq(Order.class), eq(1L))).thenReturn(mockOrder);

        // Executa o método
        BigDecimal total = orderService.calculateOrderTotal(1L);

        // Verifica o resultado
        assertEquals("Valor total deveria ser 20.00", new BigDecimal("20.00"), total);

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).find(Order.class, 1L);
        verify(mockOrder, times(1)).calculateTotal();

    }

    @Test
    public void testFinalizeOrder() {
        // Cria um mock de Order para poder mockar o método calculateTotal
        Order mockOrder = mock(Order.class);
        when(mockOrder.calculateTotal()).thenReturn(new BigDecimal("20.00"));

        // Configura o entityManager para retornar o mock de Order
        when(entityManager.find(eq(Order.class), eq(1L))).thenReturn(mockOrder);

        // Executa o método
        orderService.finalizeOrder(1L);

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).find(Order.class, 1L);
        verify(mockOrder, times(1)).calculateTotal();
        verify(mockOrder, times(1)).setTotalAmount(new BigDecimal("20.00"));

    }

    @Test
    public void testCancelOrder() {
        // Executa o método
        orderService.cancelOrder(1L);

        // Verifica se os métodos foram chamados
        verify(entityManager, times(1)).find(Order.class, 1L);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateOrderWithInvalidCustomerId() {
        // Configura o mock para retornar um cliente vazio
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // Executa o método - deve lançar IllegalArgumentException
        orderService.createOrder(999L, new ArrayList<>());
    }
}
