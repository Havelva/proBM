package cz.uhk.kpro2.model;

import jakarta.persistence.*;

@Entity
@Table(name = "players") // Changed from "fuel_cells" to "players"
public class Player { // Changed from FuelCell to Player
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Changed from long to Long

    private String name; // Added name field
    private String position; // Changed from height to position
    private int jerseyNumber; // Changed from width to jerseyNumber
    private String skillLevel; // Changed from quality to skillLevel
    private double pointsPerGame; // Changed from holeOffcenter to pointsPerGame

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false) // Changed from "course_id" to "team_id"
    private Team team; // Changed from Course to Team

    public Long getId() { // Changed return type from long to Long
        return id;
    }

    public void setId(Long id) { // Changed parameter type from long to Long
        this.id = id;
    }

    public String getName() { // Added getter for name
        return name;
    }

    public void setName(String name) { // Added setter for name
        this.name = name;
    }

    public String getPosition() { // Changed from getHeight to getPosition
        return position;
    }

    public void setPosition(String position) { // Changed from setHeight to setPosition
        this.position = position;
    }

    public int getJerseyNumber() { // Changed from getWidth to getJerseyNumber
        return jerseyNumber;
    }

    public void setJerseyNumber(int jerseyNumber) { // Changed from setWidth to setJerseyNumber
        this.jerseyNumber = jerseyNumber;
    }

    public String getSkillLevel() { // Changed from getQuality to getSkillLevel
        return skillLevel;
    }

    public void setSkillLevel(String skillLevel) { // Changed from setQuality to setSkillLevel
        this.skillLevel = skillLevel;
    }

    public double getPointsPerGame() { // Changed from getHoleOffcenter to getPointsPerGame
        return pointsPerGame;
    }

    public void setPointsPerGame(double pointsPerGame) { // Changed from setHoleOffcenter to setPointsPerGame
        this.pointsPerGame = pointsPerGame;
    }

    public Team getTeam() { // Changed from getCourse to getTeam
        return team;
    }

    public void setTeam(Team team) { // Changed from setCourse to setTeam
        this.team = team;
    }
}
