package ufrn.imd;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.netflix.ribbon.RibbonClient;

//@FeignClient(name="clienteservice")
@FeignClient(name="netflix-zuul-api-gateway-server")
@RibbonClient(name="clienteservice")
public interface RibbonClienteServiceProxy 
{
	
	@PostMapping("/clienteservice/ClientService/Register")
	public String cadastroCliente(@RequestBody String message);
	
	@PostMapping("/clienteservice/ClientService/Login")
	public String loginCliente(@RequestBody String message);
	
	@GetMapping("/clienteservice/ClientService/Verify/{value}")
	public Integer verify(@PathVariable int value);
}