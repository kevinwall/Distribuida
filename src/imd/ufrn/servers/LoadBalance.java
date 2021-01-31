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

import imd.ufrn.model.LoadAux;
import imd.ufrn.model.Message;

public class LoadBalance 
{
	private ArrayList<LoadAux> portsClientes = new ArrayList<LoadAux>();
	private ArrayList<LoadAux> portsAnimes = new ArrayList<LoadAux>();
	
	public LoadBalance() {
		LoadAux cliente1 = new LoadAux();
		cliente1.setLoad(0);
		cliente1.setPort(9040);
		LoadAux cliente2 = new LoadAux();
		cliente2.setLoad(0);
		cliente2.setPort(9041);
		LoadAux cliente3 = new LoadAux();
		cliente3.setLoad(0);
		cliente3.setPort(9030);
		LoadAux cliente4 = new LoadAux();
		cliente4.setLoad(0);
		cliente4.setPort(9031);
		portsClientes.add(cliente1);
		portsClientes.add(cliente2);
		portsAnimes.add(cliente3);
		portsAnimes.add(cliente4);
		
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
			
			for (LoadAux i : portsClientes) 
			{
				if(i.getPort() != clientPort) 
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
								inetAddress, i.getPort());
						
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
	
	private void sincronizeAnime(DatagramPacket message, int clientPort) 
	{
		try {
			DatagramSocket sincronizeSendSocket = new DatagramSocket();
			InetAddress inetAddress = InetAddress.getByName("localhost");
			byte[] sendMessage;
			
			for (LoadAux i : portsAnimes) 
			{
				if(i.getPort() != clientPort) 
				{
					System.out.println("Entrei no if");
					try {
						String returnMessage = new String(message.getData());
						
						Gson gson = new Gson();
						JsonReader reader = new JsonReader(new StringReader(returnMessage));
						reader.setLenient(true);
						Message msg = gson.fromJson(reader, Message.class);
						msg.setType(10);
						
						sendMessage = gson.toJson(msg).getBytes();
						DatagramPacket sendPacket = new DatagramPacket(
								sendMessage, sendMessage.length,
								inetAddress, i.getPort());
						
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
			
			portsClientes.sort((LoadAux rhs, LoadAux lhs) ->
			{
				if(rhs.getLoad() < lhs.getLoad()) 
				{
					return -1;
				}else if(rhs.getLoad() == lhs.getLoad()) 
				{
					return 0;
				}
				
				return 1;
			}
					);
			
			for (LoadAux i : portsClientes) 
			{
				try {
					System.out.println("Porta do cliente: " + clientPort);
					// Sending packet
					sendMessage = message.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							inetAddress, i.getPort());
					
					i.setLoad(i.getLoad() + 1);
					
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
								sincronizeClient(receivePacket, i.getPort());
								System.out.println("Cadastro realizado com sucesso");
								flag = true;
								break;
							case 6:
								sincronizeClient(receivePacket, i.getPort());
								byte[] tokenMessage;
								tokenMessage = returnMessage.getBytes();
								DatagramPacket tokenPacket = new DatagramPacket(
										tokenMessage, tokenMessage.length,
										inetAddress, clientPort);
								redirectSendSocket.send(tokenPacket);
								flag = true;
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
		try {
			DatagramSocket redirectSendSocket = new DatagramSocket();
			//DatagramSocket redirectReciveSocket = new DatagramSocket();
			//redirectReciveSocket.setSoTimeout(4000);
			InetAddress inetAddress = InetAddress.getByName("localhost");
			byte[] sendMessage;
			
			boolean flag = false;
			
			portsAnimes.sort((LoadAux rhs, LoadAux lhs) ->
			{
				if(rhs.getLoad() < lhs.getLoad()) 
				{
					return -1;
				}else if(rhs.getLoad() == lhs.getLoad()) 
				{
					return 0;
				}
				
				return 1;
			}
					);
			
			for (LoadAux i : portsAnimes) 
			{
				try {
					//System.out.println("Porta do cliente: " + clientPort);
					// Sending packet
					sendMessage = message.getBytes();
					DatagramPacket sendPacket = new DatagramPacket(
							sendMessage, sendMessage.length,
							inetAddress, i.getPort());
					
					i.setLoad(i.getLoad() + 1);
					
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
							case 11:
								autorizeToken(receivePacket);
								byte[] sincMsg = new byte[1024];
								DatagramPacket sincPacket = new DatagramPacket(sincMsg, sincMsg.length);
								redirectSendSocket.setSoTimeout(500);
								redirectSendSocket.receive(sincPacket);
								JsonReader sincReader = new JsonReader(new StringReader(new String(sincPacket.getData())));
								sincReader.setLenient(true);
								Message sincAnimeMsg = gson.fromJson(sincReader, Message.class);
								if(sincAnimeMsg.getType() == 7) 
								{
									sincronizeAnime(sincPacket, i.getPort());
									System.out.println("Cadastro realizado com sucesso");
									flag = true;
								}else if(sincAnimeMsg.getType() == 13)
								{
									System.out.println("O usuário não está logado");
									flag = true;
								}
								break;
							case 8:
								redirectSendSocket.send(receivePacket);
								flag = true;
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
	
	private void autorizeToken(DatagramPacket message) 
	{
		portsClientes.sort((LoadAux rhs, LoadAux lhs) ->
		{
			if(rhs.getLoad() < lhs.getLoad()) 
			{
				return -1;
			}else if(rhs.getLoad() == lhs.getLoad()) 
			{
				return 0;
			}
			
			return 1;
		}
				);
		
		String temp = new String(message.getData());
		
		for (LoadAux e : portsClientes) 
		{
			try {
				DatagramSocket redirectSendSocket = new DatagramSocket();
				InetAddress inetAddress = InetAddress.getByName("localhost");
				byte[] sendMessage;
				
				sendMessage = temp.getBytes();
				
				DatagramPacket sendPacket = new DatagramPacket(
						sendMessage, sendMessage.length,
						inetAddress, e.getPort());
				
				redirectSendSocket.send(sendPacket);
				
				redirectSendSocket.setSoTimeout(2000);
				
				byte[] receiveMessage = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				
				redirectSendSocket.receive(receivePacket);
				
				byte[] lastFeedback = new String(receivePacket.getData()).getBytes();
				DatagramPacket lastPacket = new DatagramPacket(
						lastFeedback, lastFeedback.length,
						message.getAddress(), message.getPort());
				
				redirectSendSocket.send(lastPacket);
				
				redirectSendSocket.close();
				break;
			}catch(SocketTimeoutException ex) 
			{
				continue;
			}catch(IOException ex2) 
			{
				System.out.println("Falha na criação do socket");
			}
			
		}
	}
	
	public static void main(String[] args) { 
		new LoadBalance();    
	}
}
