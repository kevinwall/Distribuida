package ufrn.imd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class RibbonServiceController {

	@Autowired
	private RibbonClienteServiceProxy proxy1;
	
	@Autowired
	private RibbonAnimeServiceProxy proxy2;

	@PostMapping("/AnimeService/Register")
	public String cadastroAnime(@RequestBody String message) 
	{
		return proxy2.cadastroAnime(message);
	}
	
	@PostMapping("/AnimeService/Evaluate")
	public String avaliarAnime(@RequestBody String message) 
	{
		return proxy2.avaliarAnime(message);
	}
	
	@PostMapping("/ClientService/Register")
	public String cadastroCliente(@RequestBody String message) 
	{
		return proxy1.cadastroCliente(message);
	}
	
	@PostMapping("/ClientService/Login")
	public String loginCliente(@RequestBody String message) 
	{
		return proxy1.loginCliente(message);
	}
	
	@GetMapping("/ClientService/Verify/{value}")
	public Integer verify(@PathVariable int value) 
	{
		return proxy1.verify(value);
	}

}
