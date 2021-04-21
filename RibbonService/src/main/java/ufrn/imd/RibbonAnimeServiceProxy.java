package ufrn.imd;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

//@FeignClient(name="animeservice")
@FeignClient(name="netflix-zuul-api-gateway-server")
@RibbonClient(name="animeservice")
public interface RibbonAnimeServiceProxy 
{
	
	@GetMapping("/animeservice/verifyrange/{value}")
	public Double verifyRange(@PathVariable Double value);
	
	@PostMapping("/animeservice/AnimeService/Evaluate")
	public String avaliarAnime(@RequestBody String message);
	
	@PostMapping("/animeservice/AnimeService/Register")
	public String cadastroAnime(@RequestBody String message);
}