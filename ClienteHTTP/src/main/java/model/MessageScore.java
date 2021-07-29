package model;

import java.io.Serializable;

public class MessageScore implements Serializable
{
	private String name;
	private double score;
	private int userToken;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int getUserToken() {
		return userToken;
	}
	public void setUserToken(int userToken) {
		this.userToken = userToken;
	}
	
}
