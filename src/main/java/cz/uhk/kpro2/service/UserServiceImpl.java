package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    // Consider adding PasswordEncoder here if not already handled in SecurityConfig
    // private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository) { // Potentially add PasswordEncoder to constructor
        this.userRepository = userRepository;
        // this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUser(long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    @Override
    public User saveUser(User user) { // Changed return type from void to User
        // if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) { // Basic check if password might be unencoded
        //    user.setPassword(passwordEncoder.encode(user.getPassword()));
        // }
        return userRepository.save(user); // Return the saved entity
    }

    @Override
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User findByUsername(String username) {
        // Consider optimizing this if UserRepository can directly findByUsername
        // return userRepository.findByUsername(username).orElse(null);
        // For now, keeping the loop, but a direct repository method is preferred.
        for (User user : userRepository.findAll()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}
