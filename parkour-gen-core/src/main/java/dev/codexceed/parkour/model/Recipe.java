package dev.codexceed.parkour.model;

/** The full deterministic recipe: gen_version + seed + params. */
public final class Recipe {
    public final String genVersion;
    public final long seed;
    public final GenParams params;

    public Recipe(String genVersion, long seed, GenParams params) {
        this.genVersion = genVersion; this.seed = seed; this.params = params;
    }

    public String toJson() {
        return "{\"gen_version\":\"" + genVersion + "\",\"seed\":" + seed
            + ",\"params\":{\"tier\":" + params.tier + ",\"length\":" + params.length
            + ",\"checkpointEvery\":" + params.checkpointEvery
            + ",\"turniness\":" + params.turniness
            + ",\"theme\":\"" + params.theme + "\"}}";
    }
}
