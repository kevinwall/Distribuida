package com.teste2.Mal2.control;

import java.io.StringReader;
import java.util.ArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.teste2.Mal2.model.LoadAux;
import com.teste2.Mal2.model.Message;
import com.teste2.Mal2.model.MessageAnime;
import com.teste2.Mal2.model.MessageCadastro;
import com.teste2.Mal2.model.MessageLogin;
import com.teste2.Mal2.model.MessageScore;

@RestController
@RequestMapping("/LoadService")
public class LoadControl 
{
	final static String ROOT_URI = "http://localhost:";
	
	private static ArrayList<LoadAux> portsCliente;
	private static ArrayList<LoadAux> portsAnime;
	
	public LoadControl() 
	{
		portsCliente = new ArrayList<LoadAux>();
		portsAnime = new ArrayList<LoadAux>();
		
		LoadAux load1 = new LoadAux();
		LoadAux load2 = new LoadAux();
		LoadAux load3 = new LoadAux();
		LoadAux load4 = new LoadAux();
		
		load1.setLoad(0);
		load1.setPort(9040);
		load2.setLoad(0);
		load2.setPort(9041);
		
		load3.setLoad(0);
		load3.setPort(9030);
		load4.setLoad(0);
		load4.setPort(9031);
		
		portsCliente.add(load1);
		portsCliente.add(load2);
		
		portsAnime.add(load3);
		portsAnime.add(load4);
	}
	
	private void cadastrarCliente(String message)
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		MessageCadastro msg = gson.fromJson(reader, MessageCadastro.class);
		
		portsCliente.sort((LoadAux rhs, LoadAux lhs) ->
		{
			if(rhs.getLoad() < lhs.getLoad()) 
			{
				return -1;
			}else if(rhs.getLoad() == lhs.getLoad()) 
			{
				return 0;
			}
			
			return 1;
		}
				);
		
		for(LoadAux e : portsCliente) 
		{
			RestTemplate restTemplate = new RestTemplate();
			try 
			{
				ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + e.getPort() +"/ClientService/Register", msg, String.class);
				System.out.println(response.getBody());
				break;
			}catch(Exception ex) 
			{
				System.out.println("Tentando outro servidor de cliente...");
				continue;
			}
		}
	}
	
	private String loginCliente(String message)
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		MessageLogin msg = gson.fromJson(reader, MessageLogin.class);
		
		portsCliente.sort((LoadAux rhs, LoadAux lhs) ->
		{
			if(rhs.getLoad() < lhs.getLoad()) 
			{
				return -1;
			}else if(rhs.getLoad() == lhs.getLoad()) 
			{
				return 0;
			}
			
			return 1;
		}
				);
		
		for(LoadAux e : portsCliente) 
		{
			RestTemplate restTemplate = new RestTemplate();
			try 
			{
				ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + e.getPort() +"/ClientService/Login", msg, String.class);
				return response.getBody();
			}catch(Exception ex) 
			{
				System.out.println("Tentando outro servidor de cliente...");
				continue;
			}
		}
		
		return "Not Found";
	}
	
	private void cadastrarAnime(String message)
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		MessageAnime msg = gson.fromJson(reader, MessageAnime.class);
		
		portsAnime.sort((LoadAux rhs, LoadAux lhs) ->
		{
			if(rhs.getLoad() < lhs.getLoad()) 
			{
				return -1;
			}else if(rhs.getLoad() == lhs.getLoad()) 
			{
				return 0;
			}
			
			return 1;
		}
				);
		
		for(LoadAux e : portsAnime) 
		{
			RestTemplate restTemplate = new RestTemplate();
			try 
			{
				ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + e.getPort() +"/AnimeService/Register", msg, String.class);
				System.out.println(response.getBody());
				break;
			}catch(Exception ex) 
			{
				System.out.println("Tentando outro servidor de anime...");
				continue;
			}
		}
	}
	
	private void avaliarAnime(String message)
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		MessageScore msg = gson.fromJson(reader, MessageScore.class);
		
		portsAnime.sort((LoadAux rhs, LoadAux lhs) ->
		{
			if(rhs.getLoad() < lhs.getLoad()) 
			{
				return -1;
			}else if(rhs.getLoad() == lhs.getLoad()) 
			{
				return 0;
			}
			
			return 1;
		}
				);
		
		for(LoadAux e : portsAnime) 
		{
			RestTemplate restTemplate = new RestTemplate();
			try 
			{
				ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + e.getPort() +"/AnimeService/Evaluate", msg, String.class);
				System.out.println(response.getBody());
				break;
			}catch(Exception ex) 
			{
				System.out.println("Tentando outro servidor de anime...");
				continue;
			}
		}
	}
	
	@PostMapping
	public String balance(@RequestBody String message) 
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		Message msg = gson.fromJson(reader, Message.class);
		
		if(msg.getType() == 1) 
		{
			cadastrarCliente(msg.getContent());
		}
		else if(msg.getType() == 2) 
		{
			String tk = loginCliente(msg.getContent());
			return tk;
		}
		else if(msg.getType() == 3) 
		{
			cadastrarAnime(msg.getContent());
		}
		else if(msg.getType() == 4) 
		{
			avaliarAnime(msg.getContent());
		}
		
		return "Operação bem sucedida";
	}
}
