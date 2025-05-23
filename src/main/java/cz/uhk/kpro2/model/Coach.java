package cz.uhk.kpro2.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "coaches") // Changed from "bos_members" to "coaches"
public class Coach { // Changed from BOSMember to Coach
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int experienceYears; // Added new field for coach's experience

    // Potentially add a list of teams coached by this coach
    // @OneToMany(mappedBy = "coach")
    // private List<Team> teams;

    public Long getId() {
        return id; // Allow null to be returned for new entities
    }
    public void setId(Long id) { // Changed parameter type from long to Long
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    // Getter and setter for teams if added
    // public List<Team> getTeams() {
    //     return teams;
    // }
    //
    // public void setTeams(List<Team> teams) {
    //     this.teams = teams;
    // }
}
