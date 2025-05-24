package cz.uhk.kpro2.service;

import java.util.List;

import cz.uhk.kpro2.model.Game;

public interface GameService {

    /**
     * Retrieves all games, usually ordered by date.
     * @return A list of all games.
     */
    List<Game> getAllGames();

    /**
     * Retrieves upcoming games (not yet played).
     * @return A list of upcoming games, ordered by date.
     */
    List<Game> getUpcomingGames();

    /**
     * Retrieves games that have already been played.
     * @return A list of played games, ordered by date (most recent first).
     */
    List<Game> getPlayedGames();

    /**
     * Retrieves a specific game by its ID.
     * @param id The ID of the game to retrieve.
     * @return The game, or null if not found.
     */
    Game getGame(long id);

    /**
     * Saves a game, handling both new and existing games.
     * @param game The game to save.
     * @return The saved game (with potentially updated ID or state).
     */
    Game saveGame(Game game);

    /**
     * Deletes a game by its ID.
     * @param id The ID of the game to delete.
     */
    void deleteGame(long id);

    /**
     * Updates the score of a game.
     * @param gameId The ID of the game to update.
     * @param homeScore The score of the home team.
     * @param awayScore The score of the away team.
     * @return The updated game, or null if the game wasn't found.
     */
    Game updateScore(long gameId, int homeScore, int awayScore);

}