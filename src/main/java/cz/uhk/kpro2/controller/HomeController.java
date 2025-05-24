package cz.uhk.kpro2.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import cz.uhk.kpro2.dto.TeamStandingDto;
import cz.uhk.kpro2.model.Game;
import cz.uhk.kpro2.service.GameService;
import cz.uhk.kpro2.service.TeamService;

@Controller
public class HomeController {

    private final GameService gameService;
    private final TeamService teamService;

    @Autowired
    public HomeController(GameService gameService, TeamService teamService) {
        this.gameService = gameService;
        this.teamService = teamService;
    }

    private void addAuthNameToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("authName", authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()) ? authentication.getName() : null);
    }

    @GetMapping({"/home", "/"})
    public String home(Model model) {
        addAuthNameToModel(model);
        model.addAttribute("title", "Welcome");

        // Fetch data for the dashboard
        // Get top 3 upcoming games
        List<Game> upcomingGames = gameService.getUpcomingGames().stream().limit(3).collect(Collectors.toList());
        model.addAttribute("upcomingGames", upcomingGames);

        // Get top 3 teams from standings
        List<TeamStandingDto> topTeams = teamService.getTeamStandings().stream().limit(3).collect(Collectors.toList());
        model.addAttribute("topTeams", topTeams);

        return "home";
    }
}