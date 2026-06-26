package dev.codexceed.parkour.generator;

import dev.codexceed.parkour.difficulty.DifficultyModel;
import dev.codexceed.parkour.model.*;
import dev.codexceed.parkour.physics.JumpTable;
import dev.codexceed.parkour.physics.Move;
import dev.codexceed.parkour.prng.DeterministicRng;

import java.util.HashSet;
import java.util.Set;

/** The incremental jump-grammar parkour generator (plan section 3). */
public final class ParkourGenerator {
    public static final String GEN_VERSION = "parkour_v1";
    private static final int START_Y = 64;
    private static final int MAX_ATTEMPTS = 16;

    public CourseStructure generate(Recipe recipe) {
        GenParams p = recipe.params;
        DeterministicRng rng = new DeterministicRng(recipe.seed);
        CourseStructure course = new CourseStructure();
        Set<Long> occupied = new HashSet<>();

        Vec3i cursor = new Vec3i(0, START_Y, 0);
        course.start = cursor;
        place(course, occupied, cursor, BlockRole.START, themeBlock(p.theme, BlockRole.START));

        Facing facing = Facing.SOUTH;

        for (int i = 1; i <= p.length; i++) {
            Vec3i landing = null;
            Facing chosen = facing;
            for (int a = 0; a < MAX_ATTEMPTS; a++) {
                Move m = JumpTable.pick(rng, p.tier);
                Facing f = facing;
                if (rng.nextDouble() < p.turniness) {
                    f = rng.nextBoolean() ? facing.turnLeft() : facing.turnRight();
                }
                Vec3i cand = f.apply(cursor, m.forward, m.up, m.side);
                if (isPlaceable(occupied, cand)) { landing = cand; chosen = f; break; }
            }
            if (landing == null) {
                Vec3i cand = facing.apply(cursor, 3, 0, 0); // safe fallback
                if (!isPlaceable(occupied, cand)) break;    // give up gracefully
                landing = cand;
            }
            facing = chosen;
            boolean cp = (i % p.checkpointEvery == 0);
            BlockRole role = cp ? BlockRole.CHECKPOINT : BlockRole.PATH;
            place(course, occupied, landing, role, themeBlock(p.theme, role));
            if (cp) course.checkpoints.add(landing);
            cursor = landing;
        }

        course.finish = cursor;
        if (!course.placements.isEmpty()) {
            int last = course.placements.size() - 1;
            BlockPlacement bp = course.placements.get(last);
            course.placements.set(last, new BlockPlacement(bp.pos, themeBlock(p.theme, BlockRole.FINISH), BlockRole.FINISH));
        }

        course.difficulty = DifficultyModel.score(course, p);
        course.featureVector = DifficultyModel.features(course);
        return course;
    }

    private boolean isPlaceable(Set<Long> occ, Vec3i pos) {
        return !occ.contains(pos.pack())
            && !occ.contains(pos.add(0, 1, 0).pack())
            && !occ.contains(pos.add(0, 2, 0).pack())
            && !occ.contains(pos.add(0, -1, 0).pack())
            && !occ.contains(pos.add(0, -2, 0).pack());
    }

    private void place(CourseStructure c, Set<Long> occ, Vec3i pos, BlockRole role, String block) {
        c.add(new BlockPlacement(pos, block, role));
        occ.add(pos.pack());
    }

    private String themeBlock(String theme, BlockRole role) {
        switch (role) {
            case CHECKPOINT: return "minecraft:gold_block";
            case START:      return "minecraft:emerald_block";
            case FINISH:     return "minecraft:diamond_block";
            default:         break;
        }
        switch (theme) {
            case "ice":        return "minecraft:packed_ice";
            case "deepslate":  return "minecraft:polished_deepslate";
            case "prismarine": return "minecraft:prismarine_bricks";
            default:           return "minecraft:quartz_block";
        }
    }
}
