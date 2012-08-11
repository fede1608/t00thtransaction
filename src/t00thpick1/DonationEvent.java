package t00thpick1;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class DonationEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private String player;
    private float amount;
 
    public DonationEvent(String Donator, float donation) {
        amount = donation;
        player = Donator;
    }
	/**
	 * @return the player
	 */
    public String getPlayer() {
        return player;
    }
	/**
	 * @return the donation amount
	 */
    public float getAmount() {
    	return amount;
    }
 
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}