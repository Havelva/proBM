package cz.uhk.kpro2.controller;

import java.util.ArrayList;
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

import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.service.CoachService;
import cz.uhk.kpro2.service.TeamService;
import cz.uhk.kpro2.service.UserService;
import jakarta.validation.Valid;

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

    private void addCommonAttributes(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("authName", authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()) ? authentication.getName() : null);
        model.addAttribute("coaches", coachService.getAllCoaches());
        model.addAttribute("allUsers", userService.getAllUsers());
    }

    @GetMapping("/")
    public String getAll(Model model) {
        addCommonAttributes(model);
        model.addAttribute("teams", teamService.getAllTeams());
        model.addAttribute("title", "Teams");
        return "teams_list";
    }

    @GetMapping("/new")
    public String newTeam(Model model) {
        addCommonAttributes(model);
        model.addAttribute("team", new Team());
        model.addAttribute("title", "Create New Team");
        return "teams_form";
    }

    @GetMapping("/{id}")
    public String getTeam(Model model, @PathVariable long id) {
        addCommonAttributes(model);
        Team team = teamService.getTeam(id);
        if (team != null) {
            model.addAttribute("team", team);
            model.addAttribute("title", "Team Details: " + team.getName());
            return "teams_detail";
        }
        return "redirect:/teams/";
    }

    @GetMapping("/{id}/edit")
    public String editTeam(Model model, @PathVariable long id) {
        addCommonAttributes(model);
        Team team = teamService.getTeam(id);
        if (team != null) {
            model.addAttribute("team", team);
            model.addAttribute("title", "Edit Team: " + team.getName());
            return "teams_form";
        }
        return "redirect:/teams/";
    }

    @PostMapping("/save")
    public String saveTeam(@Valid @ModelAttribute Team team,
                           BindingResult bindingResult,
                           @RequestParam(required = false) List<Long> memberIds,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        // If coach ID is null (e.g., "-- Select Coach --" was chosen), set the coach object to null.
        if (team.getCoach() != null && team.getCoach().getId() == null) {
            team.setCoach(null);
        }

        if (bindingResult.hasErrors()) {
            addCommonAttributes(model);
            model.addAttribute("title", team.getId() != null ? "Edit Team: " + team.getName() : "Create New Team");
            // Ensure users are re-populated for the form if there are errors
            if (team.getId() != null) { // If editing an existing team, reload its members for display
                Team existingTeam = teamService.getTeam(team.getId());
                if (existingTeam != null) {
                    model.addAttribute("memberIds", existingTeam.getMembers().stream().map(User::getId).toList());
                }
            } else if (memberIds != null) { // If creating, retain selected members
                 model.addAttribute("memberIds", memberIds);
            }
            return "teams_form";
        }

        List<User> members = new ArrayList<>();
        if (memberIds != null && !memberIds.isEmpty()) {
            for (Long memberId : memberIds) {
                User member = userService.getUser(memberId);
                if (member != null) {
                    members.add(member);
                }
            }
        }
        team.setMembers(members);

        teamService.saveTeam(team);
        redirectAttributes.addFlashAttribute("successMessage", "Team '" + team.getName() + "' saved successfully.");
        return "redirect:/teams/";
    }

    @PostMapping("/{id}/delete")
    public String deleteTeam(@PathVariable long id, RedirectAttributes redirectAttributes) {
        Team team = teamService.getTeam(id);
        if (team != null) {
            try {
                teamService.deleteTeam(id);
                redirectAttributes.addFlashAttribute("successMessage", "Team '" + team.getName() + "' deleted successfully.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Error deleting team: " + e.getMessage());
                 e.printStackTrace(); // Good for debugging
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Team not found.");
        }
        return "redirect:/teams/";
    }

    // New endpoint for team standings
    @GetMapping("/standings")
    public String showStandings(Model model) {
        addCommonAttributes(model); // For authName, if needed, or other common data
        model.addAttribute("standings", teamService.getTeamStandings());
        model.addAttribute("title", "Team Standings");
        return "team_standings"; // We will create this template
    }
}