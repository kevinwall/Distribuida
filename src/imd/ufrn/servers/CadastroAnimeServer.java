package imd.ufrn.servers;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import imd.ufrn.model.Anime;
import imd.ufrn.model.Cliente;
import imd.ufrn.model.Message;
import imd.ufrn.model.MessageAnime;
import imd.ufrn.model.MessageScore;
import imd.ufrn.model.MessageSyncAnime;
import imd.ufrn.model.MessageSyncClient;

public class CadastroAnimeServer 
{
	private static ArrayList<Anime> animes = new ArrayList<Anime>();
	private static Selector selector = null;
	private static final int BUFFER_SIZE = 1024;
	
	private static void processAcceptEvent(ServerSocketChannel mySocket,
			SelectionKey key) throws IOException 
	{
		System.out.println("Connection Accepted...");
		// Accept the connection and make it non-blocking
		SocketChannel myClient = mySocket.accept();
		myClient.configureBlocking(false);
		// Register interest in reading this channel
		myClient.register(selector, SelectionKey.OP_READ);
	}
	
	private static void processReadEvent(SelectionKey key) throws IOException 
	{
		System.out.println("Inside processReadEvent...");
		// create a ServerSocketChannel to read the request
		SocketChannel myClient = (SocketChannel) key.channel();
		// Set up out 1k buffer to read data into
		ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		myClient.read(myBuffer);
		String data = new String(myBuffer.array()).trim();
	
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(data));
		reader.setLenient(true);
		Message msg = gson.fromJson(reader, Message.class);
	
		//byte[] receiveMessage = new byte[1024];
		//DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
		//serverSocket.receive(receivePacket);
		//String message = new String(receivePacket.getData());
	
		// JSON Update
		//Gson gson = new Gson();
		//JsonReader reader = new JsonReader(new StringReader(message));
		//reader.setLenient(true);
		//Message msg = gson.fromJson(reader, Message.class);
		// END
	
		if(msg != null) 
		{
			int type = msg.getType();
		
			switch(type) 
			{
				case 3:
					cadastrarAnime(msg, myClient);
					break;
				//case 4:
				//	avaliarAnime(msg, receivePacket);
				//	break;
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
	
			myClient.close();
		//System.out.println("Received from client: [" + msg.getContent()+ "]\nFrom: " + receivePacket.getAddress());
	}	
	public CadastroAnimeServer() {
		System.out.println("Cadastro de anime server started");
		try {
			InetAddress hostIP= InetAddress.getLocalHost();
			int port = 9030;
			selector = Selector.open();
			ServerSocketChannel mySocket = ServerSocketChannel.open();
			ServerSocket serverSocket = mySocket.socket();
			InetSocketAddress address = new InetSocketAddress(hostIP, port);
			serverSocket.bind(address);
			mySocket.configureBlocking(false);
			mySocket.register(selector,SelectionKey.OP_ACCEPT);
			
			while (true) {
				
				selector.select();
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> i = selectedKeys.iterator();
				
				while (i.hasNext()) 
				{
					SelectionKey key = i.next();
					if (key.isAcceptable()) 
					{
						processAcceptEvent(mySocket, key);
					} 
					else if (key.isReadable()) 
					{
						processReadEvent(key);
					}
					i.remove();
				}
			}	
		}catch (IOException e) {
				e.printStackTrace();
				System.out.println("UDP Server Terminating");		
		}
	}
	
	/*
	private void avaliarAnime(Message msg, DatagramPacket receivePacket) 
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
						String msgAval = msg.getContent();
						
						JsonReader leitorAval = new JsonReader(new StringReader(msgAval));
						leitorAval.setLenient(true);
						MessageScore avaliacao = gson.fromJson(leitorAval, MessageScore.class);
						
						for (Anime e : animes) 
						{
							if(e.getName().contentEquals(dummy2.getName())) 
							{
								e.addScore(avaliacao.getScore());
								System.out.println("O anime " + e.getName() + " está com a média " + e.getMScore());
								break;
							}
						}
						
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
	*/
	private static void sincronize(Message msg) 
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
	
	private static void cadastrarAnime(Message msg, SocketChannel myClient) 
	{
		try {
			String dummy = msg.getContent();
		
			//DatagramSocket feedbackSocket = new DatagramSocket();
		
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new StringReader(dummy));
			reader.setLenient(true);
			MessageAnime dummy2 = gson.fromJson(reader, MessageAnime.class);
		
			int userToken = dummy2.getUserToken();
			
			Message identify = new Message();
			identify.setType(11);
			identify.setContent(gson.toJson(userToken, int.class));
			
			ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			myBuffer.put(gson.toJson(identify, Message.class).getBytes());
			myBuffer.flip();
			
			myClient.write(myBuffer);
			
			//byte[] tokenMessage = gson.toJson(identify, Message.class).getBytes();
			//DatagramPacket tokenPacket = new DatagramPacket(
			//		tokenMessage , tokenMessage.length,
			//		receivePacket.getAddress(), receivePacket.getPort());
			//feedbackSocket.send(tokenPacket);
			
			try {
				//byte[] receiveValidateToken = new byte[1024];
				//DatagramPacket tokenValidate = new DatagramPacket(receiveValidateToken , receiveValidateToken.length);
				//feedbackSocket.setSoTimeout(4000);
				//feedbackSocket.receive(tokenValidate);
				
				System.out.println("Estou esperando o feedback do servidor de validaçao");
				ByteBuffer buffer2 = ByteBuffer.allocate(BUFFER_SIZE);
				myClient.read(buffer2);
				System.out.println("Buffer: " + buffer2.toString());
				System.out.println("Recebi o feedback do servidor de validação " + new String(buffer2.array()).trim());
				
				String tokenValidateMessage = new String(buffer2.array()).trim();
				
				//String tokenValidateMessage = new String(tokenValidate.getData());
				
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
						
						//System.out.println("Enviando feedback para: " + receivePacket.getPort());
						
						//byte[] sendMessage = gson.toJson(feedback2, Message.class).getBytes();
						//DatagramPacket sendPacket = new DatagramPacket(
						//		sendMessage, sendMessage.length,
						//		receivePacket.getAddress(), receivePacket.getPort());
						//feedbackSocket.send(sendPacket);
						
						ByteBuffer sendBuffer = ByteBuffer.allocate(BUFFER_SIZE);
						sendBuffer.put(gson.toJson(feedback2, Message.class).getBytes());
						sendBuffer.flip();
						
						myClient.write(sendBuffer);
					}
					else 
					{
						Message feedback2 = new Message();
						
						feedback2.setType(13);
						
						//System.out.println("Enviando feedback para: " + receivePacket.getPort());
						
						//byte[] sendMessage = gson.toJson(feedback2, Message.class).getBytes();
						//DatagramPacket sendPacket = new DatagramPacket(
						//		sendMessage, sendMessage.length,
						//		receivePacket.getAddress(), receivePacket.getPort());
						//feedbackSocket.send(sendPacket);
						
						ByteBuffer sendBuffer = ByteBuffer.allocate(BUFFER_SIZE);
						sendBuffer.put(gson.toJson(feedback2, Message.class).getBytes());
						sendBuffer.flip();
						
						myClient.write(sendBuffer);
						
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
			
			
			//feedbackSocket.close();
		}catch(IOException e) 
		{
			System.out.print("Erro no envio do feedback");
		}
	}
	
	public static void main(String[] args) { 
		new CadastroAnimeServer();    
	}
}
