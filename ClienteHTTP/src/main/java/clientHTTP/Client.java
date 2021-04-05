package clientHTTP;

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
	
	public static void main(String args[]) 
	{
		RestTemplate restTemplate = new RestTemplate();
		
		MessageCadastro msg1 = new MessageCadastro();
		msg1.setUsuario("kevinwall");
		msg1.setSenha("123");
		
		Gson gson = new Gson();
		String message = gson.toJson(msg1, MessageCadastro.class);
		
		ResponseEntity<String> response = restTemplate.postForEntity(ROOT_URI + "9040/ClientService/Register", message, String.class);
		
		System.out.println("Resposta do post: " + response.getBody());
		
		MessageLogin msg2 = new MessageLogin();
		msg2.setUsuario("kevinwall");
		msg2.setSenha("123");
		
		message = gson.toJson(msg2);
		
		System.out.println(message);
		
		response = restTemplate.postForEntity(ROOT_URI + "9041/ClientService/Login", message, String.class);
		System.out.println("Resposta do post2: " + response.getBody());
		
		MessageAnime msg3 = new MessageAnime();
		msg3.setName("Mahouka");
		msg3.setEpisodes(24);
		msg3.setSummary("aaaaaaaaa");
		
		Integer token = Integer.parseInt(response.getBody());
		
		msg3.setUserToken(token);
		
		message = gson.toJson(msg3);
		
		response = restTemplate.postForEntity(ROOT_URI + "9030/AnimeService/Register", message, String.class);
		System.out.println("Resposta do post3: " + response.getBody());
		
		MessageScore msg4 = new MessageScore();
		msg4.setName("Mahouka");
		msg4.setScore(10.0);
		msg4.setUserToken(token);	
		
		message = gson.toJson(msg4);
		
		response = restTemplate.postForEntity(ROOT_URI + "9031/AnimeService/Evaluate", message, String.class);
		System.out.println("Resposta do post4: " + response.getBody());
		
		MessageScore msg5 = new MessageScore();
		msg5.setName("Mahouka");
		msg5.setScore(3.0);
		msg5.setUserToken(token);	
		
		message = gson.toJson(msg5);
		
		response = restTemplate.postForEntity(ROOT_URI + "9030/AnimeService/Evaluate", message, String.class);
		System.out.println("Resposta do post5: " + response.getBody());
	}
}
