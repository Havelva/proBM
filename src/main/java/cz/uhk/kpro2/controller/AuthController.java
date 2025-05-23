package cz.uhk.kpro2.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.service.UserService;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder; // For registration encoding

    @Autowired
    public AuthController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "register_success", required = false) String registerSuccess,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password!");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registerSuccess != null) {
            model.addAttribute("successMessage", "Registration successful! Please login.");
        }
        model.addAttribute("title", "Login");
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        model.addAttribute("title", "Register Account");
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               Model model, // Used to pass data back to the form on error
                               RedirectAttributes redirectAttributes) {

        if (userService.findByUsername(user.getUsername()) != null) {
            bindingResult.rejectValue("username", "error.user", "Username already exists!");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
             bindingResult.rejectValue("password", "error.user", "Password cannot be empty.");
        } else if (user.getPassword().length() < 6) { // Example: min password length
             bindingResult.rejectValue("password", "error.user", "Password must be at least 6 characters long.");
        }


        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Register Account");
            // user object with errors is already in model due to @ModelAttribute
            return "register";
        }

        // Encoding is now handled in UserServiceImpl by default
        // user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Roles can be set here if needed, or default in User model/UserServiceImpl
        // user.setRoles("ROLE_USER");
        userService.saveUser(user);

        redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
        return "redirect:/login?register_success";
    }
}