package ufrn.imd;

import java.io.StringReader;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import ufrn.imd.error.NotFoundAnimeException;
import ufrn.imd.model.Anime;
import ufrn.imd.model.MessageAnime;
import ufrn.imd.model.MessageScore;
import ufrn.imd.repository.AnimeRepository;

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
		
		ArrayList<Integer> arr = new ArrayList<Integer>();
		arr.add(9040);
		arr.add(9041);
		
		for(Integer e : arr) 
		{
			RestTemplate restTemplate = new RestTemplate();
			
			try {
				ResponseEntity<Integer> response = restTemplate.getForEntity(ROOT_URI + e +"/ClientService/Verify/"+msg.getUserToken(), Integer.class);
				
				if(response.getBody() == 1) 
				{
					Anime a1 = new Anime();
					a1.setName(msg.getName());
					a1.setEpisodes(msg.getEpisodes());
					a1.setSummmary(msg.getSummary());
					animeRepository.save(a1);
					
					return "Anime cadastrado com sucesso";
				}
			}catch(Exception ex) 
			{
				System.out.println("Tentando outro servidor de autenticação...");
				continue;
			}
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
		
		ArrayList<Integer> arr = new ArrayList<Integer>();
		arr.add(9040);
		arr.add(9041);
		
		for(Integer e : arr) 
		{
			try 
			{
				RestTemplate restTemplate = new RestTemplate();
				
				ResponseEntity<Integer> response = restTemplate.getForEntity(ROOT_URI + e +"/ClientService/Verify/"+msg.getUserToken(), Integer.class);
				
				if(response.getBody() == 1) 
				{
					try 
					{
						Anime a1 = animeRepository.findByName(msg.getName()).get();
						
						a1.addScore(msg.getScore());
						
						animeRepository.save(a1);
						
						return "Anime avaliado com sucesso";			
					}catch(NotFoundAnimeException ex) 
					{
						System.out.println("Anime não encontrado");
					}
				}
			}catch(Exception ex2) 
			{
				System.out.println("Tentando outro servidor de autenticação...");
				continue;
			}
		}
		
		return "Erro ao avaliar anime";
	}
}
