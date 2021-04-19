package clientHTTP;

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
	
	static int token;
	
	public static void main(String args[]) 
	{
		
		Scanner scanner = new Scanner(System.in);	
		
		while(true) 
		{
			System.out.println("Enter the message type: ");
			String type2 = scanner.nextLine();
			
			if (type2.equalsIgnoreCase("quit")) 
			{
				break;
			}
			
			int type = Integer.parseInt(type2);
			
			RestTemplate restTemplate = new RestTemplate();
			
			switch(type) 
			{
				case 1: 
					
					MessageCadastro msg = new MessageCadastro();
					
					System.out.println("Enter the username: ");
					msg.setUsuario(scanner.nextLine());
					
					System.out.println("Enter the password: ");
					msg.setSenha(scanner.nextLine());
					
					try {
						ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + 8100 + "/ClientService/Register", msg, String.class);
							
						System.out.println("Resposta da requisição: " + response.getBody());
					}catch(Exception ex) 
					{
						System.out.println("Não foi possível enviar a mensagem...");
					}
					break;
				case 2:
					MessageLogin msg2 = new MessageLogin();
					
					System.out.println("Enter the username: ");
					msg2.setUsuario(scanner.nextLine());
					
					System.out.println("Enter the password: ");
					msg2.setSenha(scanner.nextLine());
					
					try {
						ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + 8100 + "/ClientService/Login", msg2, String.class);
							
						System.out.println("Resposta da requisição: " + response.getBody());
						token = Integer.parseInt(response.getBody());
					}catch(Exception ex) 
					{
						System.out.println("Não foi possível enviar a mensagem...");
					}
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
					
					try {
						ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + 8100 + "/AnimeService/Register", msg3, String.class);
							
						System.out.println("Resposta da requisição: " + response.getBody());
					}catch(Exception ex) 
					{
						System.out.println("Não foi possível enviar a mensagem...");
					}
					break;
				case 4:
					MessageScore msg4 = new MessageScore();
					
					System.out.println("Enter the anime name: ");
					msg4.setName(scanner.nextLine());
					
					System.out.println("Enter the score that you want to give: ");
					msg4.setScore(Double.parseDouble(scanner.nextLine()));
					
					msg4.setUserToken(token);
					
					try {
						ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + 8100 + "/AnimeService/Evaluate", msg4, String.class);
							
						System.out.println("Resposta da requisição: " + response.getBody());
					}catch(Exception ex) 
					{
						System.out.println("Não foi possível enviar a mensagem...");
					}
					break;
			}
		}
		
		scanner.close();
	}
}
