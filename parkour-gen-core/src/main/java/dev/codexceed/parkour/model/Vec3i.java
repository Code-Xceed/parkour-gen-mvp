package dev.codexceed.parkour.model;

/** Immutable integer 3D vector. Integer math only -> no float nondeterminism. */
public final class Vec3i {
    public final int x, y, z;
    public Vec3i(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    public Vec3i add(int dx, int dy, int dz) { return new Vec3i(x + dx, y + dy, z + dz); }

    /** Pack into a long for fast set membership (no ordered iteration used in gen). */
    public long pack() {
        return ((long)(x + (1 << 25)) & 0x3FFFFFFL) << 38
             | ((long)(y + (1 << 11)) & 0xFFFL) << 26
             | ((long)(z + (1 << 25)) & 0x3FFFFFFL);
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Vec3i v)) return false;
        return x == v.x && y == v.y && z == v.z;
    }
    @Override public int hashCode() { return (x * 31 + y) * 31 + z; }
    @Override public String toString() { return "(" + x + "," + y + "," + z + ")"; }
}
