package gr.manolis.stelios.footie.api.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gr.manolis.stelios.footie.api.services.ViewsService;
import gr.manolis.stelios.footie.core.peristence.dtos.League;
import gr.manolis.stelios.footie.core.peristence.dtos.groups.Season;
import gr.manolis.stelios.footie.core.peristence.dtos.rounds.GroupsRound;
import gr.manolis.stelios.footie.core.peristence.dtos.rounds.PlayoffsRound;

@RestController
@RequestMapping("/rest/views")
public class RestViewsController {

	@Autowired
	private ViewsService service;

	// league
	@RequestMapping("/league")
	public League getLeague() {
		return service.getLeague();
	}

	// season
	@RequestMapping("/season")
	public Season getSeasonCurrent() {
		return service.getCurrentSeason();
	}

	@RequestMapping("/seasons")
	public List<Season> getSeasons() {
		return service.getAllSeasons();
	}

	@RequestMapping("/seasons/{year}")
	public Season getSeason(@PathVariable String year) {
		return service.getSeason(NumberUtils.toInt(year));
	}

	// @RequestMapping("/seasons/{year}/quals/{round}")
	// public QualsRound getQualsRound(@PathVariable String year, @PathVariable
	// String round) {
	//
	// int qr = NumberUtils.toInt(round);
	//
	// if (qr < 1 || qr > 2) {
	// throw new NoSuchElementException("there are only 2 qualification rounds");
	// }
	//
	// return service.getQualRound(NumberUtils.toInt(year), qr);
	// }

	@RequestMapping("/seasons/{year}/groups/{round}")
	public GroupsRound getGroupsRound(@PathVariable String year, @PathVariable String round) {

		int qr = NumberUtils.toInt(round);

		if (qr < 1 || qr > 2) {
			throw new NoSuchElementException("there are only 2 group rounds");
		}

		return service.getGroupRound(NumberUtils.toInt(year), qr);
	}

	@RequestMapping("/seasons/{year}/playoffs")
	public PlayoffsRound getPlayoffsRound(@PathVariable String year) {

		return service.getPlayoffsRound(NumberUtils.toInt(year));
	}

}