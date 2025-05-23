package cz.uhk.kpro2.controller;

import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.service.TeamService;
import cz.uhk.kpro2.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/players")
public class PlayerController {

    private final PlayerService playerService;
    private final TeamService teamService;

    @Autowired
    public PlayerController(PlayerService playerService, TeamService teamService) {
        this.playerService = playerService;
        this.teamService = teamService;
    }

    @GetMapping("/new")
    public String newPlayer(@RequestParam Long teamId, Model model) { // Changed long to Long
        if (teamId == null) { // Added null check for teamId
            return "redirect:/teams/"; // Or an error page
        }
        Team team = teamService.getTeam(teamId);
        if (team != null) {
            Player player = new Player();
            player.setTeam(team);
            model.addAttribute("player", player);
            // Add team name to the model for display purposes if needed in the form title or breadcrumbs
            model.addAttribute("teamName", team.getName());
            return "players_form";
        }
        return "redirect:/teams/";
    }

    @PostMapping("/save")
    public String savePlayer(@ModelAttribute Player player) {
        // Ensure the team is properly fetched and associated
        if (player.getTeam() != null && player.getTeam().getId() != null) { 
            Team team = teamService.getTeam(player.getTeam().getId());
            player.setTeam(team);
        } else {
            // Handle error: player must be associated with a team
            // This case should ideally be prevented by form validation or design
            return "redirect:/teams/"; // Or an error page
        }
        // The Player object 'player' now contains all fields from the form, including 'name',
        // 'position', 'jerseyNumber', 'skillLevel', and 'pointsPerGame'.
        // If player.id is null/0, it's a new player. If player.id is present, it's an update.
        playerService.savePlayer(player);
        return "redirect:/teams/" + player.getTeam().getId();
    }

    @GetMapping("/{id}/edit")
    public String editPlayer(@PathVariable Long id, Model model) { // Changed long to Long
        if (id == null) { // Added null check for id
            return "redirect:/teams/"; // Or an error page
        }
        Player player = playerService.getPlayer(id);
        if (player != null) {
            model.addAttribute("player", player);
            if (player.getTeam() != null) {
                model.addAttribute("teamName", player.getTeam().getName());
            }
            return "players_form";
        }
        return "redirect:/teams/";
    }

    @GetMapping("/{id}/delete")
    public String deletePlayer(@PathVariable Long id, Model model) { // Changed long to Long
        if (id == null) { // Added null check for id
            return "redirect:/teams/"; // Or an error page
        }
        Player player = playerService.getPlayer(id);
        if (player != null) {
            model.addAttribute("player", player);
            return "players_delete";
        }
        return "redirect:/teams/";
    }

    // This method is used by the players_delete.html form
    @PostMapping("/{id}/delete")
    public String deletePlayerPostConfirm(@PathVariable Long id) { // Changed long to Long
        if (id == null) { // Added null check for id
            return "redirect:/teams/"; // Or an error page
        }
        Player player = playerService.getPlayer(id);
        if (player != null && player.getTeam() != null && player.getTeam().getId() != null) { // Added null checks
            Long teamId = player.getTeam().getId(); // Changed long to Long
            playerService.deletePlayer(id);
            return "redirect:/teams/" + teamId;
        }
        return "redirect:/teams/";
    }

    @GetMapping("/list")
    public String listPlayers(@RequestParam Long teamId, Model model) { // Changed long to Long
        if (teamId == null) { // Added null check for teamId
            return "redirect:/teams/"; // Or an error page
        }
        Team team = teamService.getTeam(teamId);
        if (team != null) {
            model.addAttribute("team", team);
            model.addAttribute("players", team.getPlayers());
            return "players_list";
        }
        return "redirect:/teams/";
    }
}
