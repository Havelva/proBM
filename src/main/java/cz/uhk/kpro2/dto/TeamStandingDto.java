package cz.uhk.kpro2.dto;

import cz.uhk.kpro2.model.Team;

public class TeamStandingDto {
    private Team team;
    private int wins;
    private int losses;
    private int gamesPlayed;
    private double winPercentage;

    public TeamStandingDto(Team team) {
        this.team = team;
        this.wins = 0;
        this.losses = 0;
        this.gamesPlayed = 0;
        this.winPercentage = 0.0;
    }

    // Getters
    public Team getTeam() { return team; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getGamesPlayed() { return gamesPlayed; }
    public double getWinPercentage() { return winPercentage; }

    // Setters and calculation methods
    public void setTeam(Team team) { this.team = team; }
    public void incrementWins() { this.wins++; }
    public void incrementLosses() { this.losses++; }

    public void calculateStats() {
        this.gamesPlayed = this.wins + this.losses;
        if (this.gamesPlayed > 0) {
            this.winPercentage = (double) this.wins / this.gamesPlayed * 100;
        } else {
            this.winPercentage = 0.0;
        }
    }
}