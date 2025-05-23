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
        model.addAttribute("teamName", team.getName());
        model.addAttribute("title", "Add Player to " + team.getName());
        return "players_form";
    }

    @PostMapping("/save")
    public String savePlayer(@Valid @ModelAttribute Player player, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Ensure team is still valid and re-associate if necessary
        if (player.getTeam() == null || player.getTeam().getId() == null) {
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
            // If team info was lost or not correctly set, try to re-fetch for the form title
            if (player.getTeam() != null && player.getTeam().getId() != null) {
                 Team team = teamService.getTeam(player.getTeam().getId());
                 if(team != null) {
                    model.addAttribute("teamName", team.getName());
                    model.addAttribute("title", player.getId() != null ? "Edit Player" : "Add Player to " + team.getName());
                 } else {
                     model.addAttribute("title", player.getId() != null ? "Edit Player" : "Add Player");
                 }
            } else {
                 model.addAttribute("title", player.getId() != null ? "Edit Player" : "Add Player");
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
        if (player == null || player.getTeam() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Player or associated team not found.");
            return "redirect:/teams/";
        }
        model.addAttribute("player", player);
        model.addAttribute("teamName", player.getTeam().getName());
        model.addAttribute("title", "Edit Player: " + player.getName());
        return "players_form";
    }

    @PostMapping("/{id}/delete")
    public String deletePlayerConfirm(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Player player = playerService.getPlayer(id);
        if (player != null && player.getTeam() != null) {
            Long teamId = player.getTeam().getId();
            String playerName = player.getName();
            playerService.deletePlayer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Player '" + playerName + "' deleted successfully!");
            return "redirect:/teams/" + teamId;
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Player not found or could not be deleted.");
        return "redirect:/teams/"; // Fallback redirect
    }
}