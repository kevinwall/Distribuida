package com.teste2.Mal2.model;

import java.io.Serializable;

public class LoadAux implements Serializable
{
	private int load;
	private int port;
	
	public int getLoad() 
	{
		return load;
	}
	public void setLoad(int load) 
	{
		this.load = load;
	}
	public int getPort() 
	{
		return port;
	}
	public void setPort(int port) 
	{
		this.port = port;
	}
}
