package br.com.springboot.erp.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import br.com.springboot.erp.config.GlobalExceptionHandler;
import br.com.springboot.erp.model.entity.Order;
import br.com.springboot.erp.model.entity.OrderItem;
import br.com.springboot.erp.service.CustomerService;
import br.com.springboot.erp.service.OrderService;

@WebMvcTest(
	    controllers = OrderController.class,
	    // garante que apenas o controller indicado entre
	    useDefaultFilters = false,
	    includeFilters = @ComponentScan.Filter(
	        type = FilterType.ASSIGNABLE_TYPE,
	        classes = OrderController.class
	    )
	)
@AutoConfigureMockMvc(addFilters = false) // evita 401/403 se Spring Security estiver no classpath
@Import(GlobalExceptionHandler.class) // garante que 500 vire resposta JSON
class OrderControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OrderService orderService;
    
    @MockBean
    private CustomerService customerService;
   

    private Order makeOrder(Long id, String number, BigDecimal totalAmount) {
        Order o = new Order();
        o.setId(id);
        o.setOrderNumber(number);
        o.setTotalAmount(totalAmount);
        return o;
    }

    // ---------- GETs ----------
    @Test
    @DisplayName("GET /api/orders - deve retornar lista")
    void getAllOrders_ok() throws Exception {
        Mockito.when(orderService.findAllOrders())
               .thenReturn(List.of(makeOrder(1L, "ORD-001", new BigDecimal("123.45"))));

        mvc.perform(get("/api/orders"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].id").value(1))
           .andExpect(jsonPath("$[0].orderNumber").value("ORD-001"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - encontrado")
    void getOrderById_found() throws Exception {
        Mockito.when(orderService.findOrderById(1L))
               .thenReturn(Optional.of(makeOrder(1L, "ORD-001", BigDecimal.TEN)));

        mvc.perform(get("/api/orders/1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.orderNumber").value("ORD-001"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - não encontrado")
    void getOrderById_notFound() throws Exception {
        Mockito.when(orderService.findOrderById(999L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/orders/999"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/orders/number/{orderNumber} - encontrado")
    void getOrderByNumber_found() throws Exception {
        Mockito.when(orderService.findOrderByNumber("ORD-XYZ"))
               .thenReturn(Optional.of(makeOrder(2L, "ORD-XYZ", BigDecimal.ONE)));

        mvc.perform(get("/api/orders/number/ORD-XYZ"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("GET /api/orders/number/{orderNumber} - não encontrado")
    void getOrderByNumber_notFound() throws Exception {
        Mockito.when(orderService.findOrderByNumber("NOPE")).thenReturn(Optional.empty());

        mvc.perform(get("/api/orders/number/NOPE"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/orders/customer/{customerId} - lista por cliente")
    void getOrdersByCustomerId_ok() throws Exception {
        Mockito.when(orderService.findOrdersByCustomerId(10L))
               .thenReturn(List.of(makeOrder(3L, "ORD-003", new BigDecimal("99.90"))));

        mvc.perform(get("/api/orders/customer/10"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].id").value(3))
           .andExpect(jsonPath("$[0].orderNumber").value("ORD-003"));
    }

    // ---------- POST / create ----------
    @Test
    @DisplayName("POST /api/orders?customerId=... - cria pedido (201)")
    void createOrder_created() throws Exception {
        Order created = makeOrder(4L, "ORD-004", new BigDecimal("200.00"));
        Mockito.when(orderService.createOrder(eq(77L), anyList())).thenReturn(created);

        String body =
            "[\n" +
            "  {\"id\":1,\"productId\":100,\"quantity\":2,\"unitPrice\":50.00},\n" +
            "  {\"id\":2,\"productId\":101,\"quantity\":1,\"unitPrice\":100.00}\n" +
            "]";

        mvc.perform(post("/api/orders")
                .param("customerId", "77")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(4))
           .andExpect(jsonPath("$.orderNumber").value("ORD-004"));
    }

    // ---------- CRUD de itens ----------
    @Test
    @DisplayName("POST /api/orders/{id}/items - adiciona item (200)")
    void addItem_ok() throws Exception {
        String body = "{"
                + "\"productId\":100,"
                + "\"quantity\":3,"
                + "\"unitPrice\":10.00"
                + "}";

        Mockito.doNothing().when(orderService).addItemToOrder(eq(5L), any(OrderItem.class));

        mvc.perform(post("/api/orders/5/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/orders/{id}/items/{itemId} - remove item (200)")
    void removeItem_ok() throws Exception {
        Mockito.doNothing().when(orderService).removeItemFromOrder(6L, 66L);

        mvc.perform(delete("/api/orders/6/items/66"))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/items - atualiza item (200)")
    void updateItem_ok() throws Exception {
        Mockito.doNothing().when(orderService).updateOrderItem(eq(7L), any(OrderItem.class));

        String body = "{"
                + "\"id\":70,"
                + "\"productId\":123,"
                + "\"quantity\":5,"
                + "\"unitPrice\":9.99"
                + "}";

        mvc.perform(put("/api/orders/7/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
           .andExpect(status().isOk());
    }

    // ---------- total / finalize / cancel / validate ----------
    @Test
    @DisplayName("GET /api/orders/{id}/total - retorna total")
    void calculateTotal_ok() throws Exception {
        Mockito.when(orderService.calculateOrderTotal(8L)).thenReturn(new BigDecimal("345.67"));

        mvc.perform(get("/api/orders/8/total"))
           .andExpect(status().isOk())
           .andExpect(content().string("345.67"));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/finalize - finaliza pedido")
    void finalizeOrder_ok() throws Exception {
        Mockito.doNothing().when(orderService).finalizeOrder(9L);

        mvc.perform(post("/api/orders/9/finalize"))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel - cancela pedido")
    void cancelOrder_ok() throws Exception {
        Mockito.doNothing().when(orderService).cancelOrder(10L);

        mvc.perform(post("/api/orders/10/cancel"))
           .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/orders/{id}/validate - retorna {valid:true}")
    void validateOrder_ok() throws Exception {
        mvc.perform(post("/api/orders/11/validate"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.valid", is(true)));
    }

    // ---------- (Opcional) covers: erros do service propagando para 500 ----------
    @Nested
    class ErrorPaths {
        @Test
        @DisplayName("GET /api/orders/{id}/total - service lança exceção -> 500")
        void total_serviceError_500() throws Exception {
            Mockito.when(orderService.calculateOrderTotal(123L))
                   .thenThrow(new RuntimeException("boom"));

            mvc.perform(get("/api/orders/123/total"))
               .andExpect(status().is5xxServerError());
        }
    }
}
