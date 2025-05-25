package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Player;
import cz.uhk.kpro2.model.PlayerPosition; 
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlayerServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerServiceImpl playerService;

    private Player player1;
    private Player player2;
    private Team team1;
    private Team team2_placeholder;

    @BeforeEach // Ensure @BeforeEach is present
    void setUp() {
        MockitoAnnotations.openMocks(this);

        team1 = new Team();
        team1.setId(1L);
        team1.setName("Team Alpha");

        team2_placeholder = new Team(); 
        team2_placeholder.setId(2L);
        team2_placeholder.setName("Team Beta");

        player1 = new Player();
        player1.setId(1L);
        player1.setName("John Doe"); 
        player1.setPosition(PlayerPosition.SMALL_FORWARD); // Corrected: e.g., SMALL_FORWARD
        player1.setJerseyNumber(10);
        player1.setSkillLevel("High");
        player1.setPointsPerGame(20.5);
        player1.setAssistsPerGame(5.0);
        player1.setReboundsPerGame(7.0);
        player1.setStealsPerGame(1.5);
        player1.setBlocksPerGame(0.5);
        player1.setTeam(team1); 

        player2 = new Player();
        player2.setId(2L);
        player2.setName("Jane Smith");
        player2.setPosition(PlayerPosition.POINT_GUARD); // Corrected: e.g., POINT_GUARD
        player2.setJerseyNumber(5);
        player2.setSkillLevel("Medium");
        player2.setPointsPerGame(15.0);
        player2.setAssistsPerGame(8.0);
        player2.setReboundsPerGame(3.0);
        player2.setStealsPerGame(2.0);
        player2.setBlocksPerGame(0.2);
        player2.setTeam(team2_placeholder); 
    }

    @Test
    void testGetAllPlayers() {
        List<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);
        when(playerRepository.findAll()).thenReturn(players);

        List<Player> result = playerService.getAllPlayers();
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(playerRepository, times(1)).findAll();
    }

    @Test
    void testGetPlayer_existing() {
        when(playerRepository.findById(1L)).thenReturn(Optional.of(player1));
        Player result = playerService.getPlayer(1L);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(playerRepository, times(1)).findById(1L);
    }

    @Test
    void testGetPlayer_nonExisting() {
        when(playerRepository.findById(3L)).thenReturn(Optional.empty());
        Player result = playerService.getPlayer(3L);
        assertNull(result);
        verify(playerRepository, times(1)).findById(3L);
    }

    @Test
    void testSavePlayer() {
        Player newPlayer = new Player();
        newPlayer.setName("Mike Brown");
        newPlayer.setPosition(PlayerPosition.CENTER); // Corrected: e.g., CENTER
        newPlayer.setJerseyNumber(50);
        newPlayer.setSkillLevel("High");
        newPlayer.setPointsPerGame(18.0);
        newPlayer.setAssistsPerGame(2.0);
        newPlayer.setReboundsPerGame(10.0);
        newPlayer.setStealsPerGame(1.0);
        newPlayer.setBlocksPerGame(1.0);
        newPlayer.setTeam(team1); 

        when(playerRepository.save(any(Player.class))).thenReturn(newPlayer);

        Player result = playerService.savePlayer(newPlayer);
        assertNotNull(result);
        assertEquals("Mike Brown", result.getName());
        assertNotNull(result.getTeam());
        assertEquals("Team Alpha", result.getTeam().getName());
        verify(playerRepository, times(1)).save(newPlayer);
    }
    
    @Test
    void testSavePlayer_updateExisting() {
        player1.setSkillLevel("Very High");
        player1.setPointsPerGame(22.0);

        when(playerRepository.save(any(Player.class))).thenReturn(player1);

        Player result = playerService.savePlayer(player1); 
        assertNotNull(result);
        assertEquals("Very High", result.getSkillLevel());
        assertEquals(22.0, result.getPointsPerGame());
        verify(playerRepository, times(1)).save(player1);
    }

    @Test
    void testDeletePlayer_existing() {
        doNothing().when(playerRepository).deleteById(1L);
        
        playerService.deletePlayer(1L);
        verify(playerRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeletePlayer_nonExisting_throwsException() {
        doThrow(new org.springframework.dao.EmptyResultDataAccessException(1)).when(playerRepository).deleteById(99L);

        assertThrows(org.springframework.dao.EmptyResultDataAccessException.class, () -> {
            playerService.deletePlayer(99L);
        });
        
        verify(playerRepository, times(1)).deleteById(99L);
    }

    @Test
    void testGetPlayersByTeamId() {
        List<Player> team1Players = new ArrayList<>();
        team1Players.add(player1);
        when(playerRepository.findByTeamId(1L)).thenReturn(team1Players);

        List<Player> result = playerService.getPlayersByTeamId(1L);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(playerRepository, times(1)).findByTeamId(1L);
    }
    
    @Test
    void testGetPlayersByTeamId_nonExistingTeamOrNoPlayers() {
        when(playerRepository.findByTeamId(99L)).thenReturn(new ArrayList<>()); 

        List<Player> result = playerService.getPlayersByTeamId(99L);
        assertTrue(result.isEmpty());
        verify(playerRepository, times(1)).findByTeamId(99L);
    }
}
