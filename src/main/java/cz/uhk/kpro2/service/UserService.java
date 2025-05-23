package cz.uhk.kpro2.service;
import java.util.List;

import cz.uhk.kpro2.model.User;
public interface UserService {
    List<User> getAllUsers();
    User getUser(long id);
    User saveUser(User user); // Handles password encoding
    void deleteUser(long id);
    User findByUsername(String username);
    boolean isUsernameUnique(String username, Long userId);
}