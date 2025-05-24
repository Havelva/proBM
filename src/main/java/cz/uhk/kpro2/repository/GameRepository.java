package cz.uhk.kpro2.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cz.uhk.kpro2.model.Game;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    // Find games involving a specific team (either home or away)
    List<Game> findByHomeTeamIdOrAwayTeamIdOrderByGameDateTimeDesc(Long homeTeamId, Long awayTeamId);

    // Find upcoming games
    List<Game> findByPlayedFalseOrderByGameDateTimeAsc();

    // Find played games
    List<Game> findByPlayedTrueOrderByGameDateTimeDesc();
}