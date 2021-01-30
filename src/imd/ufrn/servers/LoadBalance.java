package imd.ufrn.servers;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import imd.ufrn.model.Message;

public class LoadBalance 
{
	private ArrayList<Integer> portsClientes = new ArrayList<Integer>();
	private ArrayList<Integer> portsAnimes = new ArrayList<Integer>();
	
	public LoadBalance() {
		portsClientes.add(9040);
		portsClientes.add(9041);
		portsAnimes.add(9004);
		System.out.println("Load Balance Started");
		try {
			DatagramSocket serverSocket = new DatagramSocket(9010);
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
							redirectCliente(message, receivePacket.getPort());
							break;
						case 2:
							redirectCliente(message, receivePacket.getPort());
							break;
						case 3:
							redirectAnime(message);
							break;
						case 4:
							redirectAnime(message);
							break;
					}
				}
				else 
				{
					System.out.print("Null message");
				}
			}
		}catch (IOException e) {
				e.printStackTrace();
				System.out.println("UDP Server Terminating");		
		}
	}
	
	private void sincronizeClient(DatagramPacket message, int clientPort) 
	{
		try {
			DatagramSocket sincronizeSendSocket = new DatagramSocket();
			InetAddress inetAddress = InetAddress.getByName("localhost");
			byte[] sendMessage;
			
			for (Integer i : portsClientes) 
			{
				if(i != clientPort) 
				{
					System.out.println("Entrei no if");
					try {
						String returnMessage = new String(message.getData());
						
						Gson gson = new Gson();
						JsonReader reader = new JsonReader(new StringReader(returnMessage));
						reader.setLenient(true);
						Message msg = gson.fromJson(reader, Message.class);
						msg.setType(9);
						
						sendMessage = gson.toJson(msg).getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendMessage, sendMessage.length,
								inetAddress, i);
						
						sincronizeSendSocket.send(sendPacket);
							
					}catch(IOException e) 
					{
						System.out.println("Failed to send the message");
					}
				}	
			}
			
			//redirectReciveSocket.close();
			sincronizeSendSocket.close();
		}catch(IOException e) 
		{
			System.out.println("Failed to connect");
		}
	}
	
	private void redirectCliente(String message, int clientPort) 
	{
		try {
			DatagramSocket redirectSendSocket = new DatagramSocket();
			//DatagramSocket redirectReciveSocket = new DatagramSocket();
			//redirectReciveSocket.setSoTimeout(4000);
			InetAddress inetAddress = InetAddress.getByName("localhost");
			byte[] sendMessage;
			
			boolean flag = false;
			for (Integer i : portsClientes) 
			{
				try {
					System.out.println("Porta do cliente: " + clientPort);
					// Sending packet
					sendMessage = message.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							inetAddress, i);
					
					redirectSendSocket.send(sendPacket);
					
					//Waiting for response
					byte[] receiveMessage = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
					redirectSendSocket.setSoTimeout(4000);
					redirectSendSocket.receive(receivePacket);
					
					String returnMessage = new String(receivePacket.getData());
					
					System.out.println("Mensagem de feedback recebida: " + returnMessage);
					
					// JSON Update
					Gson gson = new Gson();
					JsonReader reader = new JsonReader(new StringReader(returnMessage));
					reader.setLenient(true);
					Message msg = gson.fromJson(reader, Message.class);
					
					if(msg != null) 
					{
						switch(msg.getType()) 
						{
							case 5:
								sincronizeClient(receivePacket, i);
								System.out.println("Cadastro realizado com sucesso");
								flag = true;
								break;
							case 6:
								// Redirecting the token to the client
								redirectSendSocket.send(receivePacket);
								flag = true;
								//DatagramSocket responseTokenSocket = new DatagramSocket();
								
								//byte[] responseClientMessage = returnMessage.getBytes();
								//DatagramPacket responseClientPacket = new DatagramPacket(responseClientMessage, 
								//		responseClientMessage.length,
								//		receivePacket.getAddress(), receivePacket.getPort());
								
								//responseTokenSocket.send(responseClientPacket);
								//responseTokenSocket.close();
								break;
						}
					}	
				if(flag) 
				{
					break;
				}
				}catch(SocketTimeoutException e) 
				{
					System.out.println("Trying another server...");
					continue;
				}
			}
			
			//redirectReciveSocket.close();
			redirectSendSocket.close();
		}catch(IOException e) 
		{
			System.out.println("Failed to connect");
		}
	}
	
	private void redirectAnime(String message) 
	{
		
	}
	
	public static void main(String[] args) { 
		new LoadBalance();    
	}
}
