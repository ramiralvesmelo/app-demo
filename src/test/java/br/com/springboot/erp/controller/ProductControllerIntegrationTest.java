package br.com.springboot.erp.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

import br.com.springboot.erp.Application;
import br.com.springboot.erp.config.TestConfig;
import br.com.springboot.erp.model.entity.Product;
import br.com.springboot.erp.service.ProductService;

/**
 * Testes de integração do ProductController.
 *
 * Objetivo:
 *  - Exercitar os endpoints REST de produtos ponta a ponta (MockMvc),
 *    validando mapeamentos, serialização JSON e regras básicas de negócio.
 *
 * Contexto de teste:
 *  - Perfil ativo: {@code test}.
 *  - Banco: H2 em memória configurado por {@link TestConfig}.
 *  - Transações: classe anotada com {@link Transactional}, garantindo isolamento/rollback.
 *
 * Massa inicial (definida em {@link #setUp()}):
 *  - product1: "Produto 1", preço 10.00, estoque 100, SKU001
 *  - product2: "Produto 2", preço 20.00, estoque 5,   SKU002
 *
 * Cobertura dos endpoints:
 *  - GET    /api/products                         → listagem
 *  - GET    /api/products/{id}                    → detalhe por ID
 *  - GET    /api/products/sku/{sku}               → busca por SKU
 *  - POST   /api/products                         → criação (validação de campos obrigatórios)
 *  - PUT    /api/products/{id}                    → atualização (nome/preço/etc.)
 *  - DELETE /api/products/{id}                    → exclusão
 *  - PUT    /api/products/{id}/stock?stock=x      → atualização de estoque
 *  - PUT    /api/products/{id}/price?price=x      → atualização de preço
 *  - GET    /api/products/inventory-value         → valor total do inventário
 *  - GET    /api/products/price-range?min&max     → filtro por faixa de preço
 *
 * Observações (melhorias sugeridas ao domínio/validações):
 *  - ❗ Validação de campos: anotar {@code name} com {@code @NotBlank}, {@code price} com
 *    {@code @NotNull @PositiveOrZero}, {@code stock} com {@code @NotNull @PositiveOrZero},
 *    {@code sku} com {@code @NotBlank} e regra de unicidade (constraint + validação).
 *  - ❗ Respostas HTTP consistentes: usar 400/422 para payload inválido; 404 para não encontrado;
 *    409 para conflitos (ex.: violação de unicidade).
 *  - ❗ Conversão numérica: validar parâmetros de query/forma (ex.: price/stock) com @Validated/@RequestParam.
 *  - ❗ Precisão monetária: garantir uso de {@link BigDecimal} com scale adequado no JSON de resposta.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class ProductControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductService productService;

    @PersistenceContext
    private EntityManager entityManager;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private Product product1;
    private Product product2;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Limpeza das tabelas relevantes para isolamento da massa de dados
        entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        entityManager.createQuery("DELETE FROM Product").executeUpdate();

        // Produto base 1
        product1 = new Product();
        product1.setName("Produto 1");
        product1.setDescription("Descrição do Produto 1");
        product1.setPrice(new BigDecimal("10.00"));
        product1.setStock(100);
        product1.setSku("SKU001");
        entityManager.persist(product1);

        // Produto base 2
        product2 = new Product();
        product2.setName("Produto 2");
        product2.setDescription("Descrição do Produto 2");
        product2.setPrice(new BigDecimal("20.00"));
        product2.setStock(5);
        product2.setSku("SKU002");
        entityManager.persist(product2);

        // Garante flush da massa inicial
        entityManager.flush();
    }

    @Test
    public void testGetAllProducts() throws Exception {
        // Deve retornar 2 produtos (ordem de inserção) com JSON válido
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Produto 1")))
                .andExpect(jsonPath("$[1].name", is("Produto 2")));
    }

    @Test
    public void testGetProductById() throws Exception {
        // Detalhe por ID existente
        mockMvc.perform(get("/api/products/" + product1.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Produto 1")))
                .andExpect(jsonPath("$.sku", is("SKU001")));
    }

    @Test
    public void testGetProductByIdNotFound() throws Exception {
        // ID inexistente → 404
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetProductBySku() throws Exception {
        // Busca por SKU existente
        mockMvc.perform(get("/api/products/sku/SKU001"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Produto 1")))
                .andExpect(jsonPath("$.sku", is("SKU001")));
    }

    @Test
    public void testGetProductByInvalidSku() throws Exception {
        // SKU inexistente/curto → 404
        mockMvc.perform(get("/api/products/sku/a"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testCreateProduct() throws Exception {
        // Criação com payload válido → 201 + JSON do recurso criado
        Product newProduct = new Product();
        newProduct.setName("Novo Produto");
        newProduct.setDescription("Descrição do Novo Produto");
        newProduct.setPrice(new BigDecimal("30.00"));
        newProduct.setStock(50);
        newProduct.setSku("SKU003");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Novo Produto")))
                .andExpect(jsonPath("$.sku", is("SKU003")));
    }

    @Test
    public void testCreateProductWithoutRequiredFields() throws Exception {
        // Nome ausente (obrigatório) → esperado 400
        Product invalidProduct = new Product();
        invalidProduct.setDescription("Produto sem nome");
        invalidProduct.setPrice(new BigDecimal("40.00"));
        invalidProduct.setStock(20);
        invalidProduct.setSku("SKU004");

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateProduct() throws Exception {
        // Atualização de nome e preço → 200 com retorno atualizado
        product1.setName("Produto 1 Atualizado");
        product1.setPrice(new BigDecimal("15.00"));

        mockMvc.perform(put("/api/products/" + product1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.name", is("Produto 1 Atualizado")))
                .andExpect(jsonPath("$.price", is(15.00)));
    }

    @Test
    public void testDeleteProduct() throws Exception {
        // Exclusão por ID existente → 204
        mockMvc.perform(delete("/api/products/" + product1.getId()))
                .andExpect(status().isNoContent());

        Optional<Product> deletedProduct = productService.findProductById(product1.getId());
        assertFalse("Produto deveria ser removido", deletedProduct.isPresent());
    }

    @Test
    public void testDeleteNonExistentProduct() throws Exception {
        // Exclusão de ID inexistente → 404
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateStock() throws Exception {
        // Atualização de estoque via query param → 200 e valor persistido
        mockMvc.perform(put("/api/products/" + product1.getId() + "/stock")
                .param("stock", "200"))
                .andExpect(status().isOk());

        Optional<Product> updatedProduct = productService.findProductById(product1.getId());
        assertTrue("Produto deveria ser encontrado", updatedProduct.isPresent());
        assertEquals("Estoque deveria ser atualizado", Integer.valueOf(200), updatedProduct.get().getStock());
    }

    @Test
    public void testUpdateStockWithNegativeValue() throws Exception {
        // Estoque negativo → 400 e valor original preservado
        mockMvc.perform(put("/api/products/" + product1.getId() + "/stock")
                .param("stock", "-50"))
                .andExpect(status().isBadRequest());

        Optional<Product> updatedProduct = productService.findProductById(product1.getId());
        assertNotEquals("Estoque não deveria ser atualizado com valor negativo", Integer.valueOf(-50), updatedProduct.get().getStock());
    }

    // FIXME: Possível violação de chave única ao gerar orderNumber apenas com timestamp.
    // SUGESTÃO: incluir sufixo aleatório/sequence (ex.: timestamp + UUID curto) para garantir unicidade em concorrência.

    // FIXME: Em cenários de leitura logo após update, inconsistências podem ocorrer se o serviço/repositório
    // usar um EntityManager diferente. SUGESTÃO: manter @Transactional padrão (propagation=REQUIRED) e,
    // se necessário, forçar flush/clear no service para visibilidade imediata.
    @Test
    public void testUpdatePrice() throws Exception {
        // Atualização de preço via query param → 200 e valor persistido
        mockMvc.perform(put("/api/products/" + product1.getId() + "/price")
                .param("price", "25.00"))
                .andExpect(status().isOk());

        Optional<Product> updatedProduct = productService.findProductById(product1.getId());
        assertTrue("Produto deveria ser encontrado", updatedProduct.isPresent());
        assertEquals("Preço deveria ser atualizado", new BigDecimal("25.00"), updatedProduct.get().getPrice());
    }

    @Test
    public void testUpdatePriceWithNegativeValue() throws Exception {
        // Preço negativo → 400 e valor original preservado
        mockMvc.perform(put("/api/products/" + product1.getId() + "/price")
                .param("price", "-10.00"))
                .andExpect(status().isBadRequest());

        Optional<Product> updatedProduct = productService.findProductById(product1.getId());
        assertNotEquals("Preço não deveria ser atualizado com valor negativo", new BigDecimal("-10.00"), updatedProduct.get().getPrice());
    }

    @Test
    public void testGetInventoryValue() throws Exception {
        // Valor total do inventário (soma price*stock) → 200 + JSON
        mockMvc.perform(get("/api/products/inventory-value"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void testGetProductsByPriceRange() throws Exception {
        // Filtragem por faixa 5.00–25.00 → espera 2 produtos (10.00 e 20.00)
        mockMvc.perform(get("/api/products/price-range")
                .param("minPrice", "5.00")
                .param("maxPrice", "25.00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetProductsByPriceRangeWithDefaultValues() throws Exception {
        // Sem parâmetros → usa defaults definidos no controller (se existirem)
        mockMvc.perform(get("/api/products/price-range"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void testGetProductsByPriceRangeWithInvalidRange() throws Exception {
        // min > max → 400 Bad Request
        mockMvc.perform(get("/api/products/price-range")
                .param("minPrice", "30.00")
                .param("maxPrice", "20.00"))
                .andExpect(status().isBadRequest());
    }
}
