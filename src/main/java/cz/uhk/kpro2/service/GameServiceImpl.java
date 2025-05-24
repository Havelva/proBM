package cz.uhk.kpro2.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.model.Game;
import cz.uhk.kpro2.repository.GameRepository;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> getAllGames() {
        // You might want to sort this, e.g., by date
        return gameRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> getUpcomingGames() {
        return gameRepository.findByPlayedFalseOrderByGameDateTimeAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Game> getPlayedGames() {
        return gameRepository.findByPlayedTrueOrderByGameDateTimeDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Game getGame(long id) {
        return gameRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Game saveGame(Game game) {
        // We could add validation here, e.g., homeTeam != awayTeam
        return gameRepository.save(game);
    }

    @Override
    @Transactional
    public void deleteGame(long id) {
        gameRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Game updateScore(long gameId, int homeScore, int awayScore) {
        Game game = getGame(gameId);
        if (game != null) {
            game.setHomeTeamScore(homeScore);
            game.setAwayTeamScore(awayScore);
            game.setPlayed(true); // Mark as played when score is updated
            return gameRepository.save(game);
        }
        return null; // Or throw an exception
    }
}