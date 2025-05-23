package cz.uhk.kpro2.model;

public enum PlayerPosition {
    POINT_GUARD("Point Guard", "PG"),
    SHOOTING_GUARD("Shooting Guard", "SG"),
    SMALL_FORWARD("Small Forward", "SF"),
    POWER_FORWARD("Power Forward", "PF"),
    CENTER("Center", "C");

    private final String fullName;
    private final String abbreviation;

    PlayerPosition(String fullName, String abbreviation) {
        this.fullName = fullName;
        this.abbreviation = abbreviation;
    }

    public String getFullName() { return fullName; }
    public String getAbbreviation() { return abbreviation; }

    @Override
    public String toString() { return fullName; }
}