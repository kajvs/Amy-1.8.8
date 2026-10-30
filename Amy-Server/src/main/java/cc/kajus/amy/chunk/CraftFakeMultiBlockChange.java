package cc.kajus.amy.chunk;

import net.minecraft.server.PacketPlayOutMultiBlockChange;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * A built multi block change packet that can be sent to one player at a time.
 */
public class CraftFakeMultiBlockChange implements FakeMultiBlockChange {

	private final PacketPlayOutMultiBlockChange packet;

	public CraftFakeMultiBlockChange(PacketPlayOutMultiBlockChange packet) {
		this.packet = packet;
	}

	@Override
	public void sendTo(Player player) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(this.packet);
	}
}
