package cz.uhk.kpro2.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.repository.TeamRepository;
import cz.uhk.kpro2.repository.UserRepository;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService; // Use UserServiceImpl for testing implementation details

    @Autowired
    private UserRepository userRepository; // Autowire for setup and verification

    @Autowired
    private TeamRepository teamRepository; // Autowire for setup and verification

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testAdmin;
    private User testUser;
    private User testUserToDelete;

    @BeforeEach
    void setUp() { // Renamed from setUpTestData
        // Clean up previous test data to ensure isolation
        User existingAdmin = userRepository.findByUsername("test_admin_user_service_junit");
        if (existingAdmin != null) {
            // Manually remove from teams if necessary before deleting
            List<Team> teams = teamRepository.findAll();
            for (Team team : teams) {
                if (team.getMembers().contains(existingAdmin)) {
                    team.getMembers().remove(existingAdmin);
                    teamRepository.save(team);
                }
            }
            userRepository.delete(existingAdmin);
        }

        User existingUser = userRepository.findByUsername("test_user_user_service_junit");
        if (existingUser != null) {
            List<Team> teams = teamRepository.findAll();
            for (Team team : teams) {
                if (team.getMembers().contains(existingUser)) {
                    team.getMembers().remove(existingUser);
                    teamRepository.save(team);
                }
            }
            userRepository.delete(existingUser);
        }
        
        User existingUserToDelete = userRepository.findByUsername("test_user_to_delete_junit");
        if (existingUserToDelete != null) {
            List<Team> teams = teamRepository.findAll();
            for (Team team : teams) {
                if (team.getMembers().contains(existingUserToDelete)) {
                    team.getMembers().remove(existingUserToDelete);
                    teamRepository.save(team);
                }
            }
            userRepository.delete(existingUserToDelete);
        }


        testAdmin = new User();
        testAdmin.setUsername("test_admin_user_service_junit");
        testAdmin.setPassword("adminPass123"); // Raw password
        testAdmin.setRoles("ROLE_ADMIN,ROLE_USER");
        // Password encoding is handled by userService.saveUser
        testAdmin = userService.saveUser(testAdmin);


        testUser = new User();
        testUser.setUsername("test_user_user_service_junit");
        testUser.setPassword("userPass123"); // Raw password
        testUser.setRoles("ROLE_USER");
        testUser = userService.saveUser(testUser);
        
        testUserToDelete = new User();
        testUserToDelete.setUsername("test_user_to_delete_junit");
        testUserToDelete.setPassword("deletePass123");
        testUserToDelete.setRoles("ROLE_USER");
        testUserToDelete = userService.saveUser(testUserToDelete);
    }

    @Test
    void testGetAllUsers() {
        User user3 = new User();
        user3.setUsername("test_user3_junit");
        user3.setPassword("pass3");
        user3.setRoles("ROLE_USER");
        userService.saveUser(user3);

        List<User> users = userService.getAllUsers();
        assertNotNull(users);
        assertTrue(users.size() >= 3); // testAdmin, testUser, user3 + any other existing users
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("test_admin_user_service_junit")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("test_user_user_service_junit")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("test_user3_junit")));
    }

    @Test
    void testGetUser_existing() {
        User foundUser = userService.getUser(testAdmin.getId());
        assertNotNull(foundUser);
        assertEquals(testAdmin.getId(), foundUser.getId());
        assertEquals(testAdmin.getUsername(), foundUser.getUsername());
    }

    @Test
    void testGetUser_nonExisting() {
        User foundUser = userService.getUser(-99L); // Non-existent ID
        assertNull(foundUser);
    }
    
    @Test
    void testUserCreationAndRetrieval() {
        // Ensure a unique username for this specific test to avoid conflicts from setUp
        String uniqueUsername = "unique_junit_user_creation_" + System.currentTimeMillis();
        User newUser = new User();
        newUser.setUsername(uniqueUsername);
        newUser.setPassword("securePassword123");
        newUser.setRoles("ROLE_GUEST");

        User savedUser = userService.saveUser(newUser); // Password will be encoded by the service

        assertNotNull(savedUser, "Saved user should not be null");
        assertNotNull(savedUser.getId(), "Saved user ID should not be null");
        assertEquals(uniqueUsername, savedUser.getUsername());
        assertTrue(passwordEncoder.matches("securePassword123", savedUser.getPassword()), "Encoded password should match");
        assertEquals("ROLE_GUEST", savedUser.getRoles());

        User retrievedUser = userService.findByUsername(uniqueUsername);
        assertNotNull(retrievedUser);
        assertEquals(savedUser.getId(), retrievedUser.getId());
    }

    @Test
    void testSaveNewUser_nullPassword_throwsException() {
        User newUser = new User();
        newUser.setUsername("new_user_null_pass");
        newUser.setPassword(null);
        newUser.setRoles("ROLE_USER");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(newUser);
        });
        assertEquals("Password cannot be empty for a new user.", exception.getMessage());
    }

    @Test
    void testSaveNewUser_emptyPassword_throwsException() {
        User newUser = new User();
        newUser.setUsername("new_user_empty_pass");
        newUser.setPassword("");
        newUser.setRoles("ROLE_USER");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(newUser);
        });
        assertEquals("Password cannot be empty for a new user.", exception.getMessage());
    }

    @Test
    void testSaveNewUser_defaultRole() {
        User newUser = new User();
        // Ensure a unique username for this specific test
        String uniqueUsername = "new_user_default_role_" + System.currentTimeMillis();
        newUser.setUsername(uniqueUsername);
        newUser.setPassword("password123");
        // Not setting roles

        User savedUser = userService.saveUser(newUser);
        assertNotNull(savedUser);
        assertEquals("ROLE_USER", savedUser.getRoles());
    }
    
    @Test
    void testSaveExistingUser_updateNonExisting_throwsException() {
        User nonExistingUserUpdate = new User();
        nonExistingUserUpdate.setId(-99L); // Non-existent ID
        nonExistingUserUpdate.setUsername("ghost_user");
        nonExistingUserUpdate.setPassword("newPass");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.saveUser(nonExistingUserUpdate);
        });
        assertTrue(exception.getMessage().contains("User not found with id: -99"));
    }

    @Test
    void testSaveExistingUser_emptyPassword_keepsOldPassword() {
        User userToUpdate = userService.findByUsername(testUser.getUsername());
        assertNotNull(userToUpdate);
        String originalEncodedPassword = userToUpdate.getPassword();

        User updateData = new User();
        updateData.setId(userToUpdate.getId());
        updateData.setUsername(userToUpdate.getUsername()); // Keep username or change it
        updateData.setRoles(userToUpdate.getRoles());
        updateData.setPassword(""); // Empty password

        User updatedUser = userService.saveUser(updateData);
        assertNotNull(updatedUser);
        assertEquals(originalEncodedPassword, updatedUser.getPassword(), "Password should not have changed with empty input.");
    }


    @Test
    void testDeleteUser_existingUser_notInTeam() {
        // testUserToDelete is created in setUp and not added to any team
        Long userIdToDelete = testUserToDelete.getId();
        assertNotNull(userService.getUser(userIdToDelete), "User should exist before deletion");

        userService.deleteUser(userIdToDelete);

        assertNull(userService.getUser(userIdToDelete), "User should be deleted");
    }
    
    @Test
    void testDeleteUser_existingUser_inTeam() {
        // Create a team and add testUserToDelete to it
        Team team = new Team();
        team.setName("Team With Member To Delete");
        List<User> members = new ArrayList<>();
        members.add(testUserToDelete);
        team.setMembers(members);
        Team savedTeam = teamRepository.save(team);

        Long userIdToDelete = testUserToDelete.getId();
        Long teamId = savedTeam.getId();

        assertNotNull(userService.getUser(userIdToDelete), "User should exist before deletion");
        Team teamBeforeDelete = teamRepository.findById(teamId).orElse(null);
        assertNotNull(teamBeforeDelete);
        assertTrue(teamBeforeDelete.getMembers().stream().anyMatch(m -> m.getId().equals(userIdToDelete)), "User should be in team before deletion");

        userService.deleteUser(userIdToDelete);

        assertNull(userService.getUser(userIdToDelete), "User should be deleted from user table");
        Team teamAfterDelete = teamRepository.findById(teamId).orElse(null);
        assertNotNull(teamAfterDelete, "Team should still exist");
        assertFalse(teamAfterDelete.getMembers().stream().anyMatch(m -> m.getId().equals(userIdToDelete)), "User should be removed from team members");
        
        // Cleanup team
        teamRepository.deleteById(teamId);
    }


    @Test
    void testDeleteUser_nonExistingUser() {
        long nonExistingUserId = -999L;
        // No exception should be thrown, and the state should remain unchanged.
        // This also implicitly tests that findById(id).orElse(null) in deleteUser works.
        userService.deleteUser(nonExistingUserId);
        assertNull(userService.getUser(nonExistingUserId)); // Still null
    }

    @Test
    void testFindByUsername_nonExisting() {
        User foundUser = userService.findByUsername("non_existent_user_for_test");
        assertNull(foundUser);
    }
    
    @Test
    void testUsernameUniqueness() {
        assertNotNull(testAdmin, "Test admin should exist for uniqueness test");
        // Test with a new user trying to take an existing username
        assertFalse(userService.isUsernameUnique("test_admin_user_service_junit", null), "Username should be taken by new user");
        // Test with an existing user being edited (username should be considered unique for itself)
        assertTrue(userService.isUsernameUnique("test_admin_user_service_junit", testAdmin.getId()), "Username should be unique for the same user being edited");
        // Test with a completely new username
        assertTrue(userService.isUsernameUnique("completely_new_username_for_uniqueness", null), "New username should be unique");
    }

    @Test
    void testPasswordEncodingOnUserUpdate() {
        User userToUpdate = userService.findByUsername("test_user_user_service_junit");
        assertNotNull(userToUpdate, "Test user for update should exist");
        String oldEncodedPassword = userToUpdate.getPassword(); // Already encoded

        // Scenario 1: Update user without changing password (e.g., password field empty in a form)
        User updateDataNoPasswordChange = new User();
        updateDataNoPasswordChange.setId(userToUpdate.getId());
        updateDataNoPasswordChange.setUsername("test_user_updated_name"); // Change username
        updateDataNoPasswordChange.setRoles(userToUpdate.getRoles());
        updateDataNoPasswordChange.setPassword(null); // Simulate empty password field

        User updatedUser1 = userService.saveUser(updateDataNoPasswordChange);

        assertNotNull(updatedUser1);
        assertEquals("test_user_updated_name", updatedUser1.getUsername());
        assertEquals(oldEncodedPassword, updatedUser1.getPassword(), "Password should not have changed when update field was null");

        // Scenario 2: Update password
        User updateDataWithPasswordChange = new User();
        updateDataWithPasswordChange.setId(userToUpdate.getId());
        updateDataWithPasswordChange.setUsername(updatedUser1.getUsername()); // Keep updated username or original
        updateDataWithPasswordChange.setRoles(userToUpdate.getRoles());
        updateDataWithPasswordChange.setPassword("newSecurePassword123");

        User updatedUser2 = userService.saveUser(updateDataWithPasswordChange);

        assertNotNull(updatedUser2);
        assertTrue(passwordEncoder.matches("newSecurePassword123", updatedUser2.getPassword()), "Password should be updated and newly encoded");
        assertNotEquals(oldEncodedPassword, updatedUser2.getPassword(), "New encoded password should not match old encoded password");
    }
}
