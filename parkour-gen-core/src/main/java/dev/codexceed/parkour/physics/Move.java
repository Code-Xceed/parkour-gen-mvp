package dev.codexceed.parkour.physics;

/**
 * A jump primitive. Offsets are facing-relative:
 *   forward = blocks toward facing, up = vertical, side = lateral.
 * CALIBRATE all reach/height values on Minecraft 26.1.2 (see plan section 4).
 */
public final class Move {
    public final String name;
    public final int forward, up, side;
    public final boolean sprint;
    public final int difficulty; // 1..10
    public Move(String name, int forward, int up, int side, boolean sprint, int difficulty) {
        this.name = name; this.forward = forward; this.up = up; this.side = side;
        this.sprint = sprint; this.difficulty = difficulty;
    }
}
