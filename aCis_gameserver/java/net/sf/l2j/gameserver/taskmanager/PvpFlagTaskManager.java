package net.sf.l2j.gameserver.taskmanager;

import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.model.actor.Player;

/**
 * Updates and clears PvP flag of {@link Player} after specified time.
 *
 * @author Tryskell, Hasha
 */
public final class PvpFlagTaskManager implements Runnable {

	private final Map<Player, Long> _players = new ConcurrentHashMap<>();

	public static final PvpFlagTaskManager getInstance() {
		return SingletonHolder._instance;
	}

	protected PvpFlagTaskManager() {
		// Run task each second.
		ThreadPool.scheduleAtFixedRate(this, 1000, 1000);
	}

	/**
	 * Adds {@link Player} to the PvpFlagTask.
	 *
	 * @param player : Player to be added and checked.
	 * @param time : Time in ms, after which the PvP flag is removed.
	 */
	public final void add(Player player, long time) {
		_players.put(player, System.currentTimeMillis() + time);
	}

	/**
	 * Removes {@link Player} from the PvpFlagTask.
	 *
	 * @param player : {@link Player} to be removed.
	 */
	public final void remove(Player player) {
		_players.remove(player);
	}

	@Override
	public final void run() {
		// List is empty, skip.
		if (_players.isEmpty()) {
			return;
		}

		// Get current time.
		final long currentTime = System.currentTimeMillis();

		// Loop all players.
		for (Map.Entry<Player, Long> entry : _players.entrySet()) {
			// Get time left and check.
			final Player player = entry.getKey();
			final long timeLeft = entry.getValue();

			// Time is running out, clear PvP flag and remove from list.
			if (currentTime > timeLeft) {
				player.updatePvPFlag(0);
				_players.remove(player);
			} // Time almost runned out, update to blinking PvP flag.
			else if (currentTime > (timeLeft - 5000)) {
				player.updatePvPFlag(2);
			} // Time didn't run out, keep PvP flag.
			else {
				player.updatePvPFlag(1);
			}
		}
	}

	private static class SingletonHolder {

		protected static final PvpFlagTaskManager _instance = new PvpFlagTaskManager();
	}
}
