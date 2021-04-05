package com.teste2.Mal2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.teste2.Mal2.model.Anime;

public interface AnimeRepository extends JpaRepository<Anime, Long>
{
	Optional<Anime> findByName(String name);
}
