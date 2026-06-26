package dev.codexceed.parkour.model;

/** Tunable generation parameters (part of the recipe). */
public final class GenParams {
    public final int tier;            // 1..10 target difficulty
    public final int length;          // number of jumps
    public final int checkpointEvery; // checkpoint cadence
    public final double turniness;    // 0..1 probability of changing facing
    public final String theme;        // block palette key

    public GenParams(int tier, int length, int checkpointEvery, double turniness, String theme) {
        this.tier = tier; this.length = length; this.checkpointEvery = checkpointEvery;
        this.turniness = turniness; this.theme = theme;
    }

    public static GenParams defaults(int tier) {
        return new GenParams(tier, 40, 8, 0.25, "quartz");
    }
}
