package cz.uhk.kpro2.service;

import cz.uhk.kpro2.model.Coach;
import cz.uhk.kpro2.model.Team;
import cz.uhk.kpro2.repository.CoachRepository;
import cz.uhk.kpro2.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoachServiceImplTest {

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private CoachServiceImpl coachService;

    private Coach coach1;
    private Coach coach2;
    private Team team1;
    private Team team2_no_coach;

    @BeforeEach // Ensure @BeforeEach is present
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coach1 = new Coach();
        coach1.setId(1L);
        coach1.setName("John Doe");
        coach1.setExperienceYears(10);

        coach2 = new Coach();
        coach2.setId(2L);
        coach2.setName("Jane Smith");
        coach2.setExperienceYears(5);
        
        team1 = new Team();
        team1.setId(1L);
        team1.setName("Team Alpha");
        team1.setCoach(coach1); // coach1 is coaching team1

        team2_no_coach = new Team();
        team2_no_coach.setId(2L);
        team2_no_coach.setName("Team Beta");
        team2_no_coach.setCoach(null); // This team has no coach initially
    }

    @Test
    void testGetAllCoaches() {
        List<Coach> coaches = new ArrayList<>();
        coaches.add(coach1);
        coaches.add(coach2);
        when(coachRepository.findAll()).thenReturn(coaches);

        List<Coach> result = coachService.getAllCoaches();
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        verify(coachRepository, times(1)).findAll();
    }

    @Test
    void testGetCoach_existing() {
        when(coachRepository.findById(1L)).thenReturn(Optional.of(coach1));
        Coach result = coachService.getCoach(1L);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(coachRepository, times(1)).findById(1L);
    }

    @Test
    void testGetCoach_nonExisting() {
        when(coachRepository.findById(3L)).thenReturn(Optional.empty());
        Coach result = coachService.getCoach(3L);
        assertNull(result);
        verify(coachRepository, times(1)).findById(3L);
    }

    @Test
    void testSaveCoach_new() {
        Coach newCoach = new Coach();
        newCoach.setName("Mike Brown");
        newCoach.setExperienceYears(15);
        when(coachRepository.save(any(Coach.class))).thenReturn(newCoach);

        Coach result = coachService.saveCoach(newCoach);
        assertNotNull(result);
        assertEquals("Mike Brown", result.getName());
        assertEquals(15, result.getExperienceYears());
        verify(coachRepository, times(1)).save(newCoach);
    }
    
    @Test
    void testSaveCoach_update() {
        coach1.setExperienceYears(12); // Modify existing coach
        when(coachRepository.save(coach1)).thenReturn(coach1);

        Coach result = coachService.saveCoach(coach1);
        assertNotNull(result);
        assertEquals(12, result.getExperienceYears());
        verify(coachRepository, times(1)).save(coach1);
    }

    @Test
    void testDeleteCoach_existing_noTeamsAssociated() {
        Coach coachToDelete = new Coach(); // A coach not in setUp, not associated with team1
        coachToDelete.setId(3L);
        coachToDelete.setName("Coach ToDelete");
        coachToDelete.setExperienceYears(2);
        
        when(coachRepository.findById(3L)).thenReturn(Optional.of(coachToDelete));
        // Simulate that teamRepository.findAll() returns teams not coached by coachToDelete
        when(teamRepository.findAll()).thenReturn(List.of(team1, team2_no_coach)); 
        doNothing().when(coachRepository).deleteById(3L);

        coachService.deleteCoach(3L);

        verify(coachRepository, times(1)).findById(3L);
        verify(teamRepository, times(1)).findAll(); // Service checks all teams
        verify(teamRepository, never()).save(any(Team.class)); // No teams should be updated
        verify(coachRepository, times(1)).deleteById(3L);
    }
    
    @Test
    void testDeleteCoach_existing_withAssociatedTeam_disassociation() {
        // coach1 is associated with team1 in setUp
        when(coachRepository.findById(1L)).thenReturn(Optional.of(coach1));
        // teamRepository.findAll() will be called by the service to find teams coached by coach1
        when(teamRepository.findAll()).thenReturn(List.of(team1, team2_no_coach)); 
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(coachRepository).deleteById(1L);

        coachService.deleteCoach(1L);

        verify(coachRepository, times(1)).findById(1L);
        verify(teamRepository, times(1)).findAll();
        assertNull(team1.getCoach(), "Team1 should no longer have coach1 assigned.");
        verify(teamRepository, times(1)).save(team1); // Verify team1 was saved after disassociating coach
        verify(coachRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteCoach_nonExisting() {
        when(coachRepository.findById(99L)).thenReturn(Optional.empty());
        
        coachService.deleteCoach(99L);
        
        verify(coachRepository, times(1)).findById(99L);
        // teamRepository.findAll() should not be called if coach is not found
        verify(teamRepository, never()).findAll(); 
        verify(coachRepository, never()).deleteById(anyLong());
        verify(teamRepository, never()).save(any(Team.class));
    }
}
