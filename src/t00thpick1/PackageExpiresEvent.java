package t00thpick1;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PackageExpiresEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private static String player;
    private static String packageoption;
    private static float cost;
    private static long activated;
    
	public PackageExpiresEvent(String playerg, String packageoptiong, float costg, long activatedg) {
		player = playerg;
		packageoption = packageoptiong;
		cost = costg;
		activated = activatedg;
	}

    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }

	/**
	 * @return the player
	 */
	public static String getPlayer() {
		return player;
	}

	/**
	 * @return the package option
	 */
	public String getPackageoption() {
		return packageoption;
	}

	/**
	 * @return the cost
	 */
	public float getCost() {
		return cost;
	}

	/**
	 * @return the unix timestamp of expiration
	 */
	public long getExpire() {
		return System.currentTimeMillis();
	}

	/**
	 * @return the unix timestamp of the time package was activated by player
	 */
	public long getActivated() {
		return activated;
	}



}
