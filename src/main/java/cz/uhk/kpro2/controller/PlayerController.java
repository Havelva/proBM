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

import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.service.PlayerService;
import cz.uhk.kpro2.service.TeamService;
import jakarta.validation.Valid; // Ensure List is imported

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

    @GetMapping("/")
    public String listAllPlayers(Model model) {
        addAuthNameToModel(model);
        model.addAttribute("players", playerService.getAllPlayers());
        model.addAttribute("title", "All Players");
        return "players_overview";
    }

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
        model.addAttribute("allTeams", teamService.getAllTeams());
        return "players_form";
    }

    @GetMapping("/new_general")
    public String newPlayerGeneral(Model model) {
        addAuthNameToModel(model);
        Player player = new Player();
        model.addAttribute("player", player);
        model.addAttribute("allTeams", teamService.getAllTeams());
        model.addAttribute("title", "Add New Player");
        return "players_form";
    }

    @PostMapping("/save")
    public String savePlayer(@Valid @ModelAttribute Player player, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        Long teamId = (player.getTeam() != null && player.getTeam().getId() != null) ? player.getTeam().getId() : null;

        if (teamId == null) {
            // This check ensures that if the team object itself is null OR its ID is null, we flag it.
            // The @NotNull on player.team in Player.java handles the case where player.team is null,
            // but this explicit check for teamId handles cases where team object might exist but without an ID.
            if (!bindingResult.hasFieldErrors("team")) { // Avoid adding duplicate error if @NotNull already caught it
                 bindingResult.rejectValue("team", "error.player", "A team must be selected for the player.");
            }
        } else {
            Team team = teamService.getTeam(teamId);
            if (team == null) {
                bindingResult.rejectValue("team", "error.player", "Selected team not found.");
            } else {
                player.setTeam(team); // Ensure the full team object is set

                // **Unique Jersey Number Validation within the team**
                if (player.getJerseyNumber() != null) {
                    List<Player> teammates = playerService.getPlayersByTeamId(teamId);
                    for (Player teammate : teammates) {
                        // If we are editing an existing player, don't compare them to themselves
                        if (player.getId() != null && teammate.getId().equals(player.getId())) {
                            continue;
                        }
                        if (teammate.getJerseyNumber().equals(player.getJerseyNumber())) {
                            bindingResult.rejectValue("jerseyNumber", "error.player", "Jersey number " + player.getJerseyNumber() + " is already taken on this team.");
                            break;
                        }
                    }
                }
            }
        }

        if (bindingResult.hasErrors()) {
            addAuthNameToModel(model);
            model.addAttribute("allTeams", teamService.getAllTeams());

            // Re-populate title and teamName correctly for the form
            if (player.getTeam() != null && player.getTeam().getId() != null) {
                 Team currentTeam = player.getTeam(); // If team object is already populated (even if just ID)
                 if (currentTeam.getName() == null) { // Fetch full team if name is missing
                     currentTeam = teamService.getTeam(currentTeam.getId());
                 }
                 if(currentTeam != null){
                    model.addAttribute("teamName", currentTeam.getName());
                    model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add Player to " + currentTeam.getName());
                 } else { // Fallback if team somehow becomes null
                    model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add New Player");
                 }
            } else {
                model.addAttribute("title", player.getId() != null ? "Edit Player: " + player.getName() : "Add New Player");
            }
            return "players_form";
        }

        boolean isNew = player.getId() == null;
        playerService.savePlayer(player);
        redirectAttributes.addFlashAttribute("successMessage", "Player '" + player.getName() + "' " + (isNew ? "added" : "updated") + " successfully!");
        
        if (player.getTeam() != null && player.getTeam().getId() != null) {
            return "redirect:/teams/" + player.getTeam().getId();
        } else {
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
        model.addAttribute("allTeams", teamService.getAllTeams()); 

        if (player.getTeam() != null) {
            model.addAttribute("teamName", player.getTeam().getName()); // teamName used in the form's conditional hidden field
            model.addAttribute("title", "Edit Player: " + player.getName() + " (Team: " + player.getTeam().getName() + ")");
        } else {
            model.addAttribute("title", "Edit Player: " + player.getName());
        }
        return "players_form";
    }

    @PostMapping("/{id}/delete")
    public String deletePlayerConfirm(@PathVariable Long id, 
                                      @RequestParam(required = false) String origin,
                                      RedirectAttributes redirectAttributes) {
        Player player = playerService.getPlayer(id);
        if (player != null) {
            String playerName = player.getName();
            Long teamId = player.getTeam() != null ? player.getTeam().getId() : null;
            
            playerService.deletePlayer(id);
            redirectAttributes.addFlashAttribute("successMessage", "Player '" + playerName + "' deleted successfully!");
            
            if ("overview".equals(origin)) {
                return "redirect:/players/"; // Redirect to all players list if deleted from overview
            }
            if (teamId != null) {
                 return "redirect:/teams/" + teamId; // Redirect to team details if player had a team
            }
            return "redirect:/players/"; // Default redirect to all players list
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Player not found or could not be deleted.");
        return "redirect:/players/";
    }
}