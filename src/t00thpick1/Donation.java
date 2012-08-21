package t00thpick1;

import java.sql.Timestamp;

public class Donation {
    private String player;
    private float amount;
    private Timestamp timestamp;
    private int status;
 
    public Donation(String Donator, float donation, Timestamp time, int used) {
        amount = donation;
        player = Donator;
        timestamp = time;
        status = used;
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
	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}
	/**
	 * @return the timestamp
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}
}
