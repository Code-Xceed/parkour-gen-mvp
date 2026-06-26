package dev.codexceed.parkour.physics;

import dev.codexceed.parkour.prng.DeterministicRng;
import java.util.ArrayList;
import java.util.List;

/**
 * The move set + physics reach limits.
 * !! PLACEHOLDER CONSTANTS -- measure on Minecraft 26.1.2 and replace (plan section 4).
 */
public final class JumpTable {
    public static final List<Move> MOVES = List.of(
        new Move("walk_2",       2,  0,  0, false, 1),
        new Move("sprint_3",     3,  0,  0, true,  2),
        new Move("sprint_4",     4,  0,  0, true,  5),
        new Move("up1_3",        3,  1,  0, true,  4),
        new Move("up2_2",        2,  2,  0, true,  6),
        new Move("down_4",       4, -1,  0, true,  3),
        new Move("down2_5",      5, -2,  0, true,  3),
        new Move("diag_3r",      3,  0,  1, true,  4),
        new Move("diag_3l",      3,  0, -1, true,  4),
        new Move("headhitter_3", 3,  0,  0, true,  7),
        new Move("neo_2",        2,  1,  1, true,  8)
    );

    /** Max horizontal Euclidean reach for a given vertical delta. CALIBRATE on 26.1.2. */
    public static double maxReach(int dy) {
        if (dy >= 0) return Math.max(1.0, 4.0 - dy);     // flat 4, +1 -> 3, +2 -> 2
        return 4.0 + Math.min(3, -dy) * 0.5;             // drops extend reach
    }

    /** Pick a move weighted toward the target tier. Deterministic via rng. */
    public static Move pick(DeterministicRng rng, int tier) {
        List<Move> pool = new ArrayList<>();
        for (Move m : MOVES) {
            int w = Math.max(1, 6 - Math.abs(m.difficulty - tier));
            for (int i = 0; i < w; i++) pool.add(m);
        }
        return pool.get(rng.nextInt(pool.size()));
    }
}
