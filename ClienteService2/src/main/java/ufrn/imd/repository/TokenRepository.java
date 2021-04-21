package ufrn.imd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ufrn.imd.error.NotFoundTokenException;
import ufrn.imd.model.Token;

public interface TokenRepository extends JpaRepository<Token, Long>
{
	Optional<Token> findByValue(int value) throws NotFoundTokenException;
}
