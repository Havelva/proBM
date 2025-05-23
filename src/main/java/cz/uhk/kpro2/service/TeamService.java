package cz.uhk.kpro2.service;
import java.util.List;

import cz.uhk.kpro2.model.Team;
public interface TeamService {
    List<Team> getAllTeams();
    Team getTeam(long id);
    Team saveTeam(Team team);
    void deleteTeam(long id);
}