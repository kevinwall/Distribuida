package com.teste2.Mal2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teste2.Mal2.error.NotFoundTokenException;
import com.teste2.Mal2.model.Token;

public interface TokenRepository extends JpaRepository<Token, Long>
{
	Optional<Token> findByValue(int value) throws NotFoundTokenException;
}
