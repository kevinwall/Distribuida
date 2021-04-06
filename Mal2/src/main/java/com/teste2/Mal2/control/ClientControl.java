package com.teste2.Mal2.control;

import java.io.StringReader;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.teste2.Mal2.error.NotFoundTokenException;
import com.teste2.Mal2.model.Cliente;
import com.teste2.Mal2.model.MessageCadastro;
import com.teste2.Mal2.model.MessageLogin;
import com.teste2.Mal2.model.Token;
import com.teste2.Mal2.repository.ClienteRepository;
import com.teste2.Mal2.repository.TokenRepository;

@RestController
@RequestMapping("/ClientService")
public class ClientControl 
{
	@Autowired
	ClienteRepository clienteRepository;
	@Autowired
	TokenRepository tokenRepository;
	
	private static Integer count = 0;
	
	@PostMapping("/Register")
	public String cadastroCliente(@RequestBody String message) 
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		MessageCadastro msg = gson.fromJson(reader, MessageCadastro.class);
		
		Cliente c2 = new Cliente();
		c2.setUsername(msg.getUsuario());
		c2.setPassword(msg.getSenha());
		
		clienteRepository.save(c2);
		
		return "Cliente salvo com sucesso";
	}
	
	@PostMapping("/Login")
	public String loginCliente(@RequestBody String message) 
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		MessageLogin msg = gson.fromJson(reader, MessageLogin.class);
		
		Cliente c1  = clienteRepository.findByUsername(msg.getUsuario()).get();
		
		if(c1.getPassword().equals(msg.getSenha())) 
		{
			Token tk = new Token();
			count++;
			tk.setValue(count);
			tokenRepository.save(tk);
			return count.toString();
		}
		
		return "Não foi possível efetuar o login do cliente";
	}
	
	@GetMapping("/Verify/{value}")
	public Integer verify(@PathVariable int value) 
	{
		Optional<Token> tk;
		try 
		{
			tk = tokenRepository.findByValue(value);
			return 1;
		}catch(NotFoundTokenException e) 
		{
			System.out.println("Token inválido");
		}
		
		return 0;
	}
}
