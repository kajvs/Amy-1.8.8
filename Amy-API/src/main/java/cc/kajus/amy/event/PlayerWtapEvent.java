package cc.kajus.amy.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerWtapEvent extends Event {

	private static final HandlerList handlers = new HandlerList();
	private final Player player;
	private final boolean success;

	public PlayerWtapEvent(Player player, boolean success) {
		this.player = player;
		this.success = success;
	}

	public boolean isSuccess(){
		return success;
	}

	public Player getPlayer() {
		return player;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}