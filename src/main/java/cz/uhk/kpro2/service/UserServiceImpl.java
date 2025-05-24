package cz.uhk.kpro2.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.repository.UserRepository;
import cz.uhk.kpro2.repository.TeamRepository; // Add this import
import cz.uhk.kpro2.model.Team; // Add this import

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamRepository teamRepository; // Add this field

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, TeamRepository teamRepository) { // Add TeamRepository here
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.teamRepository = teamRepository; // Initialize here
    }

    @Override @Transactional(readOnly = true)
    public List<User> getAllUsers() { return userRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public User getUser(long id) { return userRepository.findById(id).orElse(null); }

    @Override @Transactional
    public User saveUser(User user) {
        // Handle password encoding for new users or if password is changed
        if (user.getId() == null) { // New user
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                // This should be caught by validation, but as a fallback
                throw new IllegalArgumentException("Password cannot be empty for a new user.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else { // Existing user
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                // Password field was filled, so encode the new password
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                // Password field was empty, keep the existing password
                User existingUser = userRepository.findById(user.getId())
                                      .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getId() + " for password retention."));
                user.setPassword(existingUser.getPassword());
            }
        }
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles("ROLE_USER"); // Default role if not specified
        }
        return userRepository.save(user);
    }

    @Override @Transactional
    public void deleteUser(long id) {
        User userToDelete = userRepository.findById(id).orElse(null);
        if (userToDelete != null) {
            // Remove user from all teams they are a member of
            List<Team> allTeams = teamRepository.findAll();
            for (Team team : allTeams) {
                if (team.getMembers().contains(userToDelete)) {
                    team.getMembers().remove(userToDelete);
                    teamRepository.save(team);
                }
            }
            userRepository.deleteById(id);
        }
    }

    @Override @Transactional(readOnly = true)
    public User findByUsername(String username) { return userRepository.findByUsername(username); }

    @Override @Transactional(readOnly = true)
    public boolean isUsernameUnique(String username, Long userId) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser == null) {
            return true; // Username is not taken
        }
        // If username is taken, check if it belongs to the current user being edited
        return userId != null && existingUser.getId().equals(userId);
    }
}