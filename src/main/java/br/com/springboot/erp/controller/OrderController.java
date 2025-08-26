package br.com.springboot.erp.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

import br.com.springboot.erp.model.Order;
import br.com.springboot.erp.model.OrderItem;
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

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.findOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Order> getOrderByNumber(@PathVariable String orderNumber) {
        return orderService.findOrderByNumber(orderNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable Long customerId) {
        List<Order> orders = orderService.findOrdersByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestParam Long customerId, @RequestBody List<OrderItem> items) {
        Order order = orderService.createOrder(customerId, items);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<Void> addItemToOrder(@PathVariable Long orderId, @RequestBody OrderItem item) {
        orderService.addItemToOrder(orderId, item);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Void> removeItemFromOrder(@PathVariable Long orderId, @PathVariable Long itemId) {
        orderService.removeItemFromOrder(orderId, itemId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{orderId}/items")
    public ResponseEntity<Void> updateOrderItem(@PathVariable Long orderId, @RequestBody OrderItem item) {
        orderService.updateOrderItem(orderId, item);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/total")
    public ResponseEntity<BigDecimal> calculateOrderTotal(@PathVariable Long orderId) {
        BigDecimal total = orderService.calculateOrderTotal(orderId);
        return ResponseEntity.ok(total);
    }

    @PostMapping("/{orderId}/finalize")
    public ResponseEntity<Void> finalizeOrder(@PathVariable Long orderId) {
        orderService.finalizeOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/validate")
    public ResponseEntity<Map<String, Boolean>> validateOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(Map.of("valid", true));
    }
}