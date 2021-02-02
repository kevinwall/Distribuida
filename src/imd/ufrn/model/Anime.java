package imd.ufrn.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Anime implements Serializable
{
	private String name;
	private int episodes;
	private String summmary;
	private ArrayList<Double> scores;
	private double mScore;
	
	public Anime() 
	{
		scores = new ArrayList<Double>();
		mScore=0;
	}
	
	public void addScore(Double score) 
	{
		scores.add(score);
		
		Double soma = 0.0;
		
		for (Double e : scores) 
		{
			soma += e;
		}
		
		mScore = soma / (scores.size());
	}
	
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
	public String getSummmary() {
		return summmary;
	}
	public void setSummmary(String summmary) {
		this.summmary = summmary;
	}
	public double getMScore() {
		return mScore;
	}
	public void setMScore(double score) {
		this.mScore = score;
	}
	public ArrayList<Double> getScores() {
		return scores;
	}
	public void setScores(ArrayList<Double> scores) {
		this.scores = scores;
	}
}
