package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Team; // Changed from Course to Team

import java.util.List;

public interface TeamService { // Changed from CourseService to TeamService
    List<Team> getAllTeams(); // Changed from getAllCourses to getAllTeams and Course to Team
    Team getTeam(long id); // Changed from getCourse to getTeam and Course to Team
    Team saveTeam(Team team); // Changed from saveCourse to saveTeam and Course to Team, and return type to Team
    void deleteTeam(long id); // Changed from deleteCourse to deleteTeam
}
