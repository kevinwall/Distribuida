package ufrn.imd;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

@FeignClient(name="AnimeService")
@RibbonClient(name="AnimeService")
public interface RibbonAnimeServiceProxy 
{
	
	@PostMapping("/AnimeService/Evaluate")
	public String avaliarAnime(@RequestBody String message);
	
	@PostMapping("/AnimeService/Register")
	public String cadastroAnime(@RequestBody String message);
}