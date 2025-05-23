package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.repository.CoachRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;

    public CoachServiceImpl(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    public List<Coach> getAllCoaches() {
        return coachRepository.findAll();
    }


    @Override
    public Coach getCoach(long id) {
        Optional<Coach> coach = coachRepository.findById(id);
        return coach.orElse(null);
    }

    @Override
    public Coach saveCoach(Coach coach) {
        return coachRepository.save(coach);
    }

    @Override
    public void deleteCoach(long id) {
        coachRepository.deleteById(id);
    }
}
