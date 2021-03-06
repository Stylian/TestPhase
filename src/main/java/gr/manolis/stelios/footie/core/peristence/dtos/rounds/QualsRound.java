package gr.manolis.stelios.footie.core.peristence.dtos.rounds;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gr.manolis.stelios.footie.core.peristence.dtos.Team;
import gr.manolis.stelios.footie.core.peristence.dtos.games.Game;
import gr.manolis.stelios.footie.core.peristence.dtos.groups.Season;
import gr.manolis.stelios.footie.core.peristence.dtos.matchups.Matchup;

@Entity(name = "ROUNDS_QUALSROUNDS")
public class QualsRound extends Round {

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "round")
	private List<Matchup> matchups = new ArrayList<>();

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "ROUNDS_QUALSROUNDS_STRONG_TEAMS")
	private List<Team> strongTeams;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "ROUNDS_QUALSROUNDS_WEAK_TEAMS")
	private List<Team> weakTeams;

	public QualsRound() {
	}

	public QualsRound(Season season, String name, int num) {
		super(season, name, num);
	}

	public void addMatchup(Matchup matchup) {
		this.matchups.add(matchup);
	}

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	public List<Matchup> getMatchups() {
		return matchups;
	}

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	public List<Team> getStrongTeams() {
		return strongTeams;
	}

	public void setStrongTeams(List<Team> strongTeams) {
		this.strongTeams = strongTeams;
	}

	@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
	public List<Team> getWeakTeams() {
		return weakTeams;
	}

	public void setWeakTeams(List<Team> weakTeams) {
		this.weakTeams = weakTeams;
	}
	
	@Override
	public List<Game> getGames() {
		List<Game> games = new ArrayList<>();
		matchups.stream().forEach( m -> games.addAll(m.getGames())); 
		return games;
	}

	@Override
	public String toString() {
		return "QualsRound [matchups=" + matchups.size() + ", strongTeams=" + strongTeams.size() +", weakTeams=" + weakTeams.size()  + "]";
	}

}
