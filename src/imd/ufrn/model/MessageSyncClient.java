package imd.ufrn.model;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageSyncClient implements Serializable
{
	private ArrayList<Cliente> clients;
	private ArrayList<Integer> tokens;
	private int token;

	public ArrayList<Cliente> getClients() {
		return clients;
	}

	public void setClients(ArrayList<Cliente> clients) {
		this.clients = clients;
	}

	public ArrayList<Integer> getTokens() {
		return tokens;
	}

	public void setTokens(ArrayList<Integer> tokens) {
		this.tokens = tokens;
	}

	public int getToken() {
		return token;
	}

	public void setToken(int token) {
		this.token = token;
	}
}
