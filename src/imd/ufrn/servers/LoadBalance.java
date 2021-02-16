package imd.ufrn.servers;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
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

import imd.ufrn.model.LoadAux;
import imd.ufrn.model.Message;
import imd.ufrn.model.MessageAnime;

public class LoadBalance 
{
	private static ArrayList<LoadAux> portsClientes = new ArrayList<LoadAux>();
	private static ArrayList<LoadAux> portsAnimes = new ArrayList<LoadAux>();
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
		
		if(msg != null) 
		{
			
			int type = msg.getType();
			
			switch(type) 
			{
				case 1:
					redirectCliente(data, myClient);
					break;
				case 2:
					redirectCliente(data, myClient);
					break;
				case 3:
					redirectAnime(data, myClient);
					break;
				//case 4:
				//	redirectAnime(data);
				//	break;
			}
		}
		else 
		{
			System.out.print("Null message");
		}
		
		myClient.close();
		System.out.println("Closing Server Connection...");
	}
	
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
			InetAddress hostIP= InetAddress.getLocalHost();
			int port = 9010;
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
	
	
	private static void sincronizeClient(String data, int clientPort) 
	{
			//DatagramSocket sincronizeSendSocket = new DatagramSocket();
			//InetAddress inetAddress = InetAddress.getByName("localhost");
			//byte[] sendMessage;
			
		for (LoadAux i : portsClientes) 
		{
			if(i.getPort() != clientPort) 
			{
				//System.out.println("Entrei no if");
				try {
						//String returnMessage = new String(message.getData());
						
					Gson gson = new Gson();
					JsonReader reader = new JsonReader(new StringReader(data));
					reader.setLenient(true);
					Message msg = gson.fromJson(reader, Message.class);
					msg.setType(9);
						
						
					ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
					myBuffer.put(gson.toJson(msg).getBytes());
					myBuffer.flip();
						
					InetAddress hostIP = InetAddress.getLocalHost();
						
					InetSocketAddress myAddress = new InetSocketAddress(hostIP, i.getPort());
						
					SocketChannel myClient = SocketChannel.open(myAddress);
						
					myClient.write(myBuffer);
						//sendMessage = gson.toJson(msg).getBytes();
						//DatagramPacket sendPacket = new DatagramPacket(
						//		sendMessage, sendMessage.length,
						//		inetAddress, i.getPort());
						
						//sincronizeSendSocket.send(sendPacket);
						
					myClient.close();
						
				}catch(IOException e) 
				{
					System.out.println("Failed to send the message");
					continue;
				}
			}	
		}
		//redirectReciveSocket.close();
		//sincronizeSendSocket.close();
	}
	
	
	private static void sincronizeAnime(String data, int clientPort) 
	{
			//DatagramSocket sincronizeSendSocket = new DatagramSocket();
			//InetAddress inetAddress = InetAddress.getByName("localhost");
			//byte[] sendMessage;
			
			for (LoadAux i : portsAnimes) 
			{
				if(i.getPort() != clientPort) 
				{
					System.out.println("Entrei no if");
					try {
						//String returnMessage = new String(data.getData());
						
						Gson gson = new Gson();
						JsonReader reader = new JsonReader(new StringReader(data));
						reader.setLenient(true);
						Message msg = gson.fromJson(reader, Message.class);
						msg.setType(10);
						
						ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
						myBuffer.put(gson.toJson(msg).getBytes());
						myBuffer.flip();
							
						InetAddress hostIP = InetAddress.getLocalHost();
							
						InetSocketAddress myAddress = new InetSocketAddress(hostIP, i.getPort());
							
						SocketChannel myClient = SocketChannel.open(myAddress);
							
						myClient.write(myBuffer);
							
						myClient.close();
							
					}catch(IOException e) 
					{
						System.out.println("Failed to send the message");
					}
				}	
			}
			
			//redirectReciveSocket.close();
			//sincronizeSendSocket.close();
	}
	
	private static void redirectCliente(String message, SocketChannel myClient) 
	{
		System.out.println("Mensagem recebida do cliente: " + message);
			//DatagramSocket redirectSendSocket = new DatagramSocket();
			//DatagramSocket redirectReciveSocket = new DatagramSocket();
			//redirectReciveSocket.setSoTimeout(4000);
			//InetAddress inetAddress = InetAddress.getByName("localhost");
			//byte[] sendMessage;
			
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
					//System.out.println("Porta do cliente: " + clientPort);
					// Sending packet
					//sendMessage = message.getBytes();
					//DatagramPacket sendPacket = new DatagramPacket(
					//		sendMessage, sendMessage.length,
					//		inetAddress, i.getPort());
					
					i.setLoad(i.getLoad() + 1);
					
					System.out.println("Estou tentando enviar a mensagem para o servidor");
					ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
					myBuffer.put(message.getBytes());
					myBuffer.flip();
					
					
					InetAddress hostIP = InetAddress.getLocalHost();
					
					InetSocketAddress myAddress = new InetSocketAddress(hostIP, i.getPort());
					
					
					SocketChannel myClient2 = SocketChannel.open(myAddress);
					
					myClient2.write(myBuffer);
					System.out.println("Enviei a mensagem para o servidor");
					
					ByteBuffer myBuffer2 = ByteBuffer.allocate(BUFFER_SIZE);
					System.out.println("Estou tentando receber o feedback do servidor");
					myClient2.read(myBuffer2);
					
					String data = new String(myBuffer2.array()).trim();
					System.out.println("Recebi o feedback do servidor");
					//redirectSendSocket.send(sendPacket);
					
					//Waiting for response
					//byte[] receiveMessage = new byte[1024];
					//DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
					//redirectSendSocket.setSoTimeout(4000);
					//redirectSendSocket.receive(receivePacket);
					
					//String returnMessage = new String(receivePacket.getData());
					
					//System.out.println("Mensagem de feedback recebida: " + returnMessage);
					
					// JSON Update
					Gson gson = new Gson();
					JsonReader reader = new JsonReader(new StringReader(data));
					reader.setLenient(true);
					Message msg = gson.fromJson(reader, Message.class);
					
					//System.out.println("Mensagem que chegou ao loadBalance: " + message);
					System.out.println("Mensagem de feedback do ClientServer: " + data);
					
					if(msg != null) 
					{
						switch(msg.getType()) 
						{
							case 5:
								System.out.println("Entrei no case 5");
								sincronizeClient(data, i.getPort());
								System.out.println("Cadastro realizado com sucesso");
								flag = true;
								break;
							case 6:
								System.out.println("Entrei no case 6");
								sincronizeClient(data, i.getPort());
								System.out.println("Tentando enviar o token para o cliente: " + data);
								ByteBuffer bufferToken = ByteBuffer.allocate(BUFFER_SIZE);
								bufferToken.put(data.getBytes());
								//System.out.println("Buffer token: " + new String(bufferToken.array()).trim());
								bufferToken.flip();
								myClient.write(bufferToken);
								//myClient.
								System.out.println("Token enviado para o cliente");
								//byte[] tokenMessage;
								//tokenMessage = returnMessage.getBytes();
								//DatagramPacket tokenPacket = new DatagramPacket(
								//		tokenMessage, tokenMessage.length,
								//		inetAddress, clientPort);
								//redirectSendSocket.send(tokenPacket);
								flag = true;
								break;
						}
					}	
				
				myClient2.close();
				// i.setLoad(i.getLoad() - 1);
					
				if(flag) 
				{
					System.out.println("Breaking the balance loop in RedirectClient");
					break;
				}
				}catch(IOException e2) 
				{
					System.out.println("Trying another server...");
					continue;
				}
			}
			
			//redirectReciveSocket.close();
			//redirectSendSocket.close();
	}
	
	private static void redirectAnime(String message, SocketChannel myClient) 
	{
		System.out.println("Mensagem recebida do cliente: " + message);
			//DatagramSocket redirectSendSocket = new DatagramSocket();
			//DatagramSocket redirectReciveSocket = new DatagramSocket();
			//redirectReciveSocket.setSoTimeout(4000);
			//InetAddress inetAddress = InetAddress.getByName("localhost");
			//byte[] sendMessage;
			
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
		
			boolean flag = false;
			
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new StringReader(message));
			reader.setLenient(true);
			Message dummy = gson.fromJson(reader, Message.class);
		
			JsonReader dummyReader = new JsonReader(new StringReader(dummy.getContent()));
			dummyReader.setLenient(true);
			MessageAnime dummy2 = gson.fromJson(dummyReader, MessageAnime.class);
			
			int userToken = dummy2.getUserToken();
			
			System.out.println("Token recebido: " + userToken);
			
			Message identify = new Message();
			identify.setType(11);
			identify.setContent(gson.toJson(userToken, int.class));
			
			//ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			//myBuffer.put(gson.toJson(identify, Message.class).getBytes());
			//myBuffer.flip();
			
			//myClient.write(myBuffer);
			
			String resposta = autorizeToken(gson.toJson(identify, Message.class));
			
			System.out.println(resposta);
			JsonReader leitor = new JsonReader(new StringReader(resposta));
			leitor.setLenient(true);
			
			Message tokenFeedback = gson.fromJson(leitor, Message.class);
			
			if(tokenFeedback.getType() == 12) 
			{
				int feedback = gson.fromJson(tokenFeedback.getContent(), int.class);
				if(feedback == 1) 
				{
					for (LoadAux i : portsAnimes) 
					{
						try {
							//System.out.println("Porta do cliente: " + clientPort);
							// Sending packet
							//sendMessage = message.getBytes();
							//DatagramPacket sendPacket = new DatagramPacket(
							//		sendMessage, sendMessage.length,
							//		inetAddress, i.getPort());
							
							i.setLoad(i.getLoad() + 1);
							
							System.out.println("Estou tentando enviar a mensagem para o servidor");
							ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
							myBuffer.put(message.getBytes());
							myBuffer.flip();
							
							
							InetAddress hostIP = InetAddress.getLocalHost();
							
							InetSocketAddress myAddress = new InetSocketAddress(hostIP, i.getPort());
							
							
							SocketChannel myClient2 = SocketChannel.open(myAddress);
							
							myClient2.write(myBuffer);
							System.out.println("Enviei a mensagem para o servidor");
							
							ByteBuffer myBuffer2 = ByteBuffer.allocate(BUFFER_SIZE);
							System.out.println("Estou tentando receber o feedback do servidor");
							myClient2.read(myBuffer2);
							
							String data = new String(myBuffer2.array()).trim();
							System.out.println("Recebi o feedback do servidor");
							//redirectSendSocket.send(sendPacket);
							
							//Waiting for response
							//byte[] receiveMessage = new byte[1024];
							//DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
							//redirectSendSocket.setSoTimeout(4000);
							//redirectSendSocket.receive(receivePacket);
							
							//String returnMessage = new String(receivePacket.getData());
							
							//System.out.println("Mensagem de feedback recebida: " + returnMessage);
							
							// JSON Update
							//Gson gson = new Gson();
							JsonReader reader2 = new JsonReader(new StringReader(data));
							reader2.setLenient(true);
							Message msg = gson.fromJson(reader2, Message.class);
							
							//System.out.println("Mensagem que chegou ao loadBalance: " + message);
							System.out.println("Mensagem de feedback do ClientServer: " + data);
							
							if(msg != null) 
							{
								switch(msg.getType()) 
								{
								case 7:
									sincronizeAnime(data, i.getPort());
									System.out.println("Cadastro realizado com sucesso");
									flag = true;
									//if(resposta != null) 
									//{
									//	ByteBuffer bufferResposta = ByteBuffer.allocate(BUFFER_SIZE);
									//	bufferResposta.put(resposta.getBytes());
									//	bufferResposta.flip();
										
									//	System.out.println("Buffer da resposta do cliente para o anime: " + resposta);
										
									//	try {
									//		myClient2.write(bufferResposta);
									//	}catch(IOException k) 
									//	{
									//		k.printStackTrace();
									//		System.out.println("Deu ruim no write do feedback");
									//	}
										
										
									//	System.out.println("Enviei a mensagem para o anime");
										
									//	ByteBuffer bufferFeedback = ByteBuffer.allocate(BUFFER_SIZE);
									//	myClient2.read(bufferFeedback);
									//	String response = new String(bufferFeedback.array()).trim();
										
									//	JsonReader sincReader = new JsonReader(new StringReader(response));
									//	sincReader.setLenient(true);
									//	Message sincAnimeMsg = gson.fromJson(sincReader, Message.class);
										
									//	if(sincAnimeMsg.getType() == 7) 
									//	{
											
									//	}else if(sincAnimeMsg.getType() == 13)
									//	{
									//		System.out.println("O usuário não está logado");
									//		flag = true;
									//	}
										break;
										//System.out.println("Recebi o feedback do servidor");
									}
								}	
						
						myClient2.close();
						// i.setLoad(i.getLoad() - 1);
							
						if(flag) 
						{
							System.out.println("Breaking the balance loop in RedirectClient");
							break;
						}
						}catch(IOException e2) 
						{
							System.out.println("Trying another server...");
							continue;
						}
					}
				}
				else 
				{
					System.out.println("O usuário não está logado");
				}
			}
			else 
			{
				System.out.println("Erro na verificação do token");
			}
	}
	
	/*
	private static void redirectAnime(String message) 
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
						}
					}	
					
				// i.setLoad(i.getLoad() - 1);
					
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
	*/
	
	private static String autorizeToken(String data) 
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
		
		//String temp = new String(message.getData());
		
		for (LoadAux e : portsClientes) 
		{
			try {
				//DatagramSocket redirectSendSocket = new DatagramSocket();
				//InetAddress inetAddress = InetAddress.getByName("localhost");
				//byte[] sendMessage;
				
				//sendMessage = temp.getBytes();
				
				//DatagramPacket sendPacket = new DatagramPacket(
				//		sendMessage, sendMessage.length,
				//		inetAddress, e.getPort());
				
				//redirectSendSocket.send(sendPacket);
				
				//redirectSendSocket.setSoTimeout(2000);
				
				//System.out.println("Estou tentando enviar a mensagem para o servidor");
				ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
				myBuffer.put(data.getBytes());
				myBuffer.flip();
				
				
				InetAddress hostIP = InetAddress.getLocalHost();
				
				InetSocketAddress myAddress = new InetSocketAddress(hostIP, e.getPort());
				
				
				SocketChannel myClient2 = SocketChannel.open(myAddress);
				
				myClient2.write(myBuffer);
				System.out.println("Enviei a mensagem para o servidor");
				
				//byte[] receiveMessage = new byte[1024];
				//DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
				
				//redirectSendSocket.receive(receivePacket);
				
				ByteBuffer bufferAut = ByteBuffer.allocate(BUFFER_SIZE);
				myClient2.read(bufferAut);
				String retornoAut = new String(bufferAut.array()).trim();
				
				myClient2.close();
				
				return retornoAut;
				
				//byte[] lastFeedback = new String(receivePacket.getData()).getBytes();
				//DatagramPacket lastPacket = new DatagramPacket(
				//		lastFeedback, lastFeedback.length,
				//		message.getAddress(), message.getPort());
				
				//redirectSendSocket.send(lastPacket);
				
				//redirectSendSocket.close();
			}catch(IOException ex2) 
			{
				System.out.println("Tentando outro servidor...");
				continue;
			}
			
		}
		
		return null;
	}
	
	public static void main(String[] args) { 
		new LoadBalance();    
	}
}
