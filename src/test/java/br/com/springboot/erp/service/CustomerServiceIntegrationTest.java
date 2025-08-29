package br.com.springboot.erp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

import br.com.springboot.erp.Application;
import br.com.springboot.erp.config.TestConfig;
import br.com.springboot.erp.model.entity.Customer;
import br.com.springboot.erp.model.entity.Order;
import br.com.springboot.erp.service.CustomerService;

/**
 * Testes de integração para {@link CustomerService}.
 *
 * 🎯 Objetivo
 *  - Validar regras de negócio implementadas no serviço de clientes com JPA/Hibernate e banco H2 em memória.
 *  - Cobrir operações CRUD, validações e consultas customizadas (por e-mail, por nome, com pedidos, etc).
 *
 * 🧪 Contexto
 *  - Runner: {@link SpringRunner}
 *  - App: {@link Application}
 *  - Config: {@link TestConfig} (DataSource H2, JPA/Hibernate, transações)
 *  - Perfil: {@code test}
 *  - Transações: {@link Transactional} garante rollback automático e isolamento por teste
 *
 * 🔍 Cobertura
 *  - saveCustomer (válido e inválido)
 *  - findCustomerById / findCustomerByEmail
 *  - findAllCustomers
 *  - searchCustomersByName (robustez contra SQL Injection)
 *  - updateCustomer
 *  - deleteCustomer (com/sem pedidos)
 *  - findCustomersWithOrders
 *  - validateCustomerEmail (vários cenários)
 *
 * ⚠️ Observações e melhorias sugeridas
 *  - Validação de e-mail: usar `@Email` + `@NotBlank` no modelo, ou Bean Validation customizada no service.
 *  - SQL Injection: consultas devem sempre ser parametrizadas (JPQL/Criteria API).
 *  - Regras de exclusão: impedir deleção de clientes com pedidos (retornar 400/409), em vez de permitir.
 *  - Padrão de regex para e-mail: ajustar para validar TLD e domínios corretamente (`^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$`).
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class CustomerServiceIntegrationTest {

    //@Autowired
    //private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @PersistenceContext
    private EntityManager entityManager;

    private Customer customer1;
    private Customer customer2;
    private Customer customer3;

    @Before
    public void setUp() {
        // Limpa quaisquer dados existentes
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        entityManager.createQuery("DELETE FROM Customer").executeUpdate();

        // Cria clientes para testes
        customer1 = new Customer();
        customer1.setName("João Silva");
        customer1.setEmail("joao.silva@example.com");
        customer1.setPhone("(11) 99999-1111");
        customer1.setOrders(new ArrayList<>());
        entityManager.persist(customer1);

        customer2 = new Customer();
        customer2.setName("Maria Santos");
        customer2.setEmail("maria.santos@example.com");
        customer2.setPhone("(11) 99999-2222");
        customer2.setOrders(new ArrayList<>());
        entityManager.persist(customer2);

        // Adiciona pedidos ao customer2
        Order order = new Order();
        order.setOrderNumber("ORD-001");
        order.setCustomer(customer2);
        entityManager.persist(order);

        List<Order> orders = new ArrayList<>();
        orders.add(order);
        customer2.setOrders(orders);
        entityManager.persist(customer2);

        customer3 = new Customer();
        customer3.setName("Pedro Oliveira");
        customer3.setEmail("pedro.oliveira@example.com");
        customer3.setPhone("(11) 99999-3333");
        customer3.setOrders(new ArrayList<>());
        entityManager.persist(customer3);

        // Garante que as alterações sejam persistidas
        entityManager.flush();
    }

    @Test
    public void testSaveCustomer() {
        // Cria um novo cliente
        Customer newCustomer = new Customer();
        newCustomer.setName("Novo Cliente");
        newCustomer.setEmail("novo.cliente@example.com");
        newCustomer.setPhone("(11) 99999-4444");
        newCustomer.setOrders(new ArrayList<>());

        // Executa o método
        Customer savedCustomer = customerService.saveCustomer(newCustomer);

        // Verifica o resultado
        assertNotNull("Cliente salvo não deveria ser nulo", savedCustomer);
        assertNotNull("Cliente salvo deveria ter um ID", savedCustomer.getId());
        assertEquals("Cliente salvo deveria ter o nome correto", "Novo Cliente", savedCustomer.getName());
        assertEquals("Cliente salvo deveria ter o e-mail correto", "novo.cliente@example.com", savedCustomer.getEmail());

        // Verifica se o cliente foi realmente salvo no banco de dados
        Optional<Customer> foundCustomer = customerService.findCustomerById(savedCustomer.getId());
        assertTrue("Cliente deveria ser encontrado no banco de dados", foundCustomer.isPresent());
    }

    @Test
    public void testSaveCustomerWithInvalidEmail() {
        // Cria um cliente com e-mail inválido
        Customer invalidCustomer = new Customer();
        invalidCustomer.setName("Cliente Inválido");
        invalidCustomer.setEmail("email-invalido");
        invalidCustomer.setPhone("(11) 99999-5555");
        invalidCustomer.setOrders(new ArrayList<>());

        try {
            // Executa o método - deveria falhar para emails inválidos
            Customer savedCustomer = customerService.saveCustomer(invalidCustomer);

            // Valida que o retorno não é válido
            assertNull("Cliente com e-mail inválido não deveria ser salvo", savedCustomer);
            // ou, caso retorne objeto mas não persista:
            assertNull("Cliente inválido não deveria receber ID", 
                       savedCustomer != null ? savedCustomer.getId() : null);
        } catch (Exception e) {
            // Esperamos uma exceção quando tentamos salvar um cliente com email inválido
            assertTrue("Deveria lançar exceção ao salvar cliente com email inválido", true);
        }
    }

    @Test
    public void testFindCustomerById() {
        // Executa o método
        Optional<Customer> foundCustomer = customerService.findCustomerById(customer1.getId());

        // Verifica o resultado
        assertTrue("Cliente deveria ser encontrado", foundCustomer.isPresent());
        assertEquals("Cliente encontrado deveria ter o ID correto", customer1.getId(), foundCustomer.get().getId());
        assertEquals("Cliente encontrado deveria ter o nome correto", "João Silva", foundCustomer.get().getName());
    }

    @Test
    public void testFindCustomerByEmail() {
        // Executa o método
        Optional<Customer> foundCustomer = customerService.findCustomerByEmail("joao.silva@example.com");

        // Verifica o resultado
        assertTrue("Cliente deveria ser encontrado", foundCustomer.isPresent());
        assertEquals("Cliente encontrado deveria ter o e-mail correto", "joao.silva@example.com", foundCustomer.get().getEmail());
        assertEquals("Cliente encontrado deveria ter o nome correto", "João Silva", foundCustomer.get().getName());
    }

    @Test
    public void testFindAllCustomers() {
        // Executa o método
        List<Customer> customers = customerService.findAllCustomers();

        // Verifica o resultado
        assertEquals("Deveria encontrar 3 clientes", 3, customers.size());
    }

    @Test
    public void testSearchCustomersByName() {
        // Executa o método
        List<Customer> customers = customerService.searchCustomersByName("Silva");

        // Verifica o resultado
        assertEquals("Deveria encontrar 1 cliente", 1, customers.size());
        assertEquals("Cliente encontrado deveria ser João Silva", "João Silva", customers.get(0).getName());
    }

    @Test
    public void testSearchCustomersByNameWithSQLInjection() {
        // Executa o método com uma tentativa de injeção SQL
        try {
            List<Customer> customers = customerService.searchCustomersByName("' OR '1'='1");

            // Se a consulta for vulnerável, retornará todos os clientes
            // Verificamos que a consulta não deve retornar todos os clientes (deve falhar)
            assertTrue("A consulta não deveria ser vulnerável a injeção de SQL", customers.size() == 0);
        } catch (Exception e) {
            // Se ocorrer uma exceção, capturamos e fazemos o teste falhar com uma mensagem clara
            // em vez de deixar a exceção se propagar
            fail("O teste deveria falhar com uma asserção, não com uma exceção: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateCustomer() {
        // Modifica o cliente
        customer1.setName("João Silva Atualizado");
        customer1.setEmail("joao.atualizado@example.com");

        // Executa o método
        customerService.updateCustomer(customer1);

        // Verifica se o cliente foi atualizado no banco de dados
        Optional<Customer> updatedCustomer = customerService.findCustomerById(customer1.getId());
        assertTrue("Cliente deveria ser encontrado", updatedCustomer.isPresent());
        assertEquals("Nome do cliente deveria ser atualizado", "João Silva Atualizado", updatedCustomer.get().getName());
        assertEquals("E-mail do cliente deveria ser atualizado", "joao.atualizado@example.com", updatedCustomer.get().getEmail());
    }

    @Test
    public void testDeleteCustomer() {
        // Executa o método
        customerService.deleteCustomer(customer1.getId());

        // Verifica se o cliente foi removido do banco de dados
        Optional<Customer> deletedCustomer = customerService.findCustomerById(customer1.getId());
        assertFalse("Cliente deveria ser removido", deletedCustomer.isPresent());
    }

    @Test
    public void testDeleteCustomerWithOrders() {
        // Executa o método
        customerService.deleteCustomer(customer2.getId());

        // Verifica se o cliente foi removido do banco de dados
        Optional<Customer> deletedCustomer = customerService.findCustomerById(customer2.getId());
        assertFalse("Cliente com pedidos deveria ser removido", deletedCustomer.isPresent());
    }

    @Test
    public void testFindCustomersWithOrders() {
        // Executa o método
        List<Customer> customers = customerService.findCustomersWithOrders();

        // Verifica o resultado
        assertEquals("Deveria encontrar 1 cliente com pedidos", 1, customers.size());
        assertEquals("Cliente encontrado deveria ser Maria Santos", "Maria Santos", customers.get(0).getName());
    }

    @Test
    public void testValidateCustomerEmail() {
        // Testa e-mail válido
        boolean isValid = customerService.validateCustomerEmail("joao.silva@example.com");
        assertTrue("E-mail deveria ser considerado válido", isValid);

        // Testa e-mail inválido
        isValid = customerService.validateCustomerEmail("email-invalido");
        assertFalse("E-mail deveria ser considerado inválido", isValid);

        // Testa e-mail nulo
        isValid = customerService.validateCustomerEmail(null);
        assertFalse("E-mail nulo deveria ser considerado inválido", isValid);

        // Testa e-mail vazio
        isValid = customerService.validateCustomerEmail("");
        assertFalse("E-mail vazio deveria ser considerado inválido", isValid);
    }

	// FIXME: Erro na formtação do PATTERN do E-mail
	// SUGESTÃO: Corrigir o pattern para o padrão válido de e-mail. 	    
    @Test
    public void testValidateCustomerEmailWithSimplePattern() {
        // Testa e-mail com formato válido mas domínio inválido
        boolean isValid = customerService.validateCustomerEmail("joao.silva@dominio-invalido");
        assertFalse("E-mail com domínio inválido não deveria ser aceito", isValid);

        // Testa e-mail com formato válido mas sem TLD
        isValid = customerService.validateCustomerEmail("joao.silva@dominio");
        assertFalse("E-mail sem TLD não deveria ser aceito", isValid);
    }
}
