package cc.kajus.amy.chunk;

import net.minecraft.server.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

/**
 * A taken {@link ChunkSnapshot}: the 16 sections plus the chunk's tile entities, serialised to NBT
 * when the snapshot is taken.
 */
public class CraftChunkSnapshot implements ChunkSnapshot {

    private final ChunkSectionSnapshot[] sections = new ChunkSectionSnapshot[16];
    private final List<NBTTagCompound> tileEntities = new ArrayList<>();

    public ChunkSectionSnapshot[] getSections() {
        return this.sections;
    }

    public List<NBTTagCompound> getTileEntities() {
        return this.tileEntities;
    }
}
