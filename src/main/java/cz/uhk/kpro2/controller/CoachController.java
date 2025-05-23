package cz.uhk.kpro2.controller;

import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.service.CoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/coaches")
public class CoachController {
    private CoachService coachService;

    @Autowired
    public CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping("/")
    public String getAll(Model model) {
        model.addAttribute("coachesList", coachService.getAllCoaches());
        return "coaches_list";
    }

    @GetMapping("/new")
    public String newCoach(Model model) {
        model.addAttribute("coach", new Coach());
        return "coaches_form";
    }

    @GetMapping("/{id}")
    public String getCoach(Model model, @PathVariable long id) {
        Coach coach = coachService.getCoach(id);
        if (coach != null) {
            model.addAttribute("coach", coach);
            return "coaches_detail";
        }
        return "redirect:/coaches/";
    }

    @GetMapping("/{id}/edit")
    public String editCoach(Model model, @PathVariable long id) {
        Coach coach = coachService.getCoach(id);
        if (coach != null) {
            model.addAttribute("coach", coach);
            return "coaches_form";
        }
        return "redirect:/coaches/";
    }

    @PostMapping("/save")
    public String saveCoach(@ModelAttribute Coach coach) {
        coachService.saveCoach(coach);
        return "redirect:/coaches/";
    }

    @GetMapping("/{id}/delete")
    public String deleteCoach(Model model, @PathVariable long id) {
        Coach coach = coachService.getCoach(id);
        if (coach != null) {
            model.addAttribute("coach", coach);
            return "coaches_delete";
        }
        return "redirect:/coaches/";
    }

    @PostMapping("/{id}/delete")
    public String deleteCoachConfirm(@PathVariable long id) {
        coachService.deleteCoach(id);
        return "redirect:/coaches/";
    }

}