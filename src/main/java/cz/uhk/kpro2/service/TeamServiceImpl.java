package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.repository.TeamRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;

    public TeamServiceImpl(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    public Team getTeam(long id) {
        Optional<Team> team = teamRepository.findById(id);
        return team.orElse(null);
    }

    @Override
    public Team saveTeam(Team team) {
        return teamRepository.save(team);
    }

    @Override
    public void deleteTeam(long id) {
        teamRepository.deleteById(id);
    }
}
