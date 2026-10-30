package cc.kajus.amy.chunk;

import net.minecraft.server.NibbleArray;

/**
 * One 16x16x16 section of a {@link CraftChunkSnapshot}: the raw block id/light arrays are kept as
 * they were handed out by {@link net.minecraft.server.ChunkSection#createSnapshot()}, so restoring
 * puts back exactly the state the arena had.
 */
public class ChunkSectionSnapshot {

    private final int nonEmptyBlockCount;
    private final int tickingBlockCount;
    private final char[] blockIds;
    private final NibbleArray emittedLight;
    private final NibbleArray skyLight;

    public ChunkSectionSnapshot(
            int nonEmptyBlockCount,
            int tickingBlockCount,
            char[] blockIds,
            NibbleArray emittedLight,
            NibbleArray skyLight
    ) {
        this.nonEmptyBlockCount = nonEmptyBlockCount;
        this.tickingBlockCount = tickingBlockCount;
        this.blockIds = blockIds;
        this.emittedLight = emittedLight;
        this.skyLight = skyLight;
    }

    public final int getNonEmptyBlockCount() {
        return nonEmptyBlockCount;
    }

    public final int getTickingBlockCount() {
        return tickingBlockCount;
    }

    public final char[] getBlockIds() {
        return blockIds;
    }

    public final NibbleArray getEmittedLight() {
        return emittedLight;
    }

    public final NibbleArray getSkyLight() {
        return skyLight;
    }
}
