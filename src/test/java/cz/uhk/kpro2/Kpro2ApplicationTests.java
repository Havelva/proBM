package cz.uhk.kpro2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List; // Import List

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.model.PlayerPosition;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.service.CoachService;
import cz.uhk.kpro2.service.PlayerService;
import cz.uhk.kpro2.service.TeamService;
import cz.uhk.kpro2.service.UserService;

@SpringBootTest
@Transactional 
class Kpro2ApplicationTests {

    @Autowired private UserService userService;
    @Autowired private CoachService coachService;
    @Autowired private TeamService teamService;
    @Autowired private PlayerService playerService;
    @Autowired private PasswordEncoder passwordEncoder;

    private User testAdmin;
    private User testUser;

    @BeforeEach
    void setUpTestData() {
        if (userService.findByUsername("test_admin_junit") == null) {
            testAdmin = new User();
            testAdmin.setUsername("test_admin_junit");
            testAdmin.setPassword("adminPass123"); 
            testAdmin.setRoles("ROLE_ADMIN,ROLE_USER");
            userService.saveUser(testAdmin);
        } else {
            testAdmin = userService.findByUsername("test_admin_junit");
        }


        if (userService.findByUsername("test_user_junit") == null) {
            testUser = new User();
            testUser.setUsername("test_user_junit");
            testUser.setPassword("userPass123");
            testUser.setRoles("ROLE_USER");
            userService.saveUser(testUser);
        } else {
            testUser = userService.findByUsername("test_user_junit");
        }
    }

    @Test
    void contextLoads() {
        assertNotNull(userService);
        assertNotNull(coachService);
        assertNotNull(teamService);
        assertNotNull(playerService);
        assertNotNull(passwordEncoder);
    }

    @Test
    void testUserCreationAndRetrieval() {
        User newUser = new User();
        newUser.setUsername("unique_junit_user");
        newUser.setPassword("securePassword123");
        newUser.setRoles("ROLE_GUEST");

        User savedUser = userService.saveUser(newUser);

        assertNotNull(savedUser, "Saved user should not be null");
        assertNotNull(savedUser.getId(), "Saved user ID should not be null");
        assertEquals("unique_junit_user", savedUser.getUsername());
        assertTrue(passwordEncoder.matches("securePassword123", savedUser.getPassword()), "Encoded password should match");
        assertEquals("ROLE_GUEST", savedUser.getRoles());

        User retrievedUser = userService.findByUsername("unique_junit_user");
        assertNotNull(retrievedUser);
        assertEquals(savedUser.getId(), retrievedUser.getId());
    }

    @Test
    void testUsernameUniqueness() {
        assertNotNull(testAdmin, "Test admin should exist");
        assertFalse(userService.isUsernameUnique("test_admin_junit", null), "Username should be taken by new user");
        assertTrue(userService.isUsernameUnique("test_admin_junit", testAdmin.getId()), "Username should be unique for the same user being edited");
        assertTrue(userService.isUsernameUnique("completely_new_username", null), "New username should be unique");
    }


    @Test
    void testCoachCreationAndDeletion() {
        Coach coach = new Coach();
        coach.setName("Coach JUnit Test");
        coach.setExperienceYears(5);
        Coach savedCoach = coachService.saveCoach(coach);

        assertNotNull(savedCoach);
        assertNotNull(savedCoach.getId());
        assertEquals("Coach JUnit Test", savedCoach.getName());

        long coachId = savedCoach.getId();
        coachService.deleteCoach(coachId);
        assertNull(coachService.getCoach(coachId), "Coach should be deleted");
    }

    @Test
    void testTeamAndPlayerFullLifecycle() {
        Coach coach = new Coach();
        coach.setName("Lifecycle Test Coach");
        coach.setExperienceYears(8);
        Coach savedCoach = coachService.saveCoach(coach);

        Team team = new Team();
        team.setName("Lifecycle Test Team");
        team.setCoach(savedCoach);
        team.getMembers().add(testUser);
        Team savedTeam = teamService.saveTeam(team);
        assertNotNull(savedTeam.getId());
        assertEquals(1, savedTeam.getMembers().size());

        Player player = new Player();
        player.setName("Lifecycle Test Player");
        player.setPosition(PlayerPosition.SMALL_FORWARD);
        player.setJerseyNumber(33);
        player.setSkillLevel("All-Star");
        player.setPointsPerGame(22.5);
        player.setTeam(savedTeam); 
        Player savedPlayer = playerService.savePlayer(player);
        assertNotNull(savedPlayer.getId());
        assertEquals(savedTeam.getId(), savedPlayer.getTeam().getId());

        // Retrieve players by team ID instead of relying on Team entity's collection
        List<Player> teamPlayers = playerService.getPlayersByTeamId(savedTeam.getId());
        assertNotNull(teamPlayers);
        assertFalse(teamPlayers.isEmpty(), "Team should have players");
        assertEquals(1, teamPlayers.size());
        assertEquals(savedPlayer.getName(), teamPlayers.get(0).getName());

        savedPlayer.setPointsPerGame(25.0);
        playerService.savePlayer(savedPlayer);
        Player updatedPlayer = playerService.getPlayer(savedPlayer.getId());
        assertEquals(25.0, updatedPlayer.getPointsPerGame());

        playerService.deletePlayer(savedPlayer.getId());
        assertNull(playerService.getPlayer(savedPlayer.getId()), "Player should be deleted");

        Team teamAfterPlayerDeletion = teamService.getTeam(savedTeam.getId());
        assertTrue(teamAfterPlayerDeletion.getPlayers().isEmpty(), "Team's player list should be empty");

        teamService.deleteTeam(savedTeam.getId());
        assertNull(teamService.getTeam(savedTeam.getId()), "Team should be deleted");

        Coach stillExistingCoach = coachService.getCoach(savedCoach.getId());
        assertNotNull(stillExistingCoach);
    }

     @Test
    void testPasswordEncodingOnUserUpdate() {
        User userToUpdate = userService.findByUsername("test_user_junit");
        assertNotNull(userToUpdate);
        String oldEncodedPassword = userToUpdate.getPassword();

        // Create a temporary user object for update to avoid modifying the managed entity directly
        User updateData = new User();
        updateData.setId(userToUpdate.getId());
        updateData.setUsername("test_user_junit_updated");
        updateData.setRoles(userToUpdate.getRoles());
        updateData.setPassword(null); // Simulate empty password field in form
        
        // Use the updated saveUser which handles null passwords correctly
        User updatedUser1 = userService.saveUser(updateData); 
        
        assertNotNull(updatedUser1);
        assertEquals("test_user_junit_updated", updatedUser1.getUsername());
        assertEquals(oldEncodedPassword, updatedUser1.getPassword(), "Password should not have changed when field was empty");

        // Scenario 2: Update password
        updateData.setPassword("newPassword123");
        User updatedUser2 = userService.saveUser(updateData);
        
        assertNotNull(updatedUser2);
        assertTrue(passwordEncoder.matches("newPassword123", updatedUser2.getPassword()), "Password should be updated and encoded");
        assertNotEquals(oldEncodedPassword, updatedUser2.getPassword());
    }
}