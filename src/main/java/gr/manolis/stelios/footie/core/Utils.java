package gr.manolis.stelios.footie.core;

import gr.manolis.stelios.footie.api.dtos.TeamOddsDTO;
import gr.manolis.stelios.footie.core.peristence.dtos.Team;
import gr.manolis.stelios.footie.core.peristence.dtos.games.Game;
import gr.manolis.stelios.footie.core.peristence.dtos.groups.Group;
import gr.manolis.stelios.footie.core.peristence.dtos.groups.RobinGroup;
import gr.manolis.stelios.footie.core.peristence.dtos.groups.Season;
import gr.manolis.stelios.footie.core.peristence.dtos.matchups.Matchup;
import gr.manolis.stelios.footie.core.peristence.dtos.rounds.GroupsRound;
import gr.manolis.stelios.footie.core.peristence.dtos.rounds.PlayoffsRound;
import gr.manolis.stelios.footie.core.peristence.dtos.rounds.Round;
import gr.manolis.stelios.footie.core.tools.RobinGroupOrdering;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {

	public static File getDatabaseFile() {
		String footieFolder = FileUtils.getUserDirectory()  + File.separator + "footie";
		return new File(footieFolder + File.separator + "data");
	}

	public static String getBackupsFolderPath() {
		String footieFolder = FileUtils.getUserDirectory()  + File.separator + "footie";
		return footieFolder + File.separator + "backups"  + File.separator;
	}

	public static File getTeamsFile() {
		String footieFolder = FileUtils.getUserDirectory()  + File.separator + "footie";
		return new File(footieFolder + File.separator + "teams.txt");
	}

	public static File getRulesFile() {
		String footieFolder = FileUtils.getUserDirectory()  + File.separator + "footie";
		return new File(footieFolder + File.separator + "rules.txt");
	}

	public static String toString(List<?> lsObj) {
		return lsObj.stream().map(Object::toString).collect(Collectors.joining(","));
	}

	public static String toString(Set<?> lsObj) {
		return lsObj.stream().map(Object::toString).collect(Collectors.joining(","));
	}

	// half points only for quals
	public static void addGamePointsForMatchup(Group group, Matchup matchup) {
		addGamePointsForMatchup(group, matchup, false);
	}

	public static void addGamePointsForMatchup(Group group, Matchup matchup, boolean doublePoints) {
		int matchPointsHome = 0;
		int matchPointsAway = 0;
		int numberOfGames = 0;

		for (Game game : matchup.getGames()) {

			numberOfGames++;

			if (game.getResult().homeTeamWon()) {

				if (game.getHomeTeam().equals(matchup.getTeamHome())) {
					matchPointsHome += Rules.WIN_POINTS;
				} else if (game.getHomeTeam().equals(matchup.getTeamAway())) {
					matchPointsAway += Rules.WIN_POINTS;
				} else {
					System.out.println("AAAAAAAAAAAAAAAAAAAAA");
				}

			} else if (game.getResult().tie()) {

				if (game.getHomeTeam().equals(matchup.getTeamHome())) {
					matchPointsHome += Rules.DRAW_POINTS;
				} else if (game.getHomeTeam().equals(matchup.getTeamAway())) {
					matchPointsAway += Rules.DRAW_POINTS;
				} else {
					System.out.println("AAAAAAAAAAAAAAAAAAAAA");
				}

			}

			// add points for goals scored
			if(game.getHomeTeam().equals(matchup.getTeamHome())) {
				matchPointsHome += game.getResult().getGoalsMadeByHomeTeam() * Rules.GOALS_POINTS;
			}else {
				matchPointsAway += game.getResult().getGoalsMadeByHomeTeam() * Rules.GOALS_POINTS;
			}

		}

		int mult = doublePoints ? 2 : 1;

		matchup.getTeamHome().getStatsForGroup(group).addPoints(2 * mult * matchPointsHome / numberOfGames);
		matchup.getTeamAway().getStatsForGroup(group).addPoints(2 * mult * matchPointsAway / numberOfGames);
	}

	public static int getCoefficientsUntilSeason(List<Season> allSeasons, Team team, int seasonUntil) {
		List<Season> seasonsPast = allSeasons.subList(0, seasonUntil);
		Collections.reverse(seasonsPast);

		int p1 = 0;
		for(Season season : seasonsPast) {
			p1 += team.getStatsForGroup(season).getPoints();
		}

		return p1;
	}

	public static String autosave(Round round) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		String strDate = dateFormat.format(date);

		File dataFolder = Utils.getDatabaseFile();
		File backupFolder = new File(Utils.getBackupsFolderPath() + "data_autosave_" + round.getName() + "_" + strDate);

		try {
			FileUtils.copyDirectory(dataFolder, backupFolder);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return backupFolder.getName();
	}

	public static List<Team> getTeamsFromFile(Logger logger, List<Team> existingTeams) {
		logger.info("loading teams from file");

		List<Team> lsTeams = new ArrayList<>();
		try {
			File file = Utils.getTeamsFile();

			if (!file.exists()) {
				logger.error("teams file not found");
				return null;
			}

			List<String> teams = FileUtils.readLines(file, StandardCharsets.UTF_8);

			for (String tt : teams) {
				String teamName = tt.trim();
				if(StringUtils.isBlank(teamName)) {
					continue;
				}
				Team team = new Team(teamName);

				if(!existingTeams.contains(team)) {
					logger.info("adding " + teamName);
					lsTeams.add(team);
				}
			}
			lsTeams.addAll(existingTeams);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return lsTeams;
	}

	public static int[] calculateElo(int eloHome, int eloAway, int n1, int n2) {

        double homeOdds = calculateWinningOdds(eloHome, eloAway);

        // n1 -> homeTeam games played, should be matchups but oh well
        int k1 = 16;
        if(n1<10) {
        	k1 = 48;
		}else if(n1<20) {
        	k1 =32;
		}

        int k2 = 16;
        if(n2<10) {
        	k2 = 48;
		}else if(n2<20) {
        	k2 =32;
		}

	    int gainA = (int) (k1 * (1-homeOdds));
	    int lossA = (int) (k1 * (homeOdds));
        int gainB = (int) (k2 * homeOdds);
        int lossB = (int) (k2 * (1-homeOdds));

		return new int[] {gainA, -lossA, gainB, -lossB};
	}

    public static double calculateWinningOdds(int eloHome, int eloAway) {
        double e = Math.pow(10, (double)((eloAway - eloHome)) / 400);
        double odds = 1 / ( 1 + e);

        // chop down to 4 digits
        int odds4 = (int)(odds * 10000);
        double dodds = ((double) odds4) / 10000;
        return dodds;
    }

	public static void getEloForMatchup(Season season, Matchup matchup) {
		if(matchup.getWinner() != null) {
			int homeElo = matchup.getTeamHome().getStatsForGroup(season).getElo();
			int awayElo = matchup.getTeamAway().getStatsForGroup(season).getElo();
			int[] eloRatings = Utils.calculateElo(homeElo,
					awayElo,
					matchup.getTeamHome().getAllStats().getMatchesPlayed(),
					matchup.getTeamAway().getAllStats().getMatchesPlayed()
			);
			if (matchup.getTeamHome().equals(matchup.getWinner())) {
				// home win
				homeElo += eloRatings[0];
				awayElo += eloRatings[3];
			} else {
				// away win
				homeElo += eloRatings[1];
				awayElo += eloRatings[2];
			}
			matchup.getTeamHome().getStatsForGroup(season).setElo(homeElo);
			matchup.getTeamAway().getStatsForGroup(season).setElo(awayElo);
		}
	}

	public static void calcEloForGroup(List<Season> allSeasons, Season season, GroupsRound groupsRound) {
		switch(groupsRound.getNum()) {
			case 3:
				for(RobinGroup group : groupsRound.getGroups()) {
					List<Team> teams = group.getTeams();
					Collections.sort(group.getTeams(), new RobinGroupOrdering(group,
							allSeasons, group.getSeason().getSeasonYear()-1));
					eloGroupWin(season, teams.get(0), teams.get(1));
					eloGroupWin(season, teams.get(0), teams.get(2));
					eloGroupWin(season, teams.get(1), teams.get(2));
				}
				break;
			case 4:
				for(RobinGroup group : groupsRound.getGroups()) {
					List<Team> teams = group.getTeams();
					Collections.sort(group.getTeams(), new RobinGroupOrdering(group,
							allSeasons, group.getSeason().getSeasonYear()-1));
					eloGroupWin(season, teams.get(0), teams.get(1));
					eloGroupWin(season, teams.get(0), teams.get(2));
					eloGroupWin(season, teams.get(0), teams.get(3));
					eloGroupWin(season, teams.get(1), teams.get(2));
					eloGroupWin(season, teams.get(1), teams.get(3));
					eloGroupWin(season, teams.get(2), teams.get(3));
				}
				break;
		}

	}

	public static void eloGroupWin(Season season, Team team1, Team team2) {
		int homeElo = team1.getStatsForGroup(season).getElo();
		int awayElo = team2.getStatsForGroup(season).getElo();
		int[] eloRatings = Utils.calculateElo(
				homeElo,
				awayElo,
				team1.getAllStats().getMatchesPlayed(),
				team2.getAllStats().getMatchesPlayed()
		);
		homeElo += eloRatings[0];
		awayElo += eloRatings[3];
		team1.getStatsForGroup(season).setElo(homeElo);
		team2.getStatsForGroup(season).setElo(awayElo);
	}

	public static void calculateLeagueWinningOddsInPLayoffs(PlayoffsRound pr, List<TeamOddsDTO> teams) {

		Season thatSeason = pr.getSeason();

		Team a1 = pr.getgA1();
		Team b2 = pr.getgB2();
		Team a3 = pr.getgA3();
		double[] branch1 = toFinalOdds(thatSeason, a1, b2, a3);

		Team b1 = pr.getgB1();
		Team a2 = pr.getgA2();
		Team b3 = pr.getgB3();
		double[] branch2 = toFinalOdds(thatSeason, b1, a2, b3);

		// multiplied with chance to go to final
		double wxa1 = branch1[0] * toWinFinalOdds(thatSeason, a1, b1, a2, b3, branch2);
		double wxb2 = branch1[1] * toWinFinalOdds(thatSeason, b2, b1, a2, b3, branch2);
		double wxa3 = branch1[2] * toWinFinalOdds(thatSeason, a3, b1, a2, b3, branch2);

		double wxb1 = branch2[0] * toWinFinalOdds(thatSeason, b1, a1, b2, a3, branch1);
		double wxa2 = branch2[1] * toWinFinalOdds(thatSeason, a2, a1, b2, a3, branch1);
		double wxb3 = branch2[2] * toWinFinalOdds(thatSeason, b3, a1, b2, a3, branch1);

		teams.stream().filter( t -> t.getId() == a1.getId()).findFirst().get().setChances(wxa1);
		teams.stream().filter( t -> t.getId() == a2.getId()).findFirst().get().setChances(wxa2);
		teams.stream().filter( t -> t.getId() == a3.getId()).findFirst().get().setChances(wxa3);
		teams.stream().filter( t -> t.getId() == b1.getId()).findFirst().get().setChances(wxb1);
		teams.stream().filter( t -> t.getId() == b2.getId()).findFirst().get().setChances(wxb2);
		teams.stream().filter( t -> t.getId() == b3.getId()).findFirst().get().setChances(wxb3);

	}

	public static double toWinFinalOdds(Season currentSeason, Team a1, Team b1, Team a2, Team b3, double[] otherBranch) {
		// a1 chances to win each potential final
		double oddsA1wB1 = calculateWinningOdds(a1.getStatsForGroup(currentSeason).getElo(), b1.getStatsForGroup(currentSeason).getElo());
		double oddsA1wA2 = calculateWinningOdds(a1.getStatsForGroup(currentSeason).getElo(), a2.getStatsForGroup(currentSeason).getElo());
		double oddsA1wB3 = calculateWinningOdds(a1.getStatsForGroup(currentSeason).getElo(), b3.getStatsForGroup(currentSeason).getElo());
		// a1 chance to win the final
		return  oddsA1wB1*otherBranch[0] + oddsA1wA2*otherBranch[1] + oddsA1wB3*otherBranch[2];
	}

	public static double[] toFinalOdds(Season currentSeason, Team a1, Team b2, Team a3) {
		// to the semi 1
		double oddsS1 = calculateWinningOdds(b2.getStatsForGroup(currentSeason).getElo(), a3.getStatsForGroup(currentSeason).getElo());

		//a1 to the final
		double oddsF1x1 = calculateWinningOdds(a1.getStatsForGroup(currentSeason).getElo(), b2.getStatsForGroup(currentSeason).getElo());
		double oddsF1x2 = calculateWinningOdds(a1.getStatsForGroup(currentSeason).getElo(), a3.getStatsForGroup(currentSeason).getElo());
		double oddsF1xA1 = oddsS1*oddsF1x1 + (1-oddsS1)*oddsF1x2;

		//b2 to the final
		double oddsF1xB2 = oddsS1*(1-oddsF1x1);
		//a3 to the final
		double oddsF1xA3 = (1-oddsS1)*(1-oddsF1x2);

		return new double[]{ oddsF1xA1, oddsF1xB2, oddsF1xA3 };
	}
}
