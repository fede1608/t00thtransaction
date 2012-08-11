package t00thpick1;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public class DonationPromotionEvent extends Event{
    private static final HandlerList handlers = new HandlerList();
    private String player;
    private float currenttotal;
    private int ranktier;
    private String rank;
 
    public DonationPromotionEvent(String Donator, float donation, int RankTier, String Rank) {
        currenttotal = donation;
        player = Donator;
        ranktier = RankTier;
        rank = Rank;
    }
	/**
	 * @return the player
	 */
    public String getPlayer() {
        return player;
    }
	/**
	 * @return the players current donation total
	 */
    public float getTotal() {
    	return currenttotal;
    }
	/**
	 * @return the rank tier number
	 */
    public int getRankTier() {
    	return ranktier;
    }
	/**
	 * @return the rank name
	 */
    public String getRank() {
    	return rank;
    }
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}