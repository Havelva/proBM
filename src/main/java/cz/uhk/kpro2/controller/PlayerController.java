package cz.uhk.kpro2.controller;

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

import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.service.PlayerService;
import cz.uhk.kpro2.service.TeamService;
import jakarta.validation.Valid;

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

    // New method to list all players
    @GetMapping("/")
    public String listAllPlayers(Model model) {
        addAuthNameToModel(model);
        model.addAttribute("players", playerService.getAllPlayers());
        model.addAttribute("title", "All Players");
        return "players_overview"; // We'll create this template next
    }

    private void addAuthNameToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("authName", authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()) ? authentication.getName() : null);
    }

     // Optional: If you want a separate list of players for a team
    @GetMapping("/list")
    public String listPlayersForTeam(@RequestParam Long teamId, Model model, RedirectAttributes redirectAttributes) {
        addAuthNameToModel(model);
        Team team = teamService.getTeam(teamId);
        if (team == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Team not found.");
            return "redirect:/teams/";
        }
        model.addAttribute("team", team);
        model.addAttribute("players", playerService.getPlayersByTeamId(teamId));
        model.addAttribute("title", "Players for " + team.getName());
        return "players_list";
    }


    @GetMapping("/new")
    public String newPlayer(@RequestParam Long teamId, Model model, RedirectAttributes redirectAttributes) {
        addAuthNameToModel(model);
        Team team = teamService.getTeam(teamId);
        if (team == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Team not found. Cannot add player.");
            return "redirect:/teams/";
        }
        Player player = new Player();
        player.setTeam(team);
        model.addAttribute("player", player);
        model.addAttribute("teamName", team.getName()); // Keep this for the form
        model.addAttribute("title", "Add Player to " + team.getName()); // Title for the page
        return "players_form";
    }

    @PostMapping("/save")
    public String savePlayer(@Valid @ModelAttribute Player player, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Ensure team is still valid and re-associate if necessary
        // For a general player management, team might not be required initially,
        // or selected from a dropdown. This logic might need adjustment
        // if players can exist without a team or be assigned later.
        if (player.getTeam() == null || player.getTeam().getId() == null) {
            // If creating/editing from a general players page, team might not be pre-selected.
            // This validation might need to be conditional or handled differently.
            // For now, let's assume a player must always have a team.
             bindingResult.rejectValue("team", "error.player", "Player must be associated with a team.");
        } else {
            Team team = teamService.getTeam(player.getTeam().getId());
            if (team == null) {
                bindingResult.rejectValue("team", "error.player", "Associated team not found.");
            } else {
                player.setTeam(team); // Ensure the full team object is set
            }
        }

        if (bindingResult.hasErrors()) {
            addAuthNameToModel(model);
            // Set title and teamName for the form when returning due to errors
            if (player.getTeam() != null && player.getTeam().getId() != null) {
                 Team team = teamService.getTeam(player.getTeam().getId());
                 if(team != null) {
                    model.addAttribute("teamName", team.getName());
                    model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add Player to " + team.getName());
                 } else { // Should not happen if team.id is present, but as a fallback
                     model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add Player");
                 }
            } else {
                 // If player.getTeam() or player.getTeam().getId() is null (e.g. new player without team preselected, though current flow doesn't support this)
                 model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add Player");
            }
            // Ensure allTeams is available if editing, in case the form is extended to allow team changes
            if (player.getId() != null) {
                model.addAttribute("allTeams", teamService.getAllTeams());
            }
            return "players_form";
        }

        boolean isNew = player.getId() == null;
        playerService.savePlayer(player);
        redirectAttributes.addFlashAttribute("successMessage", "Player '" + player.getName() + "' " + (isNew ? "added to " : "updated for ") + player.getTeam().getName() + " successfully!");
        return "redirect:/teams/" + player.getTeam().getId();
    }

    @GetMapping("/{id}/edit")
    public String editPlayer(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        addAuthNameToModel(model);
        Player player = playerService.getPlayer(id);
        if (player == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Player not found.");
            return "redirect:/players/";
        }
        model.addAttribute("player", player);
        model.addAttribute("allTeams", teamService.getAllTeams()); // For potential future use (changing team)

        // Set title and teamName for the form
        if (player.getTeam() != null) {
            model.addAttribute("teamName", player.getTeam().getName());
            model.addAttribute("title", "Edit Player: " + player.getName() + " (" + player.getTeam().getName() + ")");
        } else {
            model.addAttribute("title", "Edit Player: " + player.getName());
        }
        return "players_form";
    }

    @PostMapping("/{id}/delete")
    public String deletePlayerConfirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Player player = playerService.getPlayer(id);
        if (player != null) {
            // Long teamId = player.getTeam() != null ? player.getTeam().getId() : null; // Player might not have a team
            String playerName = player.getName();
            playerService.deletePlayer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Player '" + playerName + "' deleted successfully!");
            // if (teamId != null) {
            // return "redirect:/teams/" + teamId;
            // }
            return "redirect:/players/"; // Redirect to all players list
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Player not found or could not be deleted.");
        return "redirect:/players/"; // Fallback redirect
    }
}