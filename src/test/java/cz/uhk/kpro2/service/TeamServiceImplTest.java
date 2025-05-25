package cz.uhk.kpro2.service;

import cz.uhk.kpro2.dto.TeamStandingDto;
import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.model.Game;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TeamServiceImplTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private GameService gameService; // Mock GameService as it's a dependency of TeamServiceImpl

    @InjectMocks
    private TeamServiceImpl teamService;

    private Team team1;
    private Team team2;
    private Coach coach1;

    @BeforeEach // Ensure @BeforeEach is present
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coach1 = new Coach();
        coach1.setId(1L);
        coach1.setName("Coach Test");
        coach1.setExperienceYears(5); // Assuming Coach has experienceYears

        team1 = new Team();
        team1.setId(1L);
        team1.setName("Team Alpha");
        team1.setCoach(coach1); // Example: set a coach

        team2 = new Team();
        team2.setId(2L);
        team2.setName("Team Beta");
    }

    @Test
    void testGetAllTeams() {
        List<Team> teams = new ArrayList<>();
        teams.add(team1);
        teams.add(team2);
        when(teamRepository.findAll()).thenReturn(teams);

        List<Team> result = teamService.getAllTeams();
        assertEquals(2, result.size());
        assertEquals("Team Alpha", result.get(0).getName());
        verify(teamRepository, times(1)).findAll();
    }

    @Test
    void testGetTeam_existing() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        Team result = teamService.getTeam(1L);
        assertNotNull(result);
        assertEquals("Team Alpha", result.getName());
        verify(teamRepository, times(1)).findById(1L);
    }

    @Test
    void testGetTeam_nonExisting() {
        when(teamRepository.findById(3L)).thenReturn(Optional.empty());
        Team result = teamService.getTeam(3L);
        assertNull(result);
        verify(teamRepository, times(1)).findById(3L);
    }

    @Test
    void testSaveTeam_new() {
        Team newTeam = new Team();
        newTeam.setName("Team Gamma");
        when(teamRepository.save(any(Team.class))).thenReturn(newTeam);

        Team result = teamService.saveTeam(newTeam);
        assertNotNull(result);
        assertEquals("Team Gamma", result.getName());
        verify(teamRepository, times(1)).save(newTeam);
    }
    
    @Test
    void testSaveTeam_update() {
        team1.setName("Team Alpha Updated"); // Modify existing team's name
        when(teamRepository.save(team1)).thenReturn(team1);

        Team result = teamService.saveTeam(team1);
        assertNotNull(result);
        assertEquals("Team Alpha Updated", result.getName());
        verify(teamRepository, times(1)).save(team1);
    }

    @Test
    void testDeleteTeam_existing() {
        // Mocking existsById is not strictly necessary if deleteById doesn't rely on it
        // and simply proceeds (or throws an EmptyResultDataAccessException if ID not found,
        // which Spring Data JPA does). For this test, we assume the service calls deleteById.
        doNothing().when(teamRepository).deleteById(1L);
        
        teamService.deleteTeam(1L);
        // Verify that deleteById was called.
        // If the service had logic to check existence first, we'd mock that (e.g., when(teamRepository.existsById(1L)).thenReturn(true);)
        verify(teamRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTeam_nonExisting() {
        // Similar to above, Spring Data's deleteById might throw an exception if the ID doesn't exist.
        // If the service is expected to handle this gracefully (e.g., catch and log), the test would change.
        // If it's expected to not call delete if it pre-checks, then:
        // when(teamRepository.existsById(3L)).thenReturn(false); 
        // teamService.deleteTeam(3L);
        // verify(teamRepository, never()).deleteById(3L);
        
        // For now, assume deleteById is called and might throw an exception (which we are not testing here)
        // or does nothing if the underlying JPA provider handles non-existent IDs silently during delete.
        doNothing().when(teamRepository).deleteById(3L); // Mock the call
        teamService.deleteTeam(3L);
        verify(teamRepository, times(1)).deleteById(3L); // Verify it was called
    }

    @Test
    void testGetTeamStandings() {
        // Prepare mock teams
        when(teamRepository.findAll()).thenReturn(Arrays.asList(team1, team2));

        // Prepare mock games
        Game game1_2 = new Game(); // Team1 (Home) vs Team2 (Away)
        game1_2.setId(1L);
        game1_2.setHomeTeam(team1);
        game1_2.setAwayTeam(team2);
        game1_2.setHomeTeamScore(3); // Team1 wins
        game1_2.setAwayTeamScore(1);
        game1_2.setPlayed(true);

        Game game2_1 = new Game(); // Team2 (Home) vs Team1 (Away)
        game2_1.setId(2L);
        game2_1.setHomeTeam(team2);
        game2_1.setAwayTeam(team1);
        game2_1.setHomeTeamScore(2); // Team2 wins
        game2_1.setAwayTeamScore(0);
        game2_1.setPlayed(true);
        
        Game game_not_played = new Game();
        game_not_played.setId(3L);
        game_not_played.setHomeTeam(team1);
        game_not_played.setAwayTeam(team2);
        game_not_played.setPlayed(false); // Not played

        Game game_no_score = new Game();
        game_no_score.setId(4L);
        game_no_score.setHomeTeam(team1);
        game_no_score.setAwayTeam(team2);
        game_no_score.setPlayed(true); // Played but no scores
        // game_no_score.setHomeTeamScore(null); // Scores are null by default

        when(gameService.getPlayedGames()).thenReturn(Arrays.asList(game1_2, game2_1, game_no_score));

        List<TeamStandingDto> standings = teamService.getTeamStandings();

        assertNotNull(standings);
        assertEquals(2, standings.size(), "Should be two teams in standings");

        // Team1: 1 win, 1 loss
        // Team2: 1 win, 1 loss
        // Order might depend on secondary sort criteria (name if wins/losses are equal)
        // Team Alpha vs Team Beta - Alpha comes first alphabetically

        TeamStandingDto standingTeam1 = standings.stream().filter(s -> s.getTeam().getName().equals("Team Alpha")).findFirst().orElse(null);
        TeamStandingDto standingTeam2 = standings.stream().filter(s -> s.getTeam().getName().equals("Team Beta")).findFirst().orElse(null);

        assertNotNull(standingTeam1, "Standing for Team Alpha not found");
        assertEquals("Team Alpha", standingTeam1.getTeam().getName());
        assertEquals(1, standingTeam1.getWins());
        assertEquals(1, standingTeam1.getLosses());
        assertEquals(2, standingTeam1.getGamesPlayed()); // game1_2, game2_1 (game_no_score is skipped for win/loss)
        // Points are not part of TeamStandingDto in the provided service impl, but win percentage is.
        // assertEquals(0.5, standingTeam1.getWinPercentage(), 0.001);


        assertNotNull(standingTeam2, "Standing for Team Beta not found");
        assertEquals("Team Beta", standingTeam2.getTeam().getName());
        assertEquals(1, standingTeam2.getWins());
        assertEquals(1, standingTeam2.getLosses());
        assertEquals(2, standingTeam2.getGamesPlayed());
        // assertEquals(0.5, standingTeam2.getWinPercentage(), 0.001);
        
        // Check order (Team Alpha should be before Team Beta due to name sorting as wins/losses are equal)
        assertTrue(standings.indexOf(standingTeam1) < standings.indexOf(standingTeam2), "Team Alpha should be ranked higher or equal (due to name) than Team Beta");


        verify(teamRepository, times(1)).findAll();
        verify(gameService, times(1)).getPlayedGames();
    }
}
