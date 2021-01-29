package imd.ufrn;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import com.google.gson.Gson;

class UDPClient {

	public UDPClient() {
		System.out.println("UDP Client Started");
		Scanner scanner = new Scanner(System.in);
		try {
			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress inetAddress = InetAddress.getByName("localhost");
			byte[] sendMessage;
			while (true) {
				System.out.println("Enter the message type: ");
				String type = scanner.nextLine();
				
				if (type.equalsIgnoreCase("quit")) {
					break;
				}
				
				Gson gson = new Gson();
				Message msg = new Message();
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
						
						break;
					case "2":
						msg.setType(2);
						MessageLogin login = new MessageLogin();
						System.out.println("Enter the username: ");
						login.setUsuario(scanner.nextLine());
						System.out.println("Enter the password: ");
						login.setSenha(scanner.nextLine());
						

						msg.setContent(gson.toJson(login, MessageLogin.class));
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
						

						msg.setContent(gson.toJson(anime, MessageAnime.class));
						break;
					case "4":
						msg.setType(4);
						MessageScore score = new MessageScore();
						System.out.println("Enter the anime name: ");
						score.setName(scanner.nextLine());
						System.out.println("Enter the score that you want to give: ");
						score.setScore(Double.parseDouble(scanner.nextLine()));
						
						msg.setContent(gson.toJson(score, MessageScore.class));
						break;
					default:
						msg = null;
				}
					
						
				// JSON Update
				
				
				String dummy = gson.toJson(msg);
				// JSON END
				
				//System.out.println(dummy);
				
				sendMessage = dummy.getBytes();
				DatagramPacket sendPacket = new DatagramPacket(
						sendMessage, sendMessage.length,
						inetAddress, 9003);
				clientSocket.send(sendPacket);
			}
			scanner.close();
			clientSocket.close();
		} catch (IOException ex) {
		}
		System.out.println("UDP Client Terminating ");
	}

	public static void main(String args[]) {
		new UDPClient();
	}
}