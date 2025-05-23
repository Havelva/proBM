package cz.uhk.kpro2.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Player name cannot be empty.")
    @Size(max = 100, message = "Player name cannot exceed 100 characters.")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Position must be selected.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerPosition position;

    @NotNull(message = "Jersey number is required.")
    @Min(value = 0, message = "Jersey number cannot be negative.")
    @Max(value = 99, message = "Jersey number cannot exceed 99.")
    @Column(nullable = false)
    private Integer jerseyNumber;

    @NotEmpty(message = "Skill level cannot be empty.")
    @Size(max = 50, message = "Skill level cannot exceed 50 characters.")
    @Column(nullable = false, length = 50)
    private String skillLevel;

    @NotNull(message = "Points per game is required.")
    @DecimalMin(value = "0.0", message = "Points per game must be zero or positive.")
    @Column(nullable = false)
    private Double pointsPerGame;

    @NotNull(message = "Player must be associated with a team.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public PlayerPosition getPosition() { return position; }
    public void setPosition(PlayerPosition position) { this.position = position; }
    public Integer getJerseyNumber() { return jerseyNumber; }
    public void setJerseyNumber(Integer jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public String getSkillLevel() { return skillLevel; }
    public void setSkillLevel(String skillLevel) { this.skillLevel = skillLevel; }
    public Double getPointsPerGame() { return pointsPerGame; }
    public void setPointsPerGame(Double pointsPerGame) { this.pointsPerGame = pointsPerGame; }
    public Team getTeam() { return team; }
    public void setTeam(Team team) { this.team = team; }
}