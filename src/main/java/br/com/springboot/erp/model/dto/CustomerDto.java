package br.com.springboot.erp.model.dto;

import java.io.Serializable;

import br.com.springboot.erp.model.entity.Customer;

/**
 * Uso do record como DTOs, Value Objects, transporte de dados
 * 
 * 	- Reduz boilerplate
 *	- Final: não pode ser herdado
 *	- Campos imutáveis: sempre private final
 *	- Pode implementar interfaces (ex.: Serializable)
 *	- Pode ter métodos adicionais e construtores compactos
 *	- Gera automaticamente: construtor, getters, equals, hashCode, toString
 * 
 */
public record CustomerDto(Long id, String name, String email, String phone) implements Serializable{
	public static CustomerDto from(Customer c) {
		return new CustomerDto(c.getId(), c.getName(), c.getEmail(), c.getPhone());
	}
}