package ufrn.imd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

import brave.sampler.Sampler;

@SpringBootApplication
@EnableDiscoveryClient
public class ClienteServiceApplication {

	public static void main(String[] args) 
	{
		SpringApplication.run(ClienteServiceApplication.class, args);
	}

}
