package dev.codexceed.parkour.verify;

import dev.codexceed.parkour.model.BlockPlacement;
import dev.codexceed.parkour.model.CourseStructure;
import dev.codexceed.parkour.model.Vec3i;
import dev.codexceed.parkour.physics.JumpTable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Verifies a course is solvable-by-construction (plan section 6). */
public final class ConstructiveVerifier {
    public static final class Result {
        public final boolean ok;
        public final List<String> problems;
        Result(boolean ok, List<String> p) { this.ok = ok; this.problems = p; }
    }

    public static Result verify(CourseStructure c) {
        List<String> problems = new ArrayList<>();
        if (c.start == null) problems.add("missing start");
        if (c.finish == null) problems.add("missing finish");
        if (c.placements.size() < 2) problems.add("course too short");

        Set<Long> seen = new HashSet<>();
        for (BlockPlacement bp : c.placements)
            if (!seen.add(bp.pos.pack())) problems.add("duplicate block at " + bp.pos);

        for (int i = 1; i < c.placements.size(); i++) {
            Vec3i a = c.placements.get(i - 1).pos, b = c.placements.get(i).pos;
            int dy = b.y - a.y;
            double dx = b.x - a.x, dz = b.z - a.z;
            double h = Math.sqrt(dx * dx + dz * dz);
            double max = JumpTable.maxReach(dy);
            if (h > max + 1e-9) problems.add("jump " + i + " too far h=" + h + " max=" + max + " dy=" + dy);
            if (h < 1.0) problems.add("jump " + i + " no progress");
            if (seen.contains(b.add(0, 1, 0).pack())) problems.add("jump " + i + " headroom +1 blocked");
            if (seen.contains(b.add(0, 2, 0).pack())) problems.add("jump " + i + " headroom +2 blocked");
        }
        return new Result(problems.isEmpty(), problems);
    }
}
