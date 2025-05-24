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
        model.addAttribute("allTeams", teamService.getAllTeams()); // Add all teams for consistency, though not used when teamId is present
        return "players_form";
    }

    // New GET mapping for adding a player from the general players overview page
    @GetMapping("/new_general")
    public String newPlayerGeneral(Model model) {
        addAuthNameToModel(model);
        Player player = new Player();
        model.addAttribute("player", player);
        model.addAttribute("allTeams", teamService.getAllTeams()); // For selecting a team
        model.addAttribute("title", "Add New Player");
        // teamName will be null here, players_form.html will need to handle this
        return "players_form";
    }

    @PostMapping("/save")
    public String savePlayer(@Valid @ModelAttribute Player player, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Team handling:
        // If player.team.id is null, it means we are likely coming from new_general
        // and team selection should have happened in the form.
        // If player.team.id is NOT null, it means team was pre-set (e.g. "Add player to team X")
        // or selected in the form during an edit.

        Long teamId = (player.getTeam() != null) ? player.getTeam().getId() : null;

        if (teamId == null) {
            // This case implies a new player from /new_general where team selection is mandatory in the form
            // or an edit where the team was somehow removed (which shouldn't happen with current form).
            // The th:field="*{team}" in the select dropdown (to be added) should bind team.id.
            // If it's still null, it means no selection was made.
            bindingResult.rejectValue("team", "error.player", "A team must be selected for the player.");
        } else {
            Team team = teamService.getTeam(teamId);
            if (team == null) {
                bindingResult.rejectValue("team", "error.player", "Selected team not found.");
            } else {
                player.setTeam(team); // Ensure the full team object is set
            }
        }

        if (bindingResult.hasErrors()) {
            addAuthNameToModel(model);
            model.addAttribute("allTeams", teamService.getAllTeams()); // Always provide allTeams if there are errors

            if (player.getTeam() != null && player.getTeam().getId() != null && player.getTeam().getName() != null) {
                model.addAttribute("teamName", player.getTeam().getName());
                model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add Player to " + player.getTeam().getName());
            } else if (player.getTeam() != null && player.getTeam().getId() != null) {
                // If team name is not populated yet, fetch it
                Team currentTeam = teamService.getTeam(player.getTeam().getId());
                if (currentTeam != null) {
                    model.addAttribute("teamName", currentTeam.getName());
                    model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add Player to " + currentTeam.getName());
                } else {
                     model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add New Player");
                }
            }
             else {
                model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add New Player");
            }
            return "players_form";
        }

        boolean isNew = player.getId() == null;
        playerService.savePlayer(player);
        redirectAttributes.addFlashAttribute("successMessage", "Player '" + player.getName() + "' " + (isNew ? "added" : "updated") + " successfully!");
        
        // If the team is set, redirect to the team details page, otherwise to the players overview.
        if (player.getTeam() != null && player.getTeam().getId() != null) {
            return "redirect:/teams/" + player.getTeam().getId();
        } else {
            // This case should ideally not be hit if validation for team selection is robust.
            // However, as a fallback, redirect to general players list.
            return "redirect:/players/";
        }
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