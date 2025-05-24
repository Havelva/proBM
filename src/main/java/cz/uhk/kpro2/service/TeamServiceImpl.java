package cz.uhk.kpro2.service;

import java.util.Comparator; // Add this import
import java.util.List; // Add this import
import java.util.Map;
import java.util.stream.Collectors; // Add this import

import org.springframework.beans.factory.annotation.Autowired; // Add this import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.dto.TeamStandingDto;
import cz.uhk.kpro2.model.Game; // Import the DTO
import cz.uhk.kpro2.model.Team;      // Import Game
import cz.uhk.kpro2.repository.TeamRepository;

@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    private final GameService gameService; // Inject GameService

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, GameService gameService) { // Add GameService to constructor
        this.teamRepository = teamRepository;
        this.gameService = gameService; // Initialize GameService
    }

    @Override @Transactional(readOnly = true)
    public List<Team> getAllTeams() { return teamRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public Team getTeam(long id) { return teamRepository.findById(id).orElse(null); }

    @Override @Transactional
    public Team saveTeam(Team team) { return teamRepository.save(team); }

    @Override @Transactional
    public void deleteTeam(long id) {
        // Before deleting a team, consider implications on games (e.g., set teams to null, or delete games)
        // For now, we'll just delete the team. This might cause issues if games reference it without cascading delete or nullification.
        // A more robust solution would handle associated games.
        teamRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamStandingDto> getTeamStandings() {
        List<Team> allTeams = getAllTeams();
        List<Game> playedGames = gameService.getPlayedGames(); // Use GameService

        Map<Long, TeamStandingDto> standingsMap = allTeams.stream()
                .collect(Collectors.toMap(Team::getId, TeamStandingDto::new));

        for (Game game : playedGames) {
            if (game.getHomeTeamScore() == null || game.getAwayTeamScore() == null) {
                continue; // Skip games without scores
            }

            TeamStandingDto homeTeamStanding = standingsMap.get(game.getHomeTeam().getId());
            TeamStandingDto awayTeamStanding = standingsMap.get(game.getAwayTeam().getId());

            if (homeTeamStanding == null || awayTeamStanding == null) {
                // This might happen if a team involved in a played game was deleted.
                // Log this or handle as appropriate. For now, skip.
                System.err.println("Warning: Team involved in game " + game.getId() + " not found in current standings map. Skipping game for standings calculation.");
                continue;
            }


            if (game.getHomeTeamScore() > game.getAwayTeamScore()) {
                homeTeamStanding.incrementWins();
                awayTeamStanding.incrementLosses();
            } else if (game.getAwayTeamScore() > game.getHomeTeamScore()) {
                awayTeamStanding.incrementWins();
                homeTeamStanding.incrementLosses();
            }
            // Draws are not typically counted in basketball win/loss, but could be if needed.
        }

        standingsMap.values().forEach(TeamStandingDto::calculateStats);

        return standingsMap.values().stream()
                .sorted(Comparator.comparing(TeamStandingDto::getWins).reversed()
                        .thenComparing(TeamStandingDto::getLosses)
                        .thenComparing(dto -> dto.getTeam().getName()))
                .collect(Collectors.toList());
    }
}