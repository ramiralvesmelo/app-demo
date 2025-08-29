package br.com.springboot.erp.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import org.springframework.stereotype.Repository;

import br.com.springboot.erp.model.entity.Customer;

/**
 * Implementação do repositório para a entidade Customer.
 */
@Repository
public class CustomerRepositoryImpl extends BaseRepositoryImpl<Customer, Long> implements CustomerRepository {

	@Override
	public Optional<Customer> findByEmail(String email) {
		TypedQuery<Customer> query = entityManager
				.createQuery("SELECT c FROM Customer c WHERE LOWER(c.email) = LOWER(:email)", Customer.class);
		query.setParameter("email", email);

		try {
			return Optional.of(query.getSingleResult());
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	@Override
	public List<Customer> findByNameContaining(String name) {
		String nameMin = name != null ? name.toLowerCase() : "";
		TypedQuery<Customer> query = entityManager
				.createQuery("SELECT c FROM Customer c WHERE LOWER(c.name) LIKE LOWER(:name)", Customer.class);
		query.setParameter("name", "%" + nameMin + "%");
		return query.getResultList();
	}
	
	
	@Override
	public List<Customer> findCustomersWithOrders() {
		TypedQuery<Customer> query = entityManager.createQuery("SELECT c FROM Customer c JOIN FETCH c.orders o", Customer.class);
		List<Customer> allCustomers = query.getResultList();

		return allCustomers.stream().filter(c -> c.getOrders() != null && !c.getOrders().isEmpty()).distinct()
				.collect(Collectors.toList());
	}
}
