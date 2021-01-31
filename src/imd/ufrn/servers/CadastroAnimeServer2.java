package imd.ufrn.servers;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import imd.ufrn.model.Anime;
import imd.ufrn.model.Cliente;
import imd.ufrn.model.Message;
import imd.ufrn.model.MessageAnime;
import imd.ufrn.model.MessageScore;
import imd.ufrn.model.MessageSyncAnime;
import imd.ufrn.model.MessageSyncClient;

public class CadastroAnimeServer2 
{
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
	
	public CadastroAnimeServer2() {
		System.out.println("Cadastro de anime server started");
		try {
			DatagramSocket serverSocket = new DatagramSocket(9031);
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
						case 3:
							cadastrarAnime(msg, receivePacket);
							break;
						case 4:
							avaliarAnime(msg);
							break;
						case 10:
							sincronize(msg);
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
	
	private void sincronize(Message msg) 
	{
		String dummy = msg.getContent();
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(dummy));
		reader.setLenient(true);
		
		MessageSyncAnime dummy2 = gson.fromJson(reader, MessageSyncAnime.class);
		
		animes = dummy2.getAnimes();
		
		System.out.println("Animes cadastrados: ");
		for (Anime e : animes) 
		{
			System.out.println(e.getName());
		}
	}
	
	private void cadastrarAnime(Message msg, DatagramPacket receivePacket) 
	{
		try {
			String dummy = msg.getContent();
		
			DatagramSocket feedbackSocket = new DatagramSocket();
		
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new StringReader(dummy));
			reader.setLenient(true);
			MessageAnime dummy2 = gson.fromJson(reader, MessageAnime.class);
		
			int userToken = dummy2.getUserToken();
			
			Message identify = new Message();
			identify.setType(11);
			identify.setContent(gson.toJson(userToken, int.class));
			
			byte[] tokenMessage = gson.toJson(identify, Message.class).getBytes();
			DatagramPacket tokenPacket = new DatagramPacket(
					tokenMessage , tokenMessage.length,
					receivePacket.getAddress(), receivePacket.getPort());
			feedbackSocket.send(tokenPacket);
			
			try {
				byte[] receiveValidateToken = new byte[1024];
				DatagramPacket tokenValidate = new DatagramPacket(receiveValidateToken , receiveValidateToken.length);
				feedbackSocket.setSoTimeout(4000);
				feedbackSocket.receive(tokenValidate);
				
				String tokenValidateMessage = new String(tokenValidate.getData());
				
				System.out.println(tokenValidateMessage);
				JsonReader leitor = new JsonReader(new StringReader(tokenValidateMessage));
				leitor.setLenient(true);
				
				Message tokenFeedback = gson.fromJson(leitor, Message.class);
				
				if(tokenFeedback.getType() == 12) 
				{
					int feedback = gson.fromJson(tokenFeedback.getContent(), int.class);
					
					if(feedback == 1) 
					{
						System.out.println("Recebi o feedback");
						Anime anm = new Anime();
						
						anm.setName(dummy2.getName());
						anm.setEpisodes(dummy2.getEpisodes());
						anm.setSummmary(dummy2.getSummary());
					
						animes.add(anm);
						
						Message feedback2 = new Message();
						MessageSyncAnime syncList = new MessageSyncAnime();
						syncList.setAnimes(animes);
						
						feedback2.setType(7);
						feedback2.setContent(gson.toJson(syncList, MessageSyncAnime.class));
						
						System.out.println("Enviando feedback para: " + receivePacket.getPort());
						
						byte[] sendMessage = gson.toJson(feedback2, Message.class).getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendMessage, sendMessage.length,
								receivePacket.getAddress(), receivePacket.getPort());
						feedbackSocket.send(sendPacket);
						
					}
					else 
					{
						Message feedback2 = new Message();
						
						feedback2.setType(13);
						
						//System.out.println("Enviando feedback para: " + receivePacket.getPort());
						
						byte[] sendMessage = gson.toJson(feedback2, Message.class).getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendMessage, sendMessage.length,
								receivePacket.getAddress(), receivePacket.getPort());
						feedbackSocket.send(sendPacket);
						System.out.println("O cliente não está logado");
					}
					
				}else 
				{
					System.out.println("Mensagem errada recebida");
				}
				
			}catch(SocketTimeoutException ex) 
			{
				System.out.println("Erro na validação de usuário");
			}
			
			
			feedbackSocket.close();
		}catch(IOException e) 
		{
			System.out.print("Erro no envio do feedback");
		}
	}
	
	public static void main(String[] args) { 
		new CadastroAnimeServer2();    
	}
}
