package br.com.springboot.erp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.springboot.erp.model.Customer;

/**
 * Repositório para a entidade Customer.
 */
@Repository
public interface CustomerRepository extends BaseRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
    
    List<Customer> findByNameContaining(String name);
    
    List<Customer> findCustomersWithOrders();
       
}