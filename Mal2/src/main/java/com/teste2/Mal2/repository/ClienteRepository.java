package com.teste2.Mal2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teste2.Mal2.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long>
{
	 Optional<Cliente> findByUsername(String username);
}
