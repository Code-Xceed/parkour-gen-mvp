package dev.codexceed.parkour.difficulty;

import dev.codexceed.parkour.model.CourseStructure;
import dev.codexceed.parkour.model.GenParams;
import dev.codexceed.parkour.model.Vec3i;

/** Scores difficulty + extracts a feature vector. Recalibrate weights from telemetry. */
public final class DifficultyModel {
    public static int score(CourseStructure c, GenParams p) {
        double raw = p.tier * 0.6 + turnFrequency(c) * 3.0 + avgStep(c) * 0.5;
        return Math.max(1, Math.min(10, (int) Math.round(raw)));
    }

    public static double[] features(CourseStructure c) {
        return new double[]{ c.placements.size(), avgStep(c), turnFrequency(c) };
    }

    private static double avgStep(CourseStructure c) {
        if (c.placements.size() < 2) return 0;
        double sum = 0; int n = 0;
        for (int i = 1; i < c.placements.size(); i++) {
            Vec3i a = c.placements.get(i - 1).pos, b = c.placements.get(i).pos;
            double dx = b.x - a.x, dz = b.z - a.z;
            sum += Math.sqrt(dx * dx + dz * dz); n++;
        }
        return n == 0 ? 0 : sum / n;
    }

    private static double turnFrequency(CourseStructure c) {
        if (c.placements.size() < 3) return 0;
        int turns = 0;
        for (int i = 2; i < c.placements.size(); i++) {
            Vec3i a = c.placements.get(i - 2).pos, b = c.placements.get(i - 1).pos, d = c.placements.get(i).pos;
            int dx1 = b.x - a.x, dz1 = b.z - a.z, dx2 = d.x - b.x, dz2 = d.z - b.z;
            if (dx1 * dz2 - dz1 * dx2 != 0) turns++;
        }
        return (double) turns / (c.placements.size() - 2);
    }
}
