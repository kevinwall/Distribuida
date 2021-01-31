package imd.ufrn.servers;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import imd.ufrn.model.Cliente;
import imd.ufrn.model.Message;
import imd.ufrn.model.MessageCadastro;
import imd.ufrn.model.MessageLogin;
import imd.ufrn.model.MessageSyncClient;

public class ClienteServer 
{
	private ArrayList<Cliente> clientes = new ArrayList<Cliente>();
	private static ArrayList<Integer> validatedTokens;
	private int count = 0;
	
	public ClienteServer() {
		System.out.println("Client Server Started");
		try {
			DatagramSocket serverSocket = new DatagramSocket(9040);
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
							cadastrarUsuario(msg, receivePacket);
							break;
						case 2:
							consultarUsuario(msg, receivePacket);
							break;
						case 9:
							sincronize(msg);
							break;
						case 11:
							verifyToken(msg, receivePacket);
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
	
	private void verifyToken(Message msg, DatagramPacket receivePacket) 
	{
		System.out.println("Cheguei na verificação do servidor de usuário");
		String dummy = msg.getContent();
		
		Gson gson = new Gson();
		JsonReader reader = new JsonReader(new StringReader(dummy));
		reader.setLenient(true);
		
		int tokenVerify = gson.fromJson(reader, int.class);
		
		if(validatedTokens!=null) 
		{
			try {
				DatagramSocket feedbackSocket = new DatagramSocket();
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
							byte[] sendMessage = gson.toJson(feedback, Message.class).getBytes();
							DatagramPacket sendPacket = new DatagramPacket(
									sendMessage, sendMessage.length,
									receivePacket.getAddress(), receivePacket.getPort());
							feedbackSocket.send(sendPacket);
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
					byte[] sendMessage = gson.toJson(feedback, Message.class).getBytes();
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							receivePacket.getAddress(), receivePacket.getPort());
					try {
						feedbackSocket.send(sendPacket);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				
				feedbackSocket.close();
			} catch (SocketException e2) {
				e2.printStackTrace();
			}
			
		}
	}
	
	private void sincronize(Message msg) 
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
	
	private void cadastrarUsuario(Message msg, DatagramPacket receivePacket) 
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
			DatagramSocket feedbackSocket = new DatagramSocket();
			
			Message feedback = new Message();
			MessageSyncClient syncList = new MessageSyncClient();
			syncList.setClients(clientes);
			
			feedback.setType(5);
			feedback.setContent(gson.toJson(syncList, MessageSyncClient.class));
			
			System.out.println("Enviando feedback para: " + receivePacket.getPort());
			
			byte[] sendMessage = gson.toJson(feedback, Message.class).getBytes();
			DatagramPacket sendPacket = new DatagramPacket(
					sendMessage, sendMessage.length,
					receivePacket.getAddress(), receivePacket.getPort());
			feedbackSocket.send(sendPacket);
			
			feedbackSocket.close();
		}catch(IOException e) 
		{
			System.out.print("Erro no envio do feedback");
		}
		
	}
	
	private void consultarUsuario(Message msg, DatagramPacket receivePacket) 
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
				count += 1;
				validatedTokens.add(count);
				try {
					DatagramSocket feedbackSocket = new DatagramSocket();
					
					Message feedback = new Message();
					MessageSyncClient syncList = new MessageSyncClient();
					syncList.setClients(clientes);
					syncList.setTokens(validatedTokens);
					syncList.setToken(count);
					
					feedback.setType(6);
					feedback.setContent(gson.toJson(syncList, MessageSyncClient.class));
					
					System.out.println("Enviando feedback para: " + receivePacket.getPort());
					
					byte[] sendMessage = gson.toJson(feedback, Message.class).getBytes();
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							receivePacket.getAddress(), receivePacket.getPort());
					feedbackSocket.send(sendPacket);
					
					Message tokenCliente = new Message();
					tokenCliente.setType(6);
					tokenCliente.setContent(gson.toJson(count, int.class));
					
					byte[] sendToken = gson.toJson(tokenCliente, Message.class).getBytes();
					DatagramPacket tokenPacket = new DatagramPacket(
							sendToken, sendToken.length,
							receivePacket.getAddress(), receivePacket.getPort());
					feedbackSocket.send(tokenPacket);
					
					feedbackSocket.close();
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
