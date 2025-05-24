package cz.uhk.kpro2.service;
import java.util.List;

import cz.uhk.kpro2.dto.TeamStandingDto; // Import the new DTO
import cz.uhk.kpro2.model.Team;

public interface TeamService {
    List<Team> getAllTeams();
    Team getTeam(long id);
    Team saveTeam(Team team);
    void deleteTeam(long id);

    // New method for team standings
    List<TeamStandingDto> getTeamStandings();
}