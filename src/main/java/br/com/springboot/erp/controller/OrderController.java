package br.com.springboot.erp.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.springboot.erp.model.dto.OrderDto;
import br.com.springboot.erp.model.entity.Order;
import br.com.springboot.erp.model.entity.OrderItem;
import br.com.springboot.erp.service.OrderService;

/**
 * Controller para gerenciamento de pedidos.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Lista todos (resumo)
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<OrderDto> orders = orderService.findAllOrders()
                .stream()
                .map(OrderDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    // Busca por ID (resumo)
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return orderService.findOrderById(id)
                .map(OrderDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Busca por ID (detalhes com itens)
    @GetMapping("/{id}/details")
    public ResponseEntity<OrderDto> getOrderDetails(@PathVariable Long id) {
        return orderService.findOrderById(id)
                .map(OrderDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Busca por número (resumo)
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDto> getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.findOrderByNumber(orderNumber)
                .map(OrderDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Pedidos por cliente (resumo)
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<OrderDto> orders = orderService.findOrdersByCustomerId(customerId)
                .stream()
                .map(OrderDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    // Criar pedido (retorna detalhes com itens)
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestParam Long customerId, @RequestBody List<OrderItem> items) {
        Order order = orderService.createOrder(customerId, items);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderDto.from(order));
    }

    // Adicionar item
    @PostMapping("/{orderId}/items")
    public ResponseEntity<Void> addItemToOrder(@PathVariable Long orderId, @RequestBody OrderItem item) {
        orderService.addItemToOrder(orderId, item);
        return ResponseEntity.ok().build();
    }

    // Remover item
    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromOrder(@PathVariable Long orderId, @PathVariable Long itemId) {
        orderService.removeItemFromOrder(orderId, itemId);
        return ResponseEntity.ok().build();
    }

    // Atualizar item
    @PutMapping("/{orderId}/items")
    public ResponseEntity<Void> updateOrderItem(@PathVariable Long orderId, @RequestBody OrderItem item) {
        orderService.updateOrderItem(orderId, item);
        return ResponseEntity.ok().build();
    }

    // Total do pedido
    @GetMapping("/{orderId}/total")
    public ResponseEntity<BigDecimal> calculateOrderTotal(@PathVariable Long orderId) {
        BigDecimal total = orderService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(total);
    }

    // Finalizar pedido
    @PostMapping("/{orderId}/finalize")
    public ResponseEntity<Void> finalizeOrder(@PathVariable Long orderId) {
        orderService.finalizeOrder(orderId);
        return ResponseEntity.ok().build();
    }

    // Cancelar pedido
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    // Validação simples
    @PostMapping("/{orderId}/validate")
    public ResponseEntity<Map<String, Boolean>> validateOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(Map.of("valid", true));
    }
}
