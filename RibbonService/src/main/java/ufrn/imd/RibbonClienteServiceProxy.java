package ufrn.imd;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

@FeignClient(name="ClienteService")
@RibbonClient(name="ClienteService")
public interface RibbonClienteServiceProxy 
{
	
	@PostMapping("/ClientService/Register")
	public String cadastroCliente(@RequestBody String message);
	
	@PostMapping("/ClientService/Login")
	public String loginCliente(@RequestBody String message);
	
	@GetMapping("/ClientService/Verify/{value}")
	public Integer verify(@PathVariable int value);
}