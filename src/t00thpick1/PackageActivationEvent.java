package t00thpick1;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PackageActivationEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private static Player player;
    private static String packageoption;
    private static long expire;
    
	public PackageActivationEvent(Player playerg, String packageoptiong, long expireg) {
		player = playerg;
		packageoption = packageoptiong;
		expire = expireg;
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
	public static Player getPlayer() {
		return player;
	}

	/**
	 * @return the package option
	 */
	public String getPackageoption() {
		return packageoption;
	}

	/**
	 * @return the unix timestamp of expiration
	 */
	public long getExpire() {
		return expire;
	}

	/**
	 * @return the unix timestamp of the time package was activated by player
	 */
	public long getActivated() {
		return System.currentTimeMillis();
	}



}
