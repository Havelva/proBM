package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Coach;

import java.util.List;

public interface CoachService {
    List<Coach> getAllCoaches();
    Coach getCoach(long id);
    Coach saveCoach(Coach coach);
    void deleteCoach(long id);
}
