package br.com.springboot.erp.service;

import java.util.List;
import java.util.Optional;

import br.com.springboot.erp.model.Customer;

/**
 * Serviço para gerenciamento de clientes.
 */
public interface CustomerService {
    
    Customer saveCustomer(Customer customer);
    
    Optional<Customer> findCustomerById(Long id);
    
    Optional<Customer> findCustomerByEmail(String email);
    
    List<Customer> findAllCustomers();
    
    List<Customer> searchCustomersByName(String name);
    
    Customer updateCustomer(Customer customer);
    
    void deleteCustomer(Long customerId);
    
    List<Customer> findCustomersWithOrders();
    
    boolean validateCustomerEmail(String email);
}