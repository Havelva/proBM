package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUser(long id);
    User saveUser(User user);
    void deleteUser(long id);
    User findByUsername(String username);
}
