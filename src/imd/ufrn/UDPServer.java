package imd.ufrn;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import imd.ufrn.model.Anime;
import imd.ufrn.model.Cliente;
import imd.ufrn.model.Message;
import imd.ufrn.model.MessageAnime;
import imd.ufrn.model.MessageCadastro;
import imd.ufrn.model.MessageLogin;
import imd.ufrn.model.MessageScore;

public class UDPServer {
	
	private ArrayList<Cliente> clientes = new ArrayList<Cliente>();
	private ArrayList<Anime> animes = new ArrayList<Anime>();
	
	private void avaliarAnime(Message msg) 
	{
		String dummy = msg.getContent();
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(dummy));
		reader.setLenient(true);
		MessageScore dummy2 = gson.fromJson(reader, MessageScore.class);
		
		for (Anime e : animes) 
		{
			if(e.getName().contentEquals(dummy2.getName())) 
			{
				e.addScore(dummy2.getScore());
				System.out.println("O anime " + e.getName() + " está com a média " + e.getMScore());
				break;
			}
		}
	}
	
	private void cadastrarUsuario(Message msg) 
	{
		String dummy = msg.getContent();
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(dummy));
		reader.setLenient(true);
		MessageCadastro dummy2 = gson.fromJson(reader, MessageCadastro.class);
		
		
		Cliente temp = new Cliente();
		
		temp.setUsername(dummy2.getUsuario());
		temp.setPassword(dummy2.getSenha());
		
		clientes.add(temp);
	}
	
	private void consultarUsuario(Message msg) 
	{
		String dummy = msg.getContent();
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(dummy));
		reader.setLenient(true);
		MessageLogin dummy2 = gson.fromJson(reader, MessageLogin.class);
		
		Cliente temp = new Cliente();
		
		temp.setUsername(dummy2.getUsuario());
		temp.setPassword(dummy2.getSenha());
		for (Cliente e : clientes) 
		{
			if(e.getUsername().contentEquals(temp.getUsername()) && e.getPassword().contentEquals(temp.getPassword()))
			{
				System.out.print("Usuário " + temp.getUsername() + " logado no sistema");
			}
		}
	}
	
	private void cadastrarAnime(Message msg) 
	{
		String dummy = msg.getContent();
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(dummy));
		reader.setLenient(true);
		MessageAnime dummy2 = gson.fromJson(reader, MessageAnime.class);
		
		Anime anm = new Anime();
		
		anm.setName(dummy2.getName());
		anm.setEpisodes(dummy2.getEpisodes());
		anm.setSummmary(dummy2.getSummary());
		
		animes.add(anm);
		
		System.out.println("Animes cadastrados: ");
		for (Anime e : animes) 
		{
			System.out.println("---- "+e.getName());
		}
	}
	
	public UDPServer() {
		System.out.println("UDP Server Started");
		try {
			DatagramSocket serverSocket = new DatagramSocket(9003);
			while (true) {
				byte[] receiveMessage = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				serverSocket.receive(receivePacket);
				String message = new String(receivePacket.getData());
				
				// JSON Update
				Gson gson = new Gson();
				JsonReader reader = new JsonReader(new StringReader(message));
				reader.setLenient(true);
				Message msg = gson.fromJson(reader, Message.class);
				// END
				
				if(msg != null) 
				{
					int type = msg.getType();
					
					switch(type) 
					{
						case 1:
							cadastrarUsuario(msg);
							break;
						case 2:
							consultarUsuario(msg);
							break;
						case 3:
							cadastrarAnime(msg);
							break;
						case 4:
							avaliarAnime(msg);
							break;
					}
				}
				else 
				{
					System.out.print("Null message");
				}
				
				//System.out.println("Received from client: [" + msg.getContent()+ "]\nFrom: " + receivePacket.getAddress());
				
			}
		}catch (IOException e) {
				e.printStackTrace();
				System.out.println("UDP Server Terminating");		
		}
	}
	public static void main(String[] args) { 
			new UDPServer();    
		}
}
