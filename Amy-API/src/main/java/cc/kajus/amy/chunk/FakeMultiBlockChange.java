package cc.kajus.amy.chunk;

import org.bukkit.entity.Player;

/**
 * A fake multi block change created by
 * {@link org.bukkit.Chunk#createFakeBlockUpdate(org.bukkit.Location[], int[], int[])}.
 * The blocks are only faked into a client's view, the world itself is untouched.
 *
 * <p>The gomoku
 * board uses it to show the ghost preview of a cell without ever changing a block.</p>
 */
public interface FakeMultiBlockChange {

	void sendTo(Player player);

}
