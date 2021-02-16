package imd.ufrn.servers;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
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

import imd.ufrn.model.Cliente;
import imd.ufrn.model.Message;
import imd.ufrn.model.MessageCadastro;
import imd.ufrn.model.MessageLogin;
import imd.ufrn.model.MessageSyncClient;

public class ClienteServer 
{
	private static ArrayList<Cliente> clientes = new ArrayList<Cliente>();
	private static ArrayList<Integer> validatedTokens;
	private static Selector selector = null;
	private static final int BUFFER_SIZE = 1024;
	private static int count = 0;
	
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
				case 1:
					cadastrarUsuario(msg, myClient);
					break;
				case 2:
					consultarUsuario(msg, myClient);
					break;
				case 9:
					sincronize(msg);
					break;
				case 11:
					verifyToken(msg, myClient);
					break;
			}
		}
		else 
		{
			System.out.print("Null message");
		}
		
		myClient.close();
		//System.out.println("Received from client: [" + msg.getContent()+ "]\nFrom: " + receivePacket.getAddress());
	}
	
	public ClienteServer() {
		System.out.println("Client Server Started");
		try {
			InetAddress hostIP= InetAddress.getLocalHost();
			int port = 9040;
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
	
	private static void verifyToken(Message msg, SocketChannel myClient) 
	{
		System.out.println("Cheguei na verificação do servidor de usuário");
		String dummy = msg.getContent();
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(dummy));
		reader.setLenient(true);
		
		int tokenVerify = gson.fromJson(reader, int.class);
		
		System.out.println("Token recebido: " + tokenVerify);
		
		if(validatedTokens!=null) 
		{
				//DatagramSocket feedbackSocket = new DatagramSocket();
				boolean flag = false;
				for (Integer e : validatedTokens) 
				{
					if(e == tokenVerify) 
					{
						flag = true;
						try {
							Message feedback = new Message();
							feedback.setType(12);
							feedback.setContent(gson.toJson(1, int.class));
							
							ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
							myBuffer.put(gson.toJson(feedback).getBytes());
							myBuffer.flip();
								
							myClient.write(myBuffer);
							
							//byte[] sendMessage = gson.toJson(feedback, Message.class).getBytes();
							//DatagramPacket sendPacket = new DatagramPacket(
							//		sendMessage, sendMessage.length,
							//		receivePacket.getAddress(), receivePacket.getPort());
							//feedbackSocket.send(sendPacket);
						}catch(IOException e1) 
						{
							System.out.println("Deu ruim");
						}
					}
				}
				
				if(!flag) 
				{
					Message feedback = new Message();
					feedback.setType(12);
					feedback.setContent(gson.toJson(2, int.class));
					
					ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
					myBuffer.put(gson.toJson(feedback).getBytes());
					myBuffer.flip();
						
					try {
						myClient.write(myBuffer);
					} catch (IOException e1) {
						System.out.println("Não consegui mandar a mensagem de feedback");
					}
					
					//byte[] sendMessage = gson.toJson(feedback, Message.class).getBytes();
					//DatagramPacket sendPacket = new DatagramPacket(
					//		sendMessage, sendMessage.length,
					//		receivePacket.getAddress(), receivePacket.getPort());
					//try {
					//	feedbackSocket.send(sendPacket);
					//} catch (IOException e1) {
					//	e1.printStackTrace();
					//}
				}
				
				//feedbackSocket.close();	
		}
	}
	
	private static void sincronize(Message msg) 
	{
		String dummy = msg.getContent();
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(dummy));
		reader.setLenient(true);
		
		MessageSyncClient dummy2 = gson.fromJson(reader, MessageSyncClient.class);
		
		clientes = dummy2.getClients();
		if(dummy2.getTokens() != null) 
		{
			validatedTokens = dummy2.getTokens();
		}
		
		System.out.println("Clientes cadastrados: ");
		for (Cliente e : clientes) 
		{
			System.out.println(e.getUsername());
		}
		
		if(validatedTokens != null) 
		{
			System.out.println("Tokens cadastrados: ");
			for (Integer e : validatedTokens) 
			{
				System.out.println(e);
			}
		}
	}
	
	private static void cadastrarUsuario(Message msg, SocketChannel myClient) 
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
		
		System.out.println("Cliente " + temp.getUsername() + " cadastrado");
		
		try {
			//DatagramSocket feedbackSocket = new DatagramSocket();
			
			Message feedback = new Message();
			MessageSyncClient syncList = new MessageSyncClient();
			syncList.setClients(clientes);
			
			feedback.setType(5);
			feedback.setContent(gson.toJson(syncList, MessageSyncClient.class));
			
			//System.out.println("Enviando feedback para: " + receivePacket.getPort());
			
			String sendMessage = gson.toJson(feedback, Message.class);
			
			ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			myBuffer.put(sendMessage.getBytes());
			myBuffer.flip();
			
			System.out.println("Mensagem de feedback: " + sendMessage);
			
			myClient.write(myBuffer);
			
			//byte[] sendMessage = gson.toJson(feedback, Message.class).getBytes();
			//DatagramPacket sendPacket = new DatagramPacket(
			//		sendMessage, sendMessage.length,
			//		receivePacket.getAddress(), receivePacket.getPort());
			//feedbackSocket.send(sendPacket);
			
			//feedbackSocket.close();
		}catch(IOException e) 
		{
			System.out.print("Erro no envio do feedback");
		}
		
	}
	
	private static void consultarUsuario(Message msg, SocketChannel myClient) 
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
				System.out.println("Usuário " + temp.getUsername() + " logado no sistema");
				count += 1;
				validatedTokens.add(count);
				
				try {
					//DatagramSocket feedbackSocket = new DatagramSocket();
					
					Message feedback = new Message();
					MessageSyncClient syncList = new MessageSyncClient();
					syncList.setClients(clientes);
					syncList.setTokens(validatedTokens);
					syncList.setToken(count);
					
					
					
					feedback.setType(6);
					feedback.setContent(gson.toJson(syncList, MessageSyncClient.class));
					
					String sendMessage = gson.toJson(feedback, Message.class);
					
					ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
					myBuffer.put(sendMessage.getBytes());
					myBuffer.flip();
					
					System.out.println("Mensagem de feedback do login: " + sendMessage);
					
					myClient.write(myBuffer);
					
					//System.out.println("Enviando feedback para: " + receivePacket.getPort());
					
					//byte[] sendMessage = gson.toJson(feedback, Message.class).getBytes();
					//DatagramPacket sendPacket = new DatagramPacket(
					//		sendMessage, sendMessage.length,
					//		receivePacket.getAddress(), receivePacket.getPort());
					//feedbackSocket.send(sendPacket);
					
					//Message tokenCliente = new Message();
					//tokenCliente.setType(6);
					//tokenCliente.setContent(gson.toJson(count, int.class));
					
					//byte[] sendToken = gson.toJson(tokenCliente, Message.class).getBytes();
					//DatagramPacket tokenPacket = new DatagramPacket(
					//		sendToken, sendToken.length,
					//		receivePacket.getAddress(), receivePacket.getPort());
					//feedbackSocket.send(tokenPacket);
					
					//feedbackSocket.close();
				}catch(IOException i) 
				{
					System.out.print("Erro no envio do feedback de login");
				}
			}
		}
	}
	
	public static void main(String[] args) { 
		validatedTokens = new ArrayList<Integer>();
		new ClienteServer();    
	}
}
