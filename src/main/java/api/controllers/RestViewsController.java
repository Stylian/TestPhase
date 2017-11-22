package api.controllers;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import api.services.ViewsService;
import core.peristence.dtos.League;
import core.peristence.dtos.groups.Season;
import core.peristence.dtos.rounds.QualsRound;
import core.peristence.dtos.rounds.Round;

@RestController
@RequestMapping("/rest/views")
public class RestViewsController {

	@Autowired
  private ViewsService service;
	
	// league
  @RequestMapping("/league")
  public League getLeague(){
      return service.getLeague();
  }
  
  // season
  @RequestMapping("/season")
  public Season getSeasonCurrent(){
  	return service.getCurrentSeason();
  }
  
  @RequestMapping("/seasons")
  public List<Season> getSeasons(){
  	return service.getAllSeasons();
  }
  
  @RequestMapping("/seasons/{year}")
  public Season getSeason(@PathVariable String year){
  	return service.getSeason(NumberUtils.toInt(year));
  }
  
  // rounds
  @RequestMapping("/seasons/{year}/quals/{round}")
  public QualsRound getQualsPreview(@PathVariable String year, @PathVariable String round){

  	int qr = NumberUtils.toInt(round);
  	
  	if (qr < 1 || qr > 2) {
  		throw new NoSuchElementException("there are only 2 qualification rounds");
  	}
  	
  	Season season = service.getSeason(NumberUtils.toInt(year));
		List<Round> rounds = season.getRounds();
		QualsRound qualsRound1 = (QualsRound) rounds.get(qr - 1);
		
  	return qualsRound1;
  }

  // TODO
  
}