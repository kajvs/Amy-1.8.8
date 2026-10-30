package cc.kajus.amy.handler;

import net.minecraft.server.Packet;
import net.minecraft.server.PlayerConnection;

/**
 * A packet listener.
 *
 * <p>The plugin registers one of these for the gomoku board (it ray traces received movement
 * packets and cancels the block updates the server would otherwise send for the board cells, which
 * it fakes per player instead). It is registered through
 * {@code Amy.INSTANCE.addPacketHandler(...)} and dispatched by
 * {@code NetworkManager} (incoming) and {@code PlayerConnection} (outgoing).</p>
 *
 * <p>Every method has a default, so an implementation only overrides what it needs; a handler that
 * wants to be consulted on every packet must additionally return true from
 * {@link #handlesCancellable()}.</p>
 */
public interface PacketHandler {

    default void handleReceivedPacket(PlayerConnection connection, Packet<?> packet) {
    }

    default void handleSentPacket(PlayerConnection connection, Packet<?> packet) {
    }

    default boolean handleSentPacketCancellable(PlayerConnection connection, Packet<?> packet) {
        return true;
    }

    default boolean handleReceivedPacketCancellable(PlayerConnection connection, Packet<?> packet) {
        return true;
    }

    default boolean handlesCancellable() {
        return false;
    }

}
