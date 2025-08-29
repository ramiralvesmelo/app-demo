package br.com.springboot.erp.controller;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

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

import br.com.springboot.erp.model.dto.CustomerDto;
import br.com.springboot.erp.model.entity.Customer;
import br.com.springboot.erp.service.CustomerService;

/**
 * Controller para gerenciamento de clientes.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }


    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<CustomerDto> customers = customerService.findAllCustomers()
                .stream()
                .map(CustomerDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        return customerService.findCustomerById(id)
                .map(CustomerDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CustomerDto> getCustomerByEmail(@PathVariable String email) {
        return customerService.findCustomerByEmail(email)
                .map(CustomerDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<CustomerDto>> searchCustomersByName(@RequestParam String name) {
        List<CustomerDto> customers = customerService.searchCustomersByName(name)
                .stream()
                .map(CustomerDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerService.saveCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerDto.from(savedCustomer));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(@PathVariable Long id, @RequestBody @Valid Customer customer) {
        return customerService.findCustomerById(id)
                .map(existing -> {
                    customer.setId(id);
                    Customer updated = customerService.updateCustomer(customer);
                    return ResponseEntity.ok(CustomerDto.from(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/with-orders")
    public ResponseEntity<List<CustomerDto>> getCustomersWithOrders() {
        List<CustomerDto> customers = customerService.findCustomersWithOrders()
                .stream()
                .map(CustomerDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/validate-email")
    public ResponseEntity<Boolean> validateEmail(@RequestParam String email) {
        boolean isValid = customerService.validateCustomerEmail(email);
        return isValid ? ResponseEntity.ok(true) : ResponseEntity.badRequest().body(false);
    }
}