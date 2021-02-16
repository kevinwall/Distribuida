package imd.ufrn.servers;

import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import imd.ufrn.model.Message;
import imd.ufrn.model.MessageAnime;
import imd.ufrn.model.MessageCadastro;
import imd.ufrn.model.MessageLogin;
import imd.ufrn.model.MessageScore;
import imd.ufrn.model.MessageSyncClient;

class UDPClient {

	private int token;
	private ArrayList<Integer> lbs = new ArrayList<Integer>();
	private static final int BUFFER_SIZE = 1024;
	
	private void sendMessage(Message msg) 
	{
		boolean flag = false;
		Gson gson = new Gson();
		String message = gson.toJson(msg, Message.class);
		
		for (Integer e : lbs) 
		{
			InetAddress hostIP = null;
			try {
				hostIP = InetAddress.getLocalHost();
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			InetSocketAddress myAddress = new InetSocketAddress(hostIP, e);
			
			SocketChannel myClient = null;
			try {
				myClient = SocketChannel.open(myAddress);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			ByteBuffer myBuffer = ByteBuffer.allocate(BUFFER_SIZE);
			myBuffer.put(message.getBytes());
			myBuffer.flip();
			
			try {
				myClient.write(myBuffer);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			if(msg.getType() == 2) 
			{
				ByteBuffer bufferToken = ByteBuffer.allocate(BUFFER_SIZE);
				
				try {
					myClient.read(bufferToken);
					//bufferToken.flip();
					System.out.println("BufferToken: " + bufferToken.toString());
					String data = new String(bufferToken.array()).trim();
					
					System.out.println("Mensagem recebida do LB: " + data);
					
					Message tokenMsg = gson.fromJson(data, Message.class);
						
					if(tokenMsg != null) 
					{
						MessageSyncClient msgToken = gson.fromJson(tokenMsg.getContent(), MessageSyncClient.class);
						token = msgToken.getToken();
						System.out.println("O token recebido foi: " + token);
					}
					}catch (IOException e2) 
					{
						System.out.println("Não recebi o feedback do token");
					}	
			}
			
			try {
				myClient.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public UDPClient() {
		System.out.println("UDP Client Started");
		lbs.add(9010);
		//lbs.add(9011);
		Scanner scanner = new Scanner(System.in);
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress inetAddress = InetAddress.getByName("localhost");
			while (true) {
				System.out.println("Enter the message type: ");
				String type = scanner.nextLine();
				
				if (type.equalsIgnoreCase("quit")) {
					break;
				}
				
				Message msg = new Message();
				Gson gson = new Gson();
				//String dummy;
				
				switch(type) 
				{
					case "1":
						msg.setType(1);
						MessageCadastro cadastro = new MessageCadastro();
						System.out.println("Enter the username: ");
						cadastro.setUsuario(scanner.nextLine());
						//System.out.println("Username: " +username);
						System.out.println("Enter the password: ");
						cadastro.setSenha(scanner.nextLine());
						//System.out.println("Username: " +password);
						
						msg.setContent(gson.toJson(cadastro, MessageCadastro.class));
						//port = 9003;
						//dummy = gson.toJson(msg);
						
						sendMessage(msg);
						
						break;
					case "2":
						msg.setType(2);
						MessageLogin login = new MessageLogin();
						System.out.println("Enter the username: ");
						login.setUsuario(scanner.nextLine());
						System.out.println("Enter the password: ");
						login.setSenha(scanner.nextLine());
						

						msg.setContent(gson.toJson(login, MessageLogin.class));
						//port = 9003;
						//dummy = gson.toJson(msg);
						
						sendMessage(msg);
						
						//byte[] receiveMessage = new byte[1024];
						//DatagramPacket receivePacket = new DatagramPacket(receiveMessage, receiveMessage.length);
						
						//try {
						//	clientSocket.setSoTimeout(6000);
						//	clientSocket.receive(receivePacket);
						//}catch(SocketTimeoutException e) 
						//{
						//	System.out.println("Pacote não recebido...");
						//}
						//String recivedPacket = new String(receivePacket.getData());
						//JsonReader reader = new JsonReader(new StringReader(recivedPacket));
						//reader.setLenient(true);
						//Message tokenMsg = gson.fromJson(reader, Message.class);
						
						//if(tokenMsg.getType() == 6) 
						//{
						//	MessageSyncClient msgToken = gson.fromJson(tokenMsg.getContent(), MessageSyncClient.class);
						//	token = msgToken.getToken();
						//	System.out.println("O token recebido foi: " + token);
						//}
						//else 
						//{
						//	System.out.println("Error recieving token...");
						//}
						
						break;
					case "3":
						msg.setType(3);
						MessageAnime anime = new MessageAnime();
						System.out.println("Enter the anime name: ");
						anime.setName(scanner.nextLine());
						System.out.println("Enter the quantity of episodes: ");
						anime.setEpisodes(Integer.parseInt(scanner.nextLine()));
						System.out.println("Enter a brief summary of the anime");
						anime.setSummary(scanner.nextLine());
						anime.setUserToken(token);
						

						msg.setContent(gson.toJson(anime, MessageAnime.class));
						//port = 9004;
						//dummy = gson.toJson(msg);
						
						sendMessage(msg);
						break;
					case "4":
						msg.setType(4);
						MessageScore score = new MessageScore();
						System.out.println("Enter the anime name: ");
						score.setName(scanner.nextLine());
						System.out.println("Enter the score that you want to give: ");
						score.setScore(Double.parseDouble(scanner.nextLine()));
						score.setUserToken(token);
						
						msg.setContent(gson.toJson(score, MessageScore.class));
						//port = 9004;
						//dummy = gson.toJson(msg);
						
						sendMessage(msg);
						break;
					default:
						msg = null;
						//port = 9000;
				}
			}
			scanner.close();
			clientSocket.close();
		} catch (IOException ex) 
		{
			
		}
		System.out.println("UDP Client Terminating ");
	}

	public static void main(String args[]) {
		new UDPClient();
	}
}