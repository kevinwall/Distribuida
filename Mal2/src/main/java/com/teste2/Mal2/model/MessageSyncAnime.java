package com.teste2.Mal2.model;

import java.io.Serializable;
import java.util.ArrayList;

public class MessageSyncAnime implements Serializable
{
	private ArrayList<Anime> animes;

	public ArrayList<Anime> getAnimes() 
	{
		return animes;
	}

	public void setAnimes(ArrayList<Anime> animes) 
	{
		this.animes = animes;
	}
}
