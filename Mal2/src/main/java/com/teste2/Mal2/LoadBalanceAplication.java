package com.teste2.Mal2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.teste2.Mal2.custom.CustomServer;

@SpringBootApplication
public class LoadBalanceAplication 
{
	public static void main(String[] args) 
	{
		int port = 9010;

        CustomServer.PORT = port;
        SpringApplication.run(ClienteServerAplication.class, args);
    }
}
