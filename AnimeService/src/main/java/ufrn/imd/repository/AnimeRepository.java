package ufrn.imd.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ufrn.imd.error.NotFoundAnimeException;
import ufrn.imd.model.Anime;

public interface AnimeRepository extends JpaRepository<Anime, Long>
{
	Optional<Anime> findByName(String name) throws NotFoundAnimeException;
}
