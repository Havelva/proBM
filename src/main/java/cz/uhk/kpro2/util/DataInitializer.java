package cz.uhk.kpro2.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
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


@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final CoachService coachService;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final PasswordEncoder passwordEncoder;

    @Value("${default.admin.password}")
    private String adminPassword;

    @Value("${default.user.password}")
    private String userPassword;

    public DataInitializer(UserService userService, CoachService coachService, TeamService teamService, PlayerService playerService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.coachService = coachService;
        this.teamService = teamService;
        this.playerService = playerService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userService.findByUsername("admin") != null) {
            System.out.println("Sample data already initialized or admin exists.");
            return;
        }
        System.out.println("Initializing sample data...");

        // Create Users
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(adminPassword); // Raw password, will be encoded by service
        admin.setRoles("ROLE_ADMIN,ROLE_USER");
        userService.saveUser(admin);

        User user1 = new User();
        user1.setUsername("coach_k");
        user1.setPassword(userPassword);
        user1.setRoles("ROLE_USER");
        userService.saveUser(user1);

        User user2 = new User();
        user2.setUsername("manager_b");
        user2.setPassword(userPassword);
        user2.setRoles("ROLE_USER");
        userService.saveUser(user2);

        // Create Coaches
        Coach coachK = new Coach();
        coachK.setName("Phil Jackson");
        coachK.setExperienceYears(20);
        coachService.saveCoach(coachK);

        Coach coachB = new Coach();
        coachB.setName("Gregg Popovich");
        coachB.setExperienceYears(25);
        coachService.saveCoach(coachB);

        // Create Teams
        Team lions = new Team();
        lions.setName("Hradec Lions");
        lions.setCoach(coachK);
        lions.setMembers(List.of(admin, user1)); // Pass the persisted user objects
        teamService.saveTeam(lions); // Save team to get ID

        Team eagles = new Team();
        eagles.setName("Prague Eagles");
        eagles.setCoach(coachB);
        eagles.setMembers(List.of(user2));
        teamService.saveTeam(eagles); // Save team to get ID

        // Create Players with Detailed Stats
        Player p1 = new Player();
        p1.setName("Michael Jordan"); p1.setJerseyNumber(23); p1.setPointsPerGame(30.1);
        p1.setAssistsPerGame(5.3); p1.setReboundsPerGame(6.2); p1.setStealsPerGame(2.3); p1.setBlocksPerGame(0.8);
        p1.setPosition(PlayerPosition.SHOOTING_GUARD); p1.setSkillLevel("Legendary"); p1.setTeam(lions);
        playerService.savePlayer(p1);

        Player p2 = new Player();
        p2.setName("LeBron James"); p2.setJerseyNumber(6); p2.setPointsPerGame(27.1);
        p2.setAssistsPerGame(7.4); p2.setReboundsPerGame(7.5); p2.setStealsPerGame(1.6); p2.setBlocksPerGame(0.8);
        p2.setPosition(PlayerPosition.SMALL_FORWARD); p2.setSkillLevel("GOAT Candidate"); p2.setTeam(lions);
        playerService.savePlayer(p2);

        Player p3 = new Player();
        p3.setName("Tim Duncan"); p3.setJerseyNumber(21); p3.setPointsPerGame(19.0);
        p3.setAssistsPerGame(3.0); p3.setReboundsPerGame(10.8); p3.setStealsPerGame(0.7); p3.setBlocksPerGame(2.2);
        p3.setPosition(PlayerPosition.POWER_FORWARD); p3.setSkillLevel("Fundamental"); p3.setTeam(eagles);
        playerService.savePlayer(p3);

        Player p4 = new Player();
        p4.setName("Stephen Curry"); p4.setJerseyNumber(30); p4.setPointsPerGame(24.3);
        p4.setAssistsPerGame(6.5); p4.setReboundsPerGame(4.6); p4.setStealsPerGame(1.7); p4.setBlocksPerGame(0.2);
        p4.setPosition(PlayerPosition.POINT_GUARD); p4.setSkillLevel("Best Shooter Ever"); p4.setTeam(eagles);
        playerService.savePlayer(p4);

        System.out.println("Sample data initialized successfully.");
    }
}