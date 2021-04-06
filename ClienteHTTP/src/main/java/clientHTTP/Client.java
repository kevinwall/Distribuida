package clientHTTP;

import java.util.ArrayList;
import java.util.Scanner;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import model.MessageAnime;
import model.MessageCadastro;
import model.MessageLogin;
import model.MessageScore;

public class Client 
{
	final static String ROOT_URI = "http://localhost:";
	final static String CLIENT_URI = "/ClientService";
	final static String ANIME_URI = "/AnimeService";
	
	static int token;
	
	static ArrayList<Integer> portCliente = new ArrayList<Integer>();
	static ArrayList<Integer> portAnime = new ArrayList<Integer>();
	
	private static void sendMessage(int type, String message) 
	{
		RestTemplate restTemplate = new RestTemplate();
		
		if(type == 1) 
		{
			for(Integer e : portCliente) 
			{
				try {
					ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + e + CLIENT_URI + "/Register", message, String.class);
					
					System.out.println("Resposta da requisição: " + response.getBody());
					break;
				}catch(Exception ex) 
				{
					System.out.println("Tentando outro servidor...");
					continue;
				}
			}
		}
		else if(type == 2) 
		{
			for(Integer e : portCliente) 
			{
				try {
					ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + e + CLIENT_URI + "/Login", message, String.class);
					
					System.out.println("Resposta da requisição: " + response.getBody());
					token = Integer.parseInt(response.getBody());
					break;
				}catch(Exception ex) 
				{
					System.out.println("Tentando outro servidor...");
					continue;
				}
			}
		}
		else if(type == 3) 
		{
			for(Integer e : portAnime) 
			{
				try {
					ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + e + ANIME_URI + "/Register", message, String.class);
					
					System.out.println("Resposta da requisição: " + response.getBody());
					break;
				}catch(Exception ex) 
				{
					System.out.println("Tentando outro servidor...");
					continue;
				}
			}
		}
		else if(type == 4) 
		{
			for(Integer e : portAnime) 
			{
				try {
					ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + e + ANIME_URI + "/Evaluate", message, String.class);
					
					System.out.println("Resposta da requisição: " + response.getBody());
					break;
				}catch(Exception ex) 
				{
					System.out.println("Tentando outro servidor...");
					continue;
				}
			}
		}
	}
	
	public static void main(String args[]) 
	{
	
		portCliente.add(9040);
		portCliente.add(9041);
		portAnime.add(9030);
		portAnime.add(9031);
		
		Scanner scanner = new Scanner(System.in);	
		Gson gson = new Gson();
		
		while(true) 
		{
			System.out.println("Enter the message type: ");
			String type2 = scanner.nextLine();
			
			if (type2.equalsIgnoreCase("quit")) 
			{
				break;
			}
			
			int type = Integer.parseInt(type2);
			
			switch(type) 
			{
				case 1: 
					
					MessageCadastro msg = new MessageCadastro();
					
					System.out.println("Enter the username: ");
					msg.setUsuario(scanner.nextLine());
					
					System.out.println("Enter the password: ");
					msg.setSenha(scanner.nextLine());
					
					sendMessage(type, gson.toJson(msg, MessageCadastro.class));
					break;
				case 2:
					MessageLogin msg2 = new MessageLogin();
					
					System.out.println("Enter the username: ");
					msg2.setUsuario(scanner.nextLine());
					
					System.out.println("Enter the password: ");
					msg2.setSenha(scanner.nextLine());
					
					sendMessage(type, gson.toJson(msg2, MessageLogin.class));
					break;
				case 3:
					MessageAnime msg3 = new MessageAnime();
					
					System.out.println("Enter the anime name: ");
					msg3.setName(scanner.nextLine());
					
					System.out.println("Enter the quantity of episodes: ");
					msg3.setEpisodes(Integer.parseInt(scanner.nextLine()));
					
					System.out.println("Enter a brief summary of the anime");
					msg3.setSummary(scanner.nextLine());
					
					msg3.setUserToken(token);
					
					sendMessage(type, gson.toJson(msg3, MessageAnime.class));
					break;
				case 4:
					MessageScore msg4 = new MessageScore();
					
					System.out.println("Enter the anime name: ");
					msg4.setName(scanner.nextLine());
					
					System.out.println("Enter the score that you want to give: ");
					msg4.setScore(Double.parseDouble(scanner.nextLine()));
					
					msg4.setUserToken(token);
					
					sendMessage(type, gson.toJson(msg4, MessageScore.class));
					break;
			}
		}
		
		scanner.close();
	}
}
