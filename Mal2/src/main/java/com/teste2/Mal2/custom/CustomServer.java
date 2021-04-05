package com.teste2.Mal2.custom;

import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomServer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
{
	public static int PORT;

	public void customize(ConfigurableServletWebServerFactory factory)
	{
		factory.setPort(PORT);
	}
}
