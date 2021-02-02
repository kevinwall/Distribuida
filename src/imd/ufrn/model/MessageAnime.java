package imd.ufrn.model;

import java.io.Serializable;

public class MessageAnime implements Serializable
{
	private String name;
	private int episodes;
	private String summary;
	private int userToken;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getEpisodes() {
		return episodes;
	}
	public void setEpisodes(int episodes) {
		this.episodes = episodes;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public int getUserToken() {
		return userToken;
	}
	public void setUserToken(int userToken) {
		this.userToken = userToken;
	}
	
}
