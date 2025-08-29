package br.com.springboot.erp.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import br.com.springboot.erp.controller.ProductController;
import br.com.springboot.erp.model.entity.Product;
import br.com.springboot.erp.service.ProductService;

/**
 * Testes de unidade (slice de web) para o {@link ProductController}, isolando a camada web.
 *
 * 💡 Abordagem:
 *  - Usa {@link MockitoJUnitRunner} para mockar {@link ProductService}.
 *  - Constrói um {@link MockMvc} via {@link MockMvcBuilders#standaloneSetup(Object...)} sem subir o contexto do Spring.
 *  - Verifica mapeamentos, status HTTP, content-type e estrutura JSON, além de interações com o serviço.
 *
 * Escopo deste teste:
 *  - GET /api/products
 *  - GET /api/products/low-stock
 *  - GET /api/products/inventory-value
 *  - GET /api/products/price-range?minPrice&maxPrice
 *
 * O que NÃO é coberto aqui:
 *  - Integração com banco/JPA (cobrir em testes de integração).
 *  - Validações Bean Validation (ex.: @NotBlank, @Positive) aplicadas por conversores/validators do Spring MVC
 *    — em {@code standaloneSetup} você só as testa se registrar explicitamente os validadores/converters.
 *
 * Sugestões de melhoria (sem alterar o código):
 *  - Registrar um {@code @ControllerAdvice} de tratamento de erros no {@code standaloneSetup}, caso o controller use.
 *  - Adicionar conversores Jackson/UTF-8 explicitamente (ex.: MappingJackson2HttpMessageConverter), se necessário.
 *  - Em projetos com JUnit 5, migrar para {@code @WebMvcTest(ProductController.class)} + {@code @ExtendWith}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    //private final ObjectMapper objectMapper = new ObjectMapper();

    private Product product1;
    private Product product2;

    @Before
    public void setUp() {
        // Sobe apenas o controller sob teste, sem contexto completo do Spring.
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                // .setControllerAdvice(new GlobalExceptionHandler()) // (opcional) se existir
                // .setMessageConverters(new MappingJackson2HttpMessageConverter()) // (opcional) se precisar forçar JSON
                .build();

        // Massa de teste básica (sem persistência)
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
    }

    @Test
    public void testGetAllProducts() throws Exception {
        // Arrange
        when(productService.findAllProducts()).thenReturn(Arrays.asList(product1, product2));

        // Act & Assert
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Produto 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Produto 2")));

        // Verifica interação com o serviço (1 chamada)
        verify(productService, times(1)).findAllProducts();
    }

    @Test
    public void testGetProductsWithLowStock() throws Exception {
        // Arrange
        when(productService.findProductsWithLowStock()).thenReturn(Arrays.asList(product2));

        // Act & Assert
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].name", is("Produto 2")));

        verify(productService, times(1)).findProductsWithLowStock();
    }

    @Test
    public void testGetInventoryValue() throws Exception {
        // Arrange
        when(productService.calculateInventoryValue()).thenReturn(new BigDecimal("1100.00"));

        // Act & Assert
        mockMvc.perform(get("/api/products/inventory-value"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                // Observação: jsonPath numérico com BigDecimal é serializado como número JSON.
                .andExpect(jsonPath("$", is(1100.00)));

        verify(productService, times(1)).calculateInventoryValue();
    }

    @Test
    public void testGetProductsByPriceRange() throws Exception {
        // Arrange
        when(productService.findProductsByPriceRange(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(Arrays.asList(product1, product2));

        // Act & Assert
        mockMvc.perform(get("/api/products/price-range")
                        .param("minPrice", "5.00")
                        .param("maxPrice", "25.00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Produto 1")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Produto 2")));

        // Confirma que o service foi chamado com os valores esperados
        verify(productService, times(1))
                .findProductsByPriceRange(new BigDecimal("5.00"), new BigDecimal("25.00"));
    }
}
