package dev.cobblesword.nachospigot;

import java.util.Set;

import cc.kajus.amy.Amy;
import cc.kajus.amy.protocol.MovementListener;
import cc.kajus.amy.protocol.PacketListener;

@Deprecated
public class Nacho {

	private static Nacho INSTANCE;

	public Nacho() {
		INSTANCE = this;
	}

	public static Nacho get() {
		return INSTANCE == null ? new Nacho() : INSTANCE;
	}

	public void registerCommands() {

	}

	public void registerPacketListener(PacketListener packetListener) {
		Amy.getInstance().registerPacketListener(packetListener);
	}

	public void unregisterPacketListener(PacketListener packetListener) {
		Amy.getInstance().unregisterPacketListener(packetListener);
	}

	public Set<PacketListener> getPacketListeners() {
		return Amy.getInstance().getPacketListeners();
	}

	public void registerMovementListener(MovementListener movementListener) {
		Amy.getInstance().registerMovementListener(movementListener);
	}

	public void unregisterMovementListener(MovementListener movementListener) {
		Amy.getInstance().unregisterMovementListener(movementListener);
	}

	public Set<MovementListener> getMovementListeners() {
		return Amy.getInstance().getMovementListeners();
	}

}
