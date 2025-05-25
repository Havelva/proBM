package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Game;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private Game game1;
    private Game game2;
    private Team team1;
    private Team team2;
    private Team team3;

    @BeforeEach
    void setUp() {
        team1 = new Team();
        team1.setId(1L);
        team1.setName("Home Team");

        team2 = new Team();
        team2.setId(2L);
        team2.setName("Away Team");
        
        team3 = new Team();
        team3.setId(3L);
        team3.setName("Another Team");

        game1 = new Game();
        game1.setId(1L);
        game1.setHomeTeam(team1);
        game1.setAwayTeam(team2);
        game1.setGameDateTime(LocalDateTime.now().plusDays(1));
        game1.setPlayed(false);

        game2 = new Game();
        game2.setId(2L);
        game2.setHomeTeam(team2);
        game2.setAwayTeam(team3);
        game2.setGameDateTime(LocalDateTime.now().minusDays(1));
        game2.setPlayed(true);
        game2.setHomeTeamScore(100);
        game2.setAwayTeamScore(90);
    }

    @Test
    void getAllGames() {
        when(gameRepository.findAll()).thenReturn(Arrays.asList(game1, game2));
        List<Game> games = gameService.getAllGames();
        assertNotNull(games);
        assertEquals(2, games.size());
        verify(gameRepository, times(1)).findAll();
    }

    @Test
    void getUpcomingGames() {
        when(gameRepository.findByPlayedFalseOrderByGameDateTimeAsc()).thenReturn(Collections.singletonList(game1));
        List<Game> upcomingGames = gameService.getUpcomingGames();
        assertNotNull(upcomingGames);
        assertEquals(1, upcomingGames.size());
        assertEquals(game1.getId(), upcomingGames.get(0).getId());
        assertFalse(upcomingGames.get(0).isPlayed());
        verify(gameRepository, times(1)).findByPlayedFalseOrderByGameDateTimeAsc();
    }

    @Test
    void getPlayedGames() {
        when(gameRepository.findByPlayedTrueOrderByGameDateTimeDesc()).thenReturn(Collections.singletonList(game2));
        List<Game> playedGames = gameService.getPlayedGames();
        assertNotNull(playedGames);
        assertEquals(1, playedGames.size());
        assertEquals(game2.getId(), playedGames.get(0).getId());
        assertTrue(playedGames.get(0).isPlayed());
        verify(gameRepository, times(1)).findByPlayedTrueOrderByGameDateTimeDesc();
    }

    @Test
    void getGame_existing() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));
        Game foundGame = gameService.getGame(1L);
        assertNotNull(foundGame);
        assertEquals(game1.getId(), foundGame.getId());
        verify(gameRepository, times(1)).findById(1L);
    }

    @Test
    void getGame_nonExisting() {
        when(gameRepository.findById(3L)).thenReturn(Optional.empty());
        Game foundGame = gameService.getGame(3L);
        assertNull(foundGame);
        verify(gameRepository, times(1)).findById(3L);
    }

    @Test
    void saveGame() {
        when(gameRepository.save(any(Game.class))).thenReturn(game1);
        Game savedGame = gameService.saveGame(new Game());
        assertNotNull(savedGame);
        assertEquals(game1.getId(), savedGame.getId());
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    void deleteGame_existing() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));
        doNothing().when(gameRepository).delete(game1);
        gameService.deleteGame(1L);
        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).delete(game1);
    }

    @Test
    void deleteGame_nonExisting() {
        when(gameRepository.findById(3L)).thenReturn(Optional.empty());
        gameService.deleteGame(3L);
        verify(gameRepository, times(1)).findById(3L);
        verify(gameRepository, never()).delete(any(Game.class));
    }

    @Test
    void updateScore_existingGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game1));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Game updatedGame = gameService.updateScore(1L, 110, 105);

        assertNotNull(updatedGame);
        assertEquals(1L, updatedGame.getId());
        assertEquals(110, updatedGame.getHomeTeamScore());
        assertEquals(105, updatedGame.getAwayTeamScore());
        assertTrue(updatedGame.isPlayed());
        verify(gameRepository, times(1)).findById(1L);
        verify(gameRepository, times(1)).save(game1);
    }

    @Test
    void updateScore_nonExistingGame() {
        when(gameRepository.findById(3L)).thenReturn(Optional.empty());
        Game result = gameService.updateScore(3L, 100, 90);
        assertNull(result);
        verify(gameRepository, times(1)).findById(3L);
        verify(gameRepository, never()).save(any(Game.class));
    }
}
