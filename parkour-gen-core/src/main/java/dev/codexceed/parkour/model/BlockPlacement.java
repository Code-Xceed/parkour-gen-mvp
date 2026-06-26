package dev.codexceed.parkour.model;

/** One block in the course at a local position. */
public final class BlockPlacement {
    public final Vec3i pos;
    public final String block;   // e.g. "minecraft:quartz_block"
    public final BlockRole role;
    public BlockPlacement(Vec3i pos, String block, BlockRole role) {
        this.pos = pos; this.block = block; this.role = role;
    }
}
