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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.service.CoachService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/coaches")
public class CoachController {
    private final CoachService coachService;

    @Autowired
    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    private void addAuthNameToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("authName", authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()) ? authentication.getName() : null);
    }

    @GetMapping("/")
    public String getAll(Model model) {
        addAuthNameToModel(model);
        model.addAttribute("coachesList", coachService.getAllCoaches());
        model.addAttribute("title", "Coaches");
        return "coaches_list";
    }

    @GetMapping("/new")
    public String newCoach(Model model) {
        addAuthNameToModel(model);
        model.addAttribute("coach", new Coach());
        model.addAttribute("title", "Add New Coach");
        return "coaches_form";
    }

    @GetMapping("/{id}")
    public String getCoach(Model model, @PathVariable long id) {
        addAuthNameToModel(model);
        Coach coach = coachService.getCoach(id);
        if (coach != null) {
            model.addAttribute("coach", coach);
            model.addAttribute("title", "Coach Details: " + coach.getName());
            return "coaches_detail";
        }
        return "redirect:/coaches/";
    }

    @GetMapping("/{id}/edit")
    public String editCoach(Model model, @PathVariable long id) {
        addAuthNameToModel(model);
        Coach coach = coachService.getCoach(id);
        if (coach != null) {
            model.addAttribute("coach", coach);
            model.addAttribute("title", "Edit Coach: " + coach.getName());
            return "coaches_form";
        }
        return "redirect:/coaches/";
    }

    @PostMapping("/save")
    public String saveCoach(@Valid @ModelAttribute Coach coach, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addAuthNameToModel(model);
            model.addAttribute("title", coach.getId() != null ? "Edit Coach: " + coach.getName() : "Add New Coach");
            return "coaches_form";
        }
        boolean isNew = coach.getId() == null;
        Coach savedCoach = coachService.saveCoach(coach);
        redirectAttributes.addFlashAttribute("successMessage", "Coach '" + savedCoach.getName() + "' " + (isNew ? "added" : "updated") + " successfully!");
        return "redirect:/coaches/";
    }

    @PostMapping("/{id}/delete")
    public String deleteCoachConfirm(@PathVariable long id, RedirectAttributes redirectAttributes) {
        Coach coach = coachService.getCoach(id);
         if (coach != null) {
            // Add logic here if coaches shouldn't be deleted if assigned to teams (or handle in DB with constraints/service layer)
            coachService.deleteCoach(id);
            redirectAttributes.addFlashAttribute("successMessage", "Coach '" + coach.getName() + "' deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Coach not found.");
        }
        return "redirect:/coaches/";
    }
}