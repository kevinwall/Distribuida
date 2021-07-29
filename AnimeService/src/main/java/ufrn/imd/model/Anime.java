package ufrn.imd.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Animes")
public class Anime implements Serializable
{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	private String name;
	private int episodes;
	private String summmary;
	
	@ElementCollection
	private List<Double> scores;

	private double mScore;
	
	public Anime() 
	{
		scores = new ArrayList<Double>();
		mScore=0;
	}
	
	public long getId() {
		return id;
	}



	public void setId(long id) {
		this.id = id;
	}



	public double getmScore() {
		return mScore;
	}



	public void setmScore(double mScore) {
		this.mScore = mScore;
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
	public List<Double> getScores() {
		return scores;
	}
	public void setScores(ArrayList<Double> scores) {
		this.scores = scores;
	}
}
