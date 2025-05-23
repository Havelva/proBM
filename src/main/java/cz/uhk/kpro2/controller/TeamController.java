package cz.uhk.kpro2.controller;

import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.service.CoachService;
import cz.uhk.kpro2.service.TeamService;
import cz.uhk.kpro2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/teams")
public class TeamController {

    private final TeamService teamService;
    private final CoachService coachService;
    private final UserService userService;

    @Autowired
    public TeamController(TeamService teamService, CoachService coachService, UserService userService) {
        this.teamService = teamService;
        this.coachService = coachService;
        this.userService = userService;
    }

    @GetMapping("/")
    public String getAll(Model model) {
        model.addAttribute("teams", teamService.getAllTeams());
        return "teams_list";
    }

    @GetMapping("/new")
    public String newTeam(Model model) {
        model.addAttribute("team", new Team());
        model.addAttribute("coaches", coachService.getAllCoaches());
        model.addAttribute("members", userService.getAllUsers());
        return "teams_form";
    }

    @GetMapping("/{id}")
    public String getTeam(Model model, @PathVariable long id) {
        Team team = teamService.getTeam(id);
        if (team != null) {
            model.addAttribute("team", team);
            return "teams_detail";
        }
        return "redirect:/teams/";
    }

    @GetMapping("/{id}/players/new")
    public String redirectToPlayerForm(@PathVariable long id) {
        return "redirect:/players/new?teamId=" + id;
    }

    @GetMapping("/{id}/edit")
    public String editTeam(Model model, @PathVariable long id) {
        Team team = teamService.getTeam(id);
        if (team != null) {
            model.addAttribute("coaches", coachService.getAllCoaches());
            model.addAttribute("members", userService.getAllUsers());
            model.addAttribute("team", team);
            return "teams_form";
        }
        return "redirect:/teams/";
    }

    @PostMapping("/save")
    public String saveTeam(@ModelAttribute Team team) {
        if (team.getPlayers() != null) {
            for (Player player : team.getPlayers()) {
                player.setTeam(team);
            }
        }
        teamService.saveTeam(team);
        return "redirect:/teams/";
    }

    @GetMapping("/{id}/delete")
    public String deleteTeam(Model model, @PathVariable long id) {
        Team team = teamService.getTeam(id);
        if (team != null) {
            model.addAttribute("team", team);
            return "teams_delete";
        }
        return "redirect:/teams/";
    }

    @PostMapping("/{id}/delete")
    public String deleteTeamConfirm(@PathVariable long id) {
        teamService.deleteTeam(id);
        return "redirect:/teams/";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user) {
        userService.saveUser(user);
        return "redirect:/teams/login";
    }

}
