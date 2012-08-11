package t00thpick1;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class CustomGiftBoxEvent extends Event implements Cancellable{
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private String gift;
    private boolean cancelled = false;
    private String args1;
    private String args2;
    private String args3;
 
    public CustomGiftBoxEvent(Player Donator, String Gift, String arg1, String arg2, String arg3) {
        player = Donator;
        gift = Gift;
        args1 = arg1;
        args2 = arg2;
        args3 = arg3;
    }
	/**
	 * @return the player
	 */
    public Player getPlayer() {
        return player;
    }
	/**
	 * @return the gift name
	 */
    public String getGift() {
    	return gift;
    }
	/**
	 * @return the first argument
	 */
    public String getArgs1() {
    	return args1;
    }
	/**
	 * @return the second argument
	 */
    public String getArgs2() {
    	return args2;
    }
	/**
	 * @return the third argument
	 */
    public String getArgs3() {
    	return args3;
    }
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
	/**
	 * @return the whether or not the gift is cancelled
	 */
	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	/**
	 * @param Set whether or not the gift is cancelled
	 */
	@Override
	public void setCancelled(boolean booln) {
		cancelled = booln;
		
	}
}