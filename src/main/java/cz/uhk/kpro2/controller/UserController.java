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

import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.service.UserService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private void addAuthNameToModel(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("authName", authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()) ? authentication.getName() : null);
    }

    @GetMapping("/")
    public String getAll(Model model) {
        addAuthNameToModel(model);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("title", "User Management");
        return "users_list";
    }

    @GetMapping("/new")
    public String newUser(Model model) {
        addAuthNameToModel(model);
        model.addAttribute("user", new User());
        model.addAttribute("title", "Add New User");
        return "users_form";
    }

    @GetMapping("/{id}")
    public String getUser(Model model, @PathVariable long id) {
        addAuthNameToModel(model);
        User user = userService.getUser(id);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("title", "User Details: " + user.getUsername());
            return "users_detail";
        }
        return "redirect:/users/";
    }

    @GetMapping("/{id}/edit")
    public String editUser(Model model, @PathVariable long id) {
        addAuthNameToModel(model);
        User user = userService.getUser(id);
        if (user != null) {
            user.setPassword(""); // Clear password for form display
            model.addAttribute("user", user);
            model.addAttribute("title", "Edit User: " + user.getUsername());
            return "users_form";
        }
        return "redirect:/users/";
    }

    @PostMapping("/save")
    public String saveUser(@Valid @ModelAttribute("user") User user, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        // Custom validation for username uniqueness
        if (!userService.isUsernameUnique(user.getUsername(), user.getId())) {
            bindingResult.rejectValue("username", "error.user", "Username already exists.");
        }
        // For new users, password is required.
        if (user.getId() == null && (user.getPassword() == null || user.getPassword().isEmpty())) {
            bindingResult.rejectValue("password", "error.user", "Password is required for new users.");
        } else if (user.getId() == null && user.getPassword() != null && user.getPassword().length() < 6){
             bindingResult.rejectValue("password", "error.user", "Password must be at least 6 characters for new users.");
        }
        // For existing users, if password is provided, it must meet criteria
        if (user.getId() != null && user.getPassword() != null && !user.getPassword().isEmpty() && user.getPassword().length() < 6){
            bindingResult.rejectValue("password", "error.user", "New password must be at least 6 characters if changed.");
        }


        if (bindingResult.hasErrors()) {
            addAuthNameToModel(model);
            model.addAttribute("title", user.getId() != null ? "Edit User: " + user.getUsername() : "Add New User");
            // user object with errors is already in model due to @ModelAttribute
            return "users_form";
        }

        userService.saveUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "User '" + user.getUsername() + "' saved successfully.");
        return "redirect:/users/";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable long id, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User userToDelete = userService.getUser(id);

        if (userToDelete == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/users/";
        }

        // Prevent deleting the currently logged-in user or the main 'admin' user
        if (userToDelete.getUsername().equals(currentUsername) || "admin".equalsIgnoreCase(userToDelete.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete the current user or the main admin user.");
            return "redirect:/users/" + id; // Redirect back to the user's detail page
        }

        try {
            userService.deleteUser(id); // Or coachService.deleteCoach(id), teamService.deleteTeam(id)
            redirectAttributes.addFlashAttribute("successMessage", "User '" + userToDelete.getUsername() + "' deleted successfully.");
        } catch (Exception e) {
            // THIS IS THE CRITICAL PART NOW
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
            // For debugging, let's also print the stack trace to the server console
            e.printStackTrace(); // ADD THIS LINE
        }
        return "redirect:/users/"; // Or /coaches/, /teams/
    }
}