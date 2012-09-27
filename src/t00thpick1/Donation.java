package t00thpick1;

import java.sql.Timestamp;

public class Donation {
    private String player;
    private float amount;
    private Timestamp timestamp;
    private int status;
    private int id;
 
    public Donation(int unid, String Donator, float donation, Timestamp time, int used) {
        amount = donation;
        player = Donator;
        timestamp = time;
        status = used;
        id = unid;
    }
	/**
	 * @return true if same
	 */
    public boolean isSame(Donation don) {
    	if(don.getAmount()==amount&&don.getTimestamp()==timestamp&&don.getPlayer()==player){
	    		return true;
    	}
        return false;
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
	public void setStatus(int i){
		status = i;
	}
	/**
	 * @return the timestamp
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
