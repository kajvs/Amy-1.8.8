package cc.kajus.amy.hitdetection;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * Lag compensator
 * https://github.com/Islandscout/ServerSideHitDetection
 *
 * <p>
 * It keeps a short history of each player's positions and can answer "where was
 * this player N milliseconds ago", so hit detection can be tested against the
 * position the attacker actually saw instead of the current one. This is what
 * makes high-ping hits register fairly.
 *
 * <p>
 * config flag; here it is hardcoded on.
 */
public class LagCompensator {

	private final ListMultimap<UUID, Pair<Location, Long>> locationTimes = ArrayListMultimap.create();
	private final int historySize = 40;
	private final int pingOffset = 92;
	private final int timeResolution = 30;

	/**
	 * Estimates where the player was {@code rewindMillisecs} ago, interpolating
	 * between the two recorded samples that bracket that instant.
	 */
	public Location getHistoryLocation(Player player, int rewindMillisecs) {
		if (!locationTimes.containsKey(player.getUniqueId())) {
			return player.getLocation();
		}

		List<Pair<Location, Long>> previousLocations = locationTimes.get(player.getUniqueId());
		long currentTime = System.currentTimeMillis();

		int rewindTime = rewindMillisecs + pingOffset;
		int timesSize = previousLocations.size() - 1;

		for (int i = timesSize; i >= 0; i--) {
			Pair<Location, Long> locationPair = previousLocations.get(i);
			int elapsedTime = (int) (currentTime - locationPair.getValue());

			if (elapsedTime >= rewindTime) {
				if (i == timesSize) {
					return locationPair.getKey();
				}

				int maxRewindMilli = rewindMillisecs + pingOffset;
				int millisSinceLoc = (int) (currentTime - locationPair.getValue());

				double movementRelAge = millisSinceLoc - maxRewindMilli;
				double millisSinceLastLoc = currentTime - previousLocations.get(i + 1).getValue();

				double nextMoveWeight = movementRelAge / (millisSinceLoc - millisSinceLastLoc);
				Location before = locationPair.getKey().clone();
				Location after = previousLocations.get(i + 1).getKey();
				Vector interpolate = after.toVector().subtract(before.toVector());

				interpolate.multiply(nextMoveWeight);
				before.add(interpolate);

				return before;
			}
		}

		return player.getLocation();
	}

	private void processPosition(Location loc, Player p) {
		int timesSize = locationTimes.get(p.getUniqueId()).size();
		long currTime = System.currentTimeMillis();

		// Throttle: at most one sample every timeResolution ms.
		if (timesSize > 0 && currTime - locationTimes.get(p.getUniqueId()).get(timesSize - 1).getValue() < timeResolution) {
			return;
		}

		locationTimes.put(p.getUniqueId(), Pair.of(loc, currTime));

		if (timesSize > historySize) {
			locationTimes.get(p.getUniqueId()).remove(0);
		}
	}

	/** Record a position sample for this player. Called from PlayerConnection. */
	public void registerMovement(Player player, Location to) {
		processPosition(to, player);
	}

	public void clearCache(Player player) {
		locationTimes.removeAll(player.getUniqueId());
	}
}
