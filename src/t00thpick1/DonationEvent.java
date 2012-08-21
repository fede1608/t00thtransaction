package t00thpick1;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class DonationEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private Donation donation;
 
    public DonationEvent(Donation newdonation) {
        donation = newdonation;
    }
	/**
	 * @return the Donation object
	 */
    public Donation getDonation() {
        return donation;
    }
 
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}