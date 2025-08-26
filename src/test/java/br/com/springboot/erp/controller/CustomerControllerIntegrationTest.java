package br.com.springboot.erp.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.springboot.erp.TesteApplication;
import br.com.springboot.erp.config.TestConfig;
import br.com.springboot.erp.model.Customer;
import br.com.springboot.erp.model.Order;
import br.com.springboot.erp.service.CustomerService;

/**
 * Testes de integração do CustomerController.
 *
 * Objetivo:
 *  - Exercitar a camada web (MockMvc) ponta a ponta com o contexto do Spring,
 *    validando mapeamentos, serialização JSON e regras básicas de negócio.
 *
 * Contexto de teste:
 *  - Perfil ativo: {@code test}.
 *  - Banco H2 em memória configurado por {@link TestConfig}.
 *  - Transacional: cada teste roda isolado e efetua rollback ao final.
 *
 * Massa de dados inicial (criada em {@link #setUp()}):
 *  - 3 clientes: João Silva (sem pedidos), Maria Santos (1 pedido), Pedro Oliveira (sem pedidos).
 *
 * Cobertura principal:
 *  - GET /api/customers                → listagem geral
 *  - GET /api/customers/{id}           → detalhe por ID
 *  - GET /api/customers/email/{email}  → busca por e-mail
 *  - GET /api/customers/search?name=x  → busca por nome (atenção a injeção)
 *  - POST /api/customers               → criação (validação de e-mail)
 *  - PUT /api/customers/{id}           → atualização
 *  - DELETE /api/customers/{id}        → exclusão (comportamento quando há pedidos)
 *  - POST /api/customers/validate-email?email=x → validação de e-mail
 *
 * Observações e melhorias indicadas nos comentários:
 *  - ❗ SQL Injection: reforçar a implementação do repositório com parâmetros nomeados/Criteria API.
 *  - ❗ Validação: anotar {@code Customer.email} com {@code @Email} e {@code @NotBlank}.
 *  - ❗ Regras de negócio: impedir exclusão de cliente com pedidos (retornar 400/409).
 *  - ❗ Serialização cíclica: usar {@code @JsonManagedReference}/{@code @JsonBackReference} em Customer/Order.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TesteApplication.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class CustomerControllerIntegrationTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private CustomerService customerService;

	@PersistenceContext
	private EntityManager entityManager;

	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private Customer customer1;
	private Customer customer2;
	private Customer customer3;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		// Limpa as tabelas relevantes para garantir isolamento da massa de dados
		entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
		entityManager.createQuery("DELETE FROM Order").executeUpdate();
		entityManager.createQuery("DELETE FROM Customer").executeUpdate();

		// Cliente 1: sem pedidos
		customer1 = new Customer();
		customer1.setName("João Silva");
		customer1.setEmail("joao.silva@example.com");
		customer1.setPhone("(11) 99999-1111");
		customer1.setOrders(new ArrayList<>());
		entityManager.persist(customer1);

		// Cliente 2: com 1 pedido
		customer2 = new Customer();
		customer2.setName("Maria Santos");
		customer2.setEmail("maria.santos@example.com");
		customer2.setPhone("(11) 99999-2222");
		customer2.setOrders(new ArrayList<>());
		entityManager.persist(customer2);

		Order order = new Order();
		order.setOrderNumber("ORD-001");
		order.setCustomer(customer2);
		entityManager.persist(order);

		List<Order> orders = new ArrayList<>();
		orders.add(order);
		customer2.setOrders(orders);
		entityManager.persist(customer2);

		// Cliente 3: sem pedidos
		customer3 = new Customer();
		customer3.setName("Pedro Oliveira");
		customer3.setEmail("pedro.oliveira@example.com");
		customer3.setPhone("(11) 99999-3333");
		customer3.setOrders(new ArrayList<>());
		entityManager.persist(customer3);

		// Garante flush da massa inicial antes dos testes
		entityManager.flush();
	}

	@Test
	public void testGetAllCustomers() throws Exception {
		// Deve retornar 3 clientes na ordem inserida (verifica nome e tamanho)
		mockMvc.perform(get("/api/customers"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$", hasSize(3)))
				.andExpect(jsonPath("$[0].name", is("João Silva")))
				.andExpect(jsonPath("$[1].name", is("Maria Santos")))
				.andExpect(jsonPath("$[2].name", is("Pedro Oliveira")));
	}

	@Test
	public void testGetCustomerById() throws Exception {
		// Deve retornar o cliente correspondente ao ID informado
		mockMvc.perform(get("/api/customers/" + customer1.getId()))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.name", is("João Silva")))
				.andExpect(jsonPath("$.email", is("joao.silva@example.com")));
	}

	@Test
	public void testGetCustomerByEmail() throws Exception {
		// Deve localizar por e-mail válido existente
		mockMvc.perform(get("/api/customers/email/joao.silva@example.com"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.name", is("João Silva")))
				.andExpect(jsonPath("$.email", is("joao.silva@example.com")));
	}

	@Test
	public void testGetCustomerByInvalidEmail() throws Exception {
		// Deve retornar 404 para e-mail inexistente ou malformado na rota
		mockMvc.perform(get("/api/customers/email/email-invalido"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testSearchCustomersByName() throws Exception {
		// Busca por fragmento do nome; espera 1 ocorrência (João Silva)
		mockMvc.perform(get("/api/customers/search").param("name", "Silva"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is("João Silva")));
	}

	// ❗FIXME (Segurança): a implementação atual do findByNameContaining aceita parâmetros inseguros.
	// Sugestão: usar parâmetros nomeados (JPQL) ou Criteria API para evitar SQL Injection.
	@Test
	public void testSearchCustomersByNameWithSQLInjection() throws Exception {
		// Entrada maliciosa não deve retornar resultados
		mockMvc.perform(get("/api/customers/search").param("name", "' OR '1'='1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	public void testCreateCustomer() throws Exception {
		// Criação com dados válidos → 201 + payload do recurso criado
		Customer newCustomer = new Customer();
		newCustomer.setName("Novo Cliente");
		newCustomer.setEmail("novo.cliente@example.com");
		newCustomer.setPhone("(11) 99999-4444");

		mockMvc.perform(post("/api/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(newCustomer)))
				.andExpect(status().isCreated())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.name", is("Novo Cliente")))
				.andExpect(jsonPath("$.email", is("novo.cliente@example.com")));
	}

	// ❗FIXME (Validação): anotar Customer.email com @Email e @NotBlank.
	// Esperado: 400 Bad Request quando o e-mail for inválido.
	@Test
	public void testCreateCustomerWithInvalidEmail() throws Exception {
		Customer invalidCustomer = new Customer();
		invalidCustomer.setName("Cliente Inválido");
		invalidCustomer.setEmail("email-invalido");
		invalidCustomer.setPhone("(11) 99999-5555");

		mockMvc.perform(post("/api/customers")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidCustomer)))
				.andExpect(status().isBadRequest());
	}

	// ❗FIXME (Consistência): garantir que BaseRepositoryImpl/serviço apliquem validações.
	@Test
	public void testUpdateCustomer() throws Exception {
		// Atualização de nome e e-mail → 200 + retorno atualizado
		customer1.setName("João Silva Atualizado");
		customer1.setEmail("joao.atualizado@example.com");

		mockMvc.perform(put("/api/customers/" + customer1.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(customer1)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.name", is("João Silva Atualizado")))
				.andExpect(jsonPath("$.email", is("joao.atualizado@example.com")));
	}

	@Test
	public void testDeleteCustomer() throws Exception {
		// Exclusão de cliente sem pedidos → 204 No Content
		mockMvc.perform(delete("/api/customers/" + customer1.getId()))
				.andExpect(status().isNoContent());

		Optional<Customer> deletedCustomer = customerService.findCustomerById(customer1.getId());
		assertFalse("Cliente deveria ser removido", deletedCustomer.isPresent());
	}

	@Test
	public void testDeleteCustomerWithOrders() throws Exception {
		// ❗Comportamento desejado: impedir exclusão quando houver pedidos (400/409).
		// Implementação atual permite e retorna 204; o teste documenta a lacuna.
		mockMvc.perform(delete("/api/customers/" + customer2.getId()))
				.andExpect(status().isNoContent());

		Optional<Customer> deletedCustomer = customerService.findCustomerById(customer2.getId());
		assertFalse("Cliente com pedidos não deveria ser removido (ajustar regra de negócio)", deletedCustomer.isPresent());
	}

	// ❗FIXME (Serialização): evitar recursão entre Customer e Order.
	// Sugestão: usar @JsonManagedReference/@JsonBackReference ou @JsonIgnore em um dos lados.
	@Test
	public void testGetCustomersWithOrders() throws Exception {
		mockMvc.perform(get("/api/customers/with-orders"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is("Maria Santos")));
	}

	// ❗FIXME (Status HTTP): para e-mail inválido, retornar 400 com body "false"; para válido, 200 "true".
	@Test
	public void testValidateEmail() throws Exception {
		// E-mail válido
		mockMvc.perform(post("/api/customers/validate-email").param("email", "joao.silva@example.com"))
				.andExpect(status().isOk())
				.andExpect(content().string("true"));

		// E-mails inválidos
		mockMvc.perform(post("/api/customers/validate-email").param("email", "oemaildeveserinvalido"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("false"));

		mockMvc.perform(post("/api/customers/validate-email").param("email", "email-invalido"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("false"));
	}
}
