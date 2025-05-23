package cz.uhk.kpro2;

import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.model.User;
import cz.uhk.kpro2.service.CoachService;
import cz.uhk.kpro2.service.PlayerService;
import cz.uhk.kpro2.service.TeamService;
import cz.uhk.kpro2.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Kpro2ApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private CoachService coachService;

    @Autowired
    private TeamService teamService;

    @Autowired
    private PlayerService playerService;

    @Test
    void contextLoads() {
    }

    @Test
    @Transactional // Use Transactional to handle lazy loading or session issues if any
    void testUserCreationAndRetrieval() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password"); // In a real app, password would be hashed by the service
        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser, "Saved user should not be null");
        assertNotNull(savedUser.getId(), "Saved user ID should not be null");
        assertEquals("testuser", savedUser.getUsername(), "Username should match");

        User retrievedUser = userService.findByUsername("testuser");
        assertNotNull(retrievedUser, "Retrieved user should not be null");
        assertEquals(savedUser.getId(), retrievedUser.getId(), "Retrieved user ID should match saved user ID");
    }

    @Test
    @Transactional
    void testCoachCreationAndRetrieval() {
        Coach coach = new Coach();
        coach.setName("Test Coach");
        coach.setExperienceYears(5);
        Coach savedCoach = coachService.saveCoach(coach);

        assertNotNull(savedCoach, "Saved coach should not be null");
        assertNotNull(savedCoach.getId(), "Saved coach ID should not be null");
        assertEquals("Test Coach", savedCoach.getName(), "Coach name should match");

        Coach retrievedCoach = coachService.getCoach(savedCoach.getId());
        assertNotNull(retrievedCoach, "Retrieved coach should not be null");
        assertEquals(savedCoach.getId(), retrievedCoach.getId(), "Retrieved coach ID should match");
    }

    @Test
    @Transactional
    void testTeamCreationAndAssignment() {
        Coach coach = new Coach();
        coach.setName("Team Coach");
        coach.setExperienceYears(10);
        Coach savedCoach = coachService.saveCoach(coach);

        Team team = new Team();
        team.setName("Test Team");
        team.setCoach(savedCoach);
        Team savedTeam = teamService.saveTeam(team);

        assertNotNull(savedTeam, "Saved team should not be null");
        assertNotNull(savedTeam.getId(), "Saved team ID should not be null");
        assertEquals("Test Team", savedTeam.getName(), "Team name should match");
        assertNotNull(savedTeam.getCoach(), "Team coach should not be null");
        assertEquals(savedCoach.getId(), savedTeam.getCoach().getId(), "Team coach ID should match");

        Team retrievedTeam = teamService.getTeam(savedTeam.getId());
        assertNotNull(retrievedTeam, "Retrieved team should not be null");
        assertEquals(savedTeam.getId(), retrievedTeam.getId(), "Retrieved team ID should match");
    }

    @Test
    @Transactional
    void testPlayerCreationAndAssignment() {
        Coach coach = new Coach();
        coach.setName("Player Team Coach");
        coach.setExperienceYears(3);
        coachService.saveCoach(coach);

        Team team = new Team();
        team.setName("Player Test Team");
        team.setCoach(coach);
        teamService.saveTeam(team);

        Player player = new Player();
        player.setName("Test Player");
        player.setPosition("Guard");
        player.setJerseyNumber(23);
        player.setSkillLevel("High");
        player.setPointsPerGame(25.5);
        player.setTeam(team);
        Player savedPlayer = playerService.savePlayer(player);

        assertNotNull(savedPlayer, "Saved player should not be null");
        assertNotNull(savedPlayer.getId(), "Saved player ID should not be null");
        assertEquals("Test Player", savedPlayer.getName(), "Player name should match");
        assertNotNull(savedPlayer.getTeam(), "Player team should not be null");
        assertEquals(team.getId(), savedPlayer.getTeam().getId(), "Player team ID should match");

        Player retrievedPlayer = playerService.getPlayer(savedPlayer.getId());
        assertNotNull(retrievedPlayer, "Retrieved player should not be null");
        assertEquals(savedPlayer.getId(), retrievedPlayer.getId(), "Retrieved player ID should match");

        // Test deletion
        playerService.deletePlayer(savedPlayer.getId());
        Player deletedPlayer = playerService.getPlayer(savedPlayer.getId());
        assertNull(deletedPlayer, "Deleted player should be null");
    }
}
