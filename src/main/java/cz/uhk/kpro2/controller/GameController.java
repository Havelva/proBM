package cz.uhk.kpro2.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.uhk.kpro2.model.Game;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.service.GameService;
import cz.uhk.kpro2.service.TeamService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;
    private final TeamService teamService;

    @Autowired
    public GameController(GameService gameService, TeamService teamService) {
        this.gameService = gameService;
        this.teamService = teamService;
    }

    private void addCommonAttributes(Model model) {
        List<Team> teams = teamService.getAllTeams();
        model.addAttribute("allTeams", teams);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("authName", authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()) ? authentication.getName() : null);
    }

    @GetMapping("/")
    public String listGames(Model model) {
        addCommonAttributes(model);
        model.addAttribute("upcomingGames", gameService.getUpcomingGames());
        model.addAttribute("playedGames", gameService.getPlayedGames());
        model.addAttribute("title", "Game Schedule & Results");
        return "games_list";
    }

    @GetMapping("/new")
    public String newGame(Model model) {
        addCommonAttributes(model);
        model.addAttribute("game", new Game());
        model.addAttribute("title", "Schedule New Game");
        return "games_form";
    }

    @PostMapping("/save")
    public String saveGame(@Valid @ModelAttribute("game") Game game, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        // Custom validation: Ensure home team is not the same as away team
        if (game.getHomeTeam() != null && game.getAwayTeam() != null && game.getHomeTeam().getId().equals(game.getAwayTeam().getId())) {
            bindingResult.rejectValue("awayTeam", "error.game", "Away team cannot be the same as the home team.");
        }

        // Score validation logic
        Integer homeScore = game.getHomeTeamScore();
        Integer awayScore = game.getAwayTeamScore();

        boolean homeScoreProvided = (homeScore != null);
        boolean awayScoreProvided = (awayScore != null);

        if (homeScoreProvided && homeScore < 0) {
            bindingResult.rejectValue("homeTeamScore", "error.game", "Home score cannot be negative.");
        }
        if (awayScoreProvided && awayScore < 0) {
            bindingResult.rejectValue("awayTeamScore", "error.game", "Away score cannot be negative.");
        }

        // If one score is provided, the other must also be provided
        if (homeScoreProvided && !awayScoreProvided) {
            bindingResult.rejectValue("awayTeamScore", "error.game", "Away score must be entered if home score is entered.");
        } else if (!homeScoreProvided && awayScoreProvided) {
            bindingResult.rejectValue("homeTeamScore", "error.game", "Home score must be entered if away score is entered.");
        }

        // If scores are valid and both provided, game is considered played
        // Otherwise, if no scores, it's not played (or scores are being cleared)
        if (homeScoreProvided && awayScoreProvided && homeScore >= 0 && awayScore >= 0) {
            game.setPlayed(true);
        } else if (!homeScoreProvided && !awayScoreProvided) {
            // If scores are cleared from a previously played game, mark as not played
            if (game.getId() != null) { // Only for existing games
                 Game existingGame = gameService.getGame(game.getId());
                 if (existingGame != null && existingGame.isPlayed()) {
                    game.setPlayed(false);
                 }
            } else {
                game.setPlayed(false);
            }
        }
        // If only one score is provided or scores are negative, bindingResult will have errors,
        // and the game's 'played' status might be intermediate but won't be saved if errors exist.

        if (bindingResult.hasErrors()) {
            addCommonAttributes(model);
            model.addAttribute("title", game.getId() != null ? "Edit Game" : "Schedule New Game");
            return "games_form";
        }

        gameService.saveGame(game);
        redirectAttributes.addFlashAttribute("successMessage", "Game " + (game.getId() == null || (homeScore == null && awayScore == null && !game.isPlayed()) ? "scheduled" : "updated") + " successfully!");
        return "redirect:/games/";
    }

    @GetMapping("/{id}")
    public String getGame(Model model, @PathVariable long id, RedirectAttributes redirectAttributes) {
        Game game = gameService.getGame(id);
        if (game != null) {
            model.addAttribute("game", game);
            model.addAttribute("title", "Game Details");
            return "games_detail";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Game not found.");
        return "redirect:/games/";
    }

    @GetMapping("/{id}/edit")
    public String editGame(Model model, @PathVariable long id, RedirectAttributes redirectAttributes) {
        Game game = gameService.getGame(id);
        if (game == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Game not found.");
            return "redirect:/games/";
        }
        addCommonAttributes(model);
        model.addAttribute("game", game);
        model.addAttribute("title", "Update Game / Enter Score");
        return "games_form";
    }
    
    @PostMapping("/{id}/updateScore") // This specific endpoint might become redundant if saveGame handles scores well
    public String updateScore(@PathVariable long id, 
                              @RequestParam(required = false) Integer homeTeamScore, // Make params optional
                              @RequestParam(required = false) Integer awayTeamScore,
                              RedirectAttributes redirectAttributes) {
        
        Game game = gameService.getGame(id);
        if (game == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Game not found.");
            return "redirect:/games/";
        }

        if (homeTeamScore == null || awayTeamScore == null) {
             redirectAttributes.addFlashAttribute("errorMessage", "Both home and away scores must be provided to update.");
             return "redirect:/games/" + id + "/edit"; // Redirect back to edit page
        }
        if (homeTeamScore < 0 || awayTeamScore < 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Scores must be non-negative.");
            return "redirect:/games/" + id + "/edit"; // Redirect back to edit page
        }
        
        Game updatedGame = gameService.updateScore(id, homeTeamScore, awayTeamScore); // updateScore in service sets played=true
        if (updatedGame != null) {
            redirectAttributes.addFlashAttribute("successMessage", "Score updated successfully!");
        } else {
            // This case should ideally be caught by game == null check above
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update score.");
        }
        return "redirect:/games/"; // Or redirect to game details: "redirect:/games/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteGame(@PathVariable long id, RedirectAttributes redirectAttributes) {
        Game game = gameService.getGame(id);
        if (game != null) {
            gameService.deleteGame(id);
            redirectAttributes.addFlashAttribute("successMessage", "Game deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Game not found.");
        }
        return "redirect:/games/";
    }
}