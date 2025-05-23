package cz.uhk.kpro2.service;
import java.util.List;

import cz.uhk.kpro2.model.Coach;
public interface CoachService {
    List<Coach> getAllCoaches();
    Coach getCoach(long id);
    Coach saveCoach(Coach coach);
    void deleteCoach(long id);
}