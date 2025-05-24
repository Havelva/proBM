package cz.uhk.kpro2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.repository.UserRepository;
import cz.uhk.kpro2.repository.TeamRepository;
import cz.uhk.kpro2.model.Team;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TeamRepository teamRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, TeamRepository teamRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.teamRepository = teamRepository;
    }

    @Override @Transactional(readOnly = true)
    public List<User> getAllUsers() { return userRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public User getUser(long id) { return userRepository.findById(id).orElse(null); }

    @Override @Transactional
    public User saveUser(User user) {
        if (user.getId() == null) { // New user
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be empty for a new user.");
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                user.setRoles("ROLE_USER"); // Default role if not specified
            }
            return userRepository.save(user);
        } else { // Existing user
            User existingUser = userRepository.findById(user.getId())
                                  .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + user.getId()));

            // Update fields from 'user' onto 'existingUser'
            existingUser.setUsername(user.getUsername());
            existingUser.setRoles(user.getRoles());

            // Only update password if a new one is provided and not empty
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            // If user.getPassword() is null or empty, existingUser's password remains unchanged.

            return userRepository.save(existingUser); // Save the merged entity
        }
    }


    @Override @Transactional
    public void deleteUser(long id) {
        User userToDelete = userRepository.findById(id).orElse(null);
        if (userToDelete != null) {
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
            return true;
        }
        return userId != null && existingUser.getId().equals(userId);
    }
}