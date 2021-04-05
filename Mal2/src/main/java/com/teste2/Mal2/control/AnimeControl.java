package com.teste2.Mal2.control;

import java.io.StringReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.teste2.Mal2.error.NotFoundAnimeException;
import com.teste2.Mal2.model.Anime;
import com.teste2.Mal2.model.MessageAnime;
import com.teste2.Mal2.model.MessageScore;
import com.teste2.Mal2.repository.AnimeRepository;

@RestController
@RequestMapping("/AnimeService")
public class AnimeControl 
{
	@Autowired
	AnimeRepository animeRepository;
	
	final static String ROOT_URI = "http://localhost:";
	
	@PostMapping("/Register")
	public String cadastroAnime(@RequestBody String message) 
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		MessageAnime msg = gson.fromJson(reader, MessageAnime.class);
		
		RestTemplate restTemplate = new RestTemplate();
	
		ResponseEntity<Integer> response = restTemplate.getForEntity(ROOT_URI + "9040/ClientService/Verify/"+msg.getUserToken(), Integer.class);
		
		if(response.getBody() == 1) 
		{
			Anime a1 = new Anime();
			a1.setName(msg.getName());
			a1.setEpisodes(msg.getEpisodes());
			a1.setSummmary(msg.getSummary());
			animeRepository.save(a1);
			
			return "Anime cadastrado com sucesso";
		}
		
		return "Erro ao cadastrar anime";
	}
	
	@PostMapping("/Evaluate")
	public String avaliarAnime(@RequestBody String message) 
	{
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(message));
		reader.setLenient(true);
		
		MessageScore msg = gson.fromJson(reader, MessageScore.class);
		
		RestTemplate restTemplate = new RestTemplate();
	
		ResponseEntity<Integer> response = restTemplate.getForEntity(ROOT_URI + "9040/ClientService/Verify/"+msg.getUserToken(), Integer.class);
		
		if(response.getBody() == 1) 
		{
			try 
			{
				Anime a1 = animeRepository.findByName(msg.getName()).get();
				
				a1.addScore(msg.getScore());
				
				animeRepository.save(a1);
				
				return "Anime avaliado com sucesso";			
			}catch(NotFoundAnimeException e) 
			{
				System.out.println("Anime não encontrado");
			}
		}
		
		return "Erro ao avaliar anime";
	}
}
