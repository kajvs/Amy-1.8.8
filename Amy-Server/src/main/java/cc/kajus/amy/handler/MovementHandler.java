package cc.kajus.amy.handler;

import net.minecraft.server.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A movement listener.
 *
 * <p>{@code PlayerConnection} calls these for every accepted movement packet, right after the
 * {@code PlayerMoveEvent}. This core feeds its own lag compensator
 * ({@code cc.kajus.amy.hitdetection.LagCompensator}, held by {@code Amy#getLagCompensator()})
 * from that path: it keeps the recent positions of every player, which is what the bots ask for
 * when they want a player's position "as the other client saw it".</p>
 */
public interface MovementHandler {

    void handleUpdateLocation(Player player, Location to, Location from, PacketPlayInFlying packet);

    void handleUpdateRotation(Player player, Location to, Location from, PacketPlayInFlying packet);

}
