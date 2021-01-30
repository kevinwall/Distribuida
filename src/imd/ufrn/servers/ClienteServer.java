package imd.ufrn.servers;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import imd.ufrn.model.Cliente;
import imd.ufrn.model.Message;
import imd.ufrn.model.MessageCadastro;
import imd.ufrn.model.MessageLogin;

public class ClienteServer 
{
	private ArrayList<Cliente> clientes = new ArrayList<Cliente>();
	
	public ClienteServer() {
		System.out.println("Client Server Started");
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
	
	public static void main(String[] args) { 
		new ClienteServer();    
	}
}
