package dev.codexceed.parkour.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/** The generated course: ordered block placements + metadata. */
public final class CourseStructure {
    public final List<BlockPlacement> placements = new ArrayList<>();
    public final List<Vec3i> checkpoints = new ArrayList<>();
    public Vec3i start;
    public Vec3i finish;
    public int difficulty;
    public double[] featureVector = new double[0];

    public void add(BlockPlacement p) { placements.add(p); }

    /** Stable canonical hash. Same recipe -> same hash forever (golden-test anchor). */
    public String stableHash() {
        StringBuilder sb = new StringBuilder();
        for (BlockPlacement p : placements) {
            sb.append(p.role).append(':').append(p.block).append(':')
              .append(p.pos.x).append(',').append(p.pos.y).append(',').append(p.pos.z).append(';');
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : d) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
