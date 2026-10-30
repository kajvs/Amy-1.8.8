package cc.kajus.amy.chunk;

/**
 * A full chunk snapshot (blocks, light and tile entities) which can be restored onto the chunk it
 * was taken from.
 *
 * <p>Take a snapshot with {@link org.bukkit.Chunk#takeSnapshot()} and restore it with
 * {@link org.bukkit.Chunk#restoreSnapshot(ChunkSnapshot)}. This is the same API MineHQ based server
 * forks expose for arena resetting (PotPvP &amp; co): the arena takes a snapshot when a match starts
 * and puts it back between rounds and when the match ends.</p>
 */
public interface ChunkSnapshot {

}
