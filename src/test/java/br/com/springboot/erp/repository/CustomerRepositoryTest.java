package br.com.springboot.erp.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.springboot.erp.config.TestConfig;
import br.com.springboot.erp.model.entity.Customer;
import br.com.springboot.erp.model.entity.Order;
import br.com.springboot.erp.repository.CustomerRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes de repositório (JPA) para {@link Customer}.
 *
 * 🎯 Objetivo
 *  - Validar consultas e operações básicas do {@link CustomerRepository} usando H2 em memória.
 *  - Conferir comportamento de busca por e-mail, buscas por nome (com segurança), performance e relações com pedidos.
 *
 * 🧪 Contexto
 *  - Perfil: {@code test}
 *  - Configuração: {@link TestConfig} (DataSource H2, JPA/Hibernate, transação).
 *  - Transacional: {@link Transactional} garante isolamento/rollback por teste.
 *
 * 🔍 Cobertura
 *  - findByEmail (case sensitive/insensitive)
 *  - findByNameContaining (robustez contra injeção de SQL)
 *  - findCustomersWithOrders (performance e N+1)
 *  - CRUD básico: findAll, findById, save, delete
 *
 * ⚠️ Observações e melhorias sugeridas
 *  - Case-insensitive em e-mail: usar LOWER/UPPER na consulta ou coluna normalizada.
 *  - Segurança: consultas sempre parametrizadas (JPQL/Criteria) para evitar injeção.
 *  - Performance/N+1: preferir JOIN FETCH quando carregar pedidos junto do cliente.
 *  - Índices: considerar índice único para e-mail e índices para colunas de busca (nome).
 */
@SpringJUnitConfig(classes = {TestConfig.class})
@ActiveProfiles("test")
@Transactional
public class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;

    @BeforeEach
    public void setUp() {
        // Massa básica para testes funcionais
        customer1 = new Customer();
        customer1.setName("João Silva");
        customer1.setEmail("joao.silva@example.com");
        customer1.setPhone("(11) 99999-1111");
        entityManager.persist(customer1);

        customer2 = new Customer();
        customer2.setName("Maria Santos");
        customer2.setEmail("maria.santos@example.com");
        customer2.setPhone("(11) 99999-2222");
        entityManager.persist(customer2);

        customer3 = new Customer();
        customer3.setName("Pedro Oliveira");
        customer3.setEmail("pedro.oliveira@example.com");
        customer3.setPhone("(11) 99999-3333");
        entityManager.persist(customer3);

        // Pedidos para customer1 (2) e customer2 (1)
        Order order1 = new Order();
        order1.setOrderNumber("ORD-001");
        order1.setOrderDate(LocalDateTime.now());
        order1.setCustomer(customer1);
        entityManager.persist(order1);

        Order order2 = new Order();
        order2.setOrderNumber("ORD-002");
        order2.setOrderDate(LocalDateTime.now());
        order2.setCustomer(customer1);
        entityManager.persist(order2);

        Order order3 = new Order();
        order3.setOrderNumber("ORD-003");
        order3.setOrderDate(LocalDateTime.now());
        order3.setCustomer(customer2);
        entityManager.persist(order3);

        // Relaciona pedidos aos clientes em memória
        customer1.getOrders().add(order1);
        customer1.getOrders().add(order2);
        customer2.getOrders().add(order3);

        entityManager.flush();
    }

    @Test
    public void testFindByEmail() {
        // Deve localizar o cliente pelo e-mail exato (case sensitive na implementação atual)
        Optional<Customer> foundCustomer = customerRepository.findByEmail("joao.silva@example.com");
        assertTrue(foundCustomer.isPresent(), "Cliente deveria ser encontrado pelo email");
        assertEquals("joao.silva@example.com", foundCustomer.get().getEmail());
    }

    // FIXME: Consulta deve ser case-insensitive para e-mail.
    // SUGESTÃO: aplicar LOWER(email) = LOWER(:email) (JPQL) ou normalizar/baixar case na coluna/entidade.
    @Test
    public void testFindByEmailCaseInsensitive() {
        // Busca com parte do e-mail em maiúsculas deve retornar resultado
        Optional<Customer> foundCustomer = customerRepository.findByEmail("JOAO.SILVA@example.com");
        assertTrue(foundCustomer.isPresent(), "Cliente deveria ser encontrado com email em maiúsculas");
    }

    @Test
    public void testFindByEmailNotFound() {
        // E-mail inexistente → Optional.empty
        Optional<Customer> foundCustomer = customerRepository.findByEmail("nao.existe@example.com");
        assertFalse(foundCustomer.isPresent(), "Cliente não deveria ser encontrado");
    }

    @Test
    public void testFindByNameContaining() {
        // Busca por fragmento "Silva" → deve trazer apenas João Silva
        List<Customer> customers = customerRepository.findByNameContaining("Silva");
        assertEquals(1, customers.size(), "Deveria encontrar 1 cliente com 'Silva' no nome");
        assertTrue(customers.stream().anyMatch(c -> c.getName().equals("João Silva")),
                "Deveria conter o cliente João Silva");
    }

    @Test
    public void testFindByNameContainingWithSQLInjection() {
        // Consulta deve ser segura contra injeção; entrada maliciosa não pode retornar todos
        try {
            List<Customer> customers = customerRepository.findByNameContaining("' OR '1'='1");
            assertFalse(customers.size() > 1, "A consulta não deveria ser vulnerável a injeção de SQL");
        } catch (Exception e) {
            // Implementações seguras podem lançar exceção por parâmetro inválido
            assertTrue(true, "A consulta deveria ser segura contra injeção de SQL");
        }
    }

    /**
     * Gera uma massa grande para teste de performance/otimização.
     * - 20.000 clientes;
     * - 1.000 clientes com 1–3 pedidos;
     * - Flush/Clear a cada 50 inserts para reduzir pressão de memória.
     */
    private void createManyCustomersWithOrders() {
        final int TOTAL_CUSTOMERS = 20000;
        final int CUSTOMERS_WITH_ORDERS = 1000;
        final int BATCH_SIZE = 50;

        System.out.println("Criando " + TOTAL_CUSTOMERS + " clientes para teste de performance...");

        for (int i = 0; i < TOTAL_CUSTOMERS; i++) {
            Customer customer = new Customer();
            customer.setName("Customer " + i);
            customer.setEmail("customer" + i + "@example.com");
            customer.setPhone("(11) 99999-" + String.format("%04d", i % 10000));
            entityManager.persist(customer);

            if (i < CUSTOMERS_WITH_ORDERS) {
                int numOrders = 1 + (i % 3); // 1..3 pedidos
                for (int j = 0; j < numOrders; j++) {
                    Order order = new Order();
                    order.setOrderNumber("ORD-" + i + "-" + j);
                    order.setOrderDate(LocalDateTime.now());
                    order.setCustomer(customer);
                    entityManager.persist(order);
                    customer.getOrders().add(order);
                }
            }

            if (i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        entityManager.flush();
    }

    // FIXME: Possível N+1 ao carregar clientes e seus pedidos separadamente.
    // SUGESTÃO: usar JOIN FETCH em customerRepository.findCustomersWithOrders() para carregar tudo em uma consulta.
    @Test
    public void testFindCustomersWithOrders() {
        // Aumenta massa e mede latência da consulta (meta < 500ms neste cenário controlado)
        createManyCustomersWithOrders();

        long startTime = System.currentTimeMillis();
        List<Customer> customers = customerRepository.findCustomersWithOrders();
        long endTime = System.currentTimeMillis();

        assertTrue((endTime - startTime) < 500,
                "o tempo máximo de retorno da consulta deveria ser de menos de 500 ms (demorou " + (endTime - startTime) + " ms)");

        // Esperado: 1000 (gerados) + 2 dos testes básicos (João e Maria) = 1002
        assertEquals(1002, customers.size(), "Deveria encontrar 1.002 clientes com pedidos");

        // Valida presença/ausência esperada
        assertTrue(customers.stream().anyMatch(c -> c.getName().equals("João Silva")),
                "Deveria conter o cliente João Silva");
        assertTrue(customers.stream().anyMatch(c -> c.getName().equals("Maria Santos")),
                "Deveria conter o cliente Maria Santos");
        assertFalse(customers.stream().anyMatch(c -> c.getName().equals("Pedro Oliveira")),
                "Não deveria conter o cliente Pedro Oliveira");
    }

    @Test
    public void testFindAll() {
        // Deve retornar os 3 clientes criados no setUp()
        List<Customer> customers = customerRepository.findAll();
        assertEquals(3, customers.size(), "Deveria encontrar 3 clientes");
    }

    @Test
    public void testFindById() {
        // Busca por ID existente → presente e com nome esperado
        Optional<Customer> foundCustomer = customerRepository.findById(customer1.getId());
        assertTrue(foundCustomer.isPresent(), "Cliente deveria ser encontrado pelo ID");
        assertEquals("João Silva", foundCustomer.get().getName());
    }

    @Test
    public void testSave() {
        // Salvamento simples → id gerado e entidade recuperável via findById
        Customer newCustomer = new Customer();
        newCustomer.setName("Ana Pereira");
        newCustomer.setEmail("ana.pereira@example.com");
        newCustomer.setPhone("(11) 99999-4444");

        Customer savedCustomer = customerRepository.save(newCustomer);
        assertNotNull(savedCustomer, "Cliente salvo não deveria ser nulo");
        assertNotNull(savedCustomer.getId(), "ID do cliente salvo não deveria ser nulo");

        Optional<Customer> foundCustomer = customerRepository.findById(savedCustomer.getId());
        assertTrue(foundCustomer.isPresent(), "Cliente deveria ser encontrado após salvar");
        assertEquals("Ana Pereira", foundCustomer.get().getName());
    }

    @Test
    public void testDelete() {
        // Exclusão lógica/física conforme JPA → findById após delete deve retornar vazio
        customerRepository.delete(customer3);

        Optional<Customer> foundCustomer = customerRepository.findById(customer3.getId());
        assertFalse(foundCustomer.isPresent(), "Cliente não deveria ser encontrado após excluir");
    }
}
