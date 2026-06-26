package dev.codexceed.parkour.generator;

import dev.codexceed.parkour.model.Vec3i;

/** Cardinal facing with forward + right(side) unit vectors on the XZ plane. */
public enum Facing {
    NORTH(0, -1), EAST(1, 0), SOUTH(0, 1), WEST(-1, 0);
    public final int fx, fz;
    Facing(int fx, int fz) { this.fx = fx; this.fz = fz; }
    public int sx() { return -fz; }   // 90 deg clockwise
    public int sz() { return fx; }
    public Facing turnLeft()  { return values()[(ordinal() + 3) % 4]; }
    public Facing turnRight() { return values()[(ordinal() + 1) % 4]; }

    public Vec3i apply(Vec3i base, int forward, int up, int side) {
        int dx = fx * forward + sx() * side;
        int dz = fz * forward + sz() * side;
        return base.add(dx, up, dz);
    }
}
