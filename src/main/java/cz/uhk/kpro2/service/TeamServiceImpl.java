package cz.uhk.kpro2.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.repository.TeamRepository;
@Service
public class TeamServiceImpl implements TeamService {
    private final TeamRepository teamRepository;
    @Autowired public TeamServiceImpl(TeamRepository teamRepository) { this.teamRepository = teamRepository; }
    @Override @Transactional(readOnly = true)
    public List<Team> getAllTeams() { return teamRepository.findAll(); }
    @Override @Transactional(readOnly = true)
    public Team getTeam(long id) { return teamRepository.findById(id).orElse(null); }
    @Override @Transactional
    public Team saveTeam(Team team) { return teamRepository.save(team); }
    @Override @Transactional
    public void deleteTeam(long id) { teamRepository.deleteById(id); }
}