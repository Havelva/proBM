package cz.uhk.kpro2.service;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.repository.CoachRepository;
import cz.uhk.kpro2.repository.TeamRepository;
import cz.uhk.kpro2.model.Team;

@Service
public class CoachServiceImpl implements CoachService {
    private final CoachRepository coachRepository;
    private final TeamRepository teamRepository;

    @Autowired
    public CoachServiceImpl(CoachRepository coachRepository, TeamRepository teamRepository) {
        this.coachRepository = coachRepository;
        this.teamRepository = teamRepository;
    }

    @Override @Transactional(readOnly = true)
    public List<Coach> getAllCoaches() { return coachRepository.findAll(); }

    @Override @Transactional(readOnly = true)
    public Coach getCoach(long id) { return coachRepository.findById(id).orElse(null); }

    @Override @Transactional
    public Coach saveCoach(Coach coach) { return coachRepository.save(coach); }

    @Override @Transactional
    public void deleteCoach(long id) {
        Coach coachToDelete = coachRepository.findById(id).orElse(null);
        if (coachToDelete != null) {
            // Disassociate coach from all teams they manage
            List<Team> allTeams = teamRepository.findAll();
            for (Team team : allTeams) {
                if (team.getCoach() != null && team.getCoach().getId().equals(id)) {
                    team.setCoach(null);
                    teamRepository.save(team);
                }
            }
            coachRepository.deleteById(id);
        }
    }
}