package t00thpick1;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


import t00thpick1.DonationEvent;
import t00thpick1.DonationPromotionEvent;

public class T00thTransaction extends JavaPlugin implements Listener{
	ChatColor red = ChatColor.DARK_RED;
	ChatColor gold = ChatColor.GOLD;
	ChatColor white = ChatColor.WHITE;
	ChatColor green = ChatColor.GREEN;
	Logger log;
	public Configuration config;
    private static String user;
    private static String pass;
    private static String url;
    private static boolean lifetimeranks;
    private static boolean packages;
    private static boolean onlinemode;
    private Map<Player, String> Players = new HashMap<Player, String>();
    private int tiers;
    private static Economy econ = null;
    private static Permission perms = null;
    public void onEnable(){	
    	Plugin T00thTransaction = getServer().getPluginManager().getPlugin("T00thTransaction");
    	this.log = getLogger();
        File file = this.getDataFolder();
        if (!file.isDirectory()){
            if (!file.mkdirs()) {
            	this.log.severe("Failed to create T00thTransaction directory folder!");
            	getServer().getPluginManager().disablePlugin(T00thTransaction);
            	return;
            }
        }
        loadDefaults(true);
        if(!this.isEnabled()){
        	return;
    	}	
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        this.log.info(perms.getName());	
		CreateTables();
        getServer().getPluginManager().registerEvents(this, this);
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			   public void run() {   
			       try {
			    	   UpdateNewDonations();
			    	   if(packages){
			    		   UpdateNewPackages();
			    		   CheckExpirations();
			    	   }
			    	   if(lifetimeranks){
			    		   UpdateRanks();
			    	   }
			       } catch (SQLException e) {
			    	   error(e);
			       }
			   }
		}, 60L, 6000L);
	}
    public void loadDefaults(Boolean init){
    	this.log = getLogger();
        FileConfiguration configG = getConfig();
        File configFile = new File(this.getDataFolder()+"/config.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        boolean defaul = false;
        if(!config.contains("Config.MySQL.URL.IP")){
            getConfig().addDefault("Config.MySQL.URL.IP", "LOCALHOST");
            defaul = true;
        }
        if(!config.contains("Config.MySQL.URL.PORT")){
            getConfig().addDefault("Config.MySQL.URL.PORT", "3306");
            defaul = true;
        }
        if(!config.contains("Config.MySQL.URL.DATABASE")){
            getConfig().addDefault("Config.MySQL.URL.DATABASE", "DATABASE");
            defaul = true;
        }
        if(!config.contains("Config.MySQL.Password")){
            getConfig().addDefault("Config.MySQL.Password", "Password");
            defaul = true;
        }
        if(!config.contains("Config.MySQL.Username")){
            getConfig().addDefault("Config.MySQL.Username", "username");
            defaul = true;
        }
        if(!config.contains("Config.Settings.OnlyUpdateOnlinePlayers")){
            getConfig().addDefault("Config.Settings.OnlyUpdateOnlinePlayers", false);
        }
        if(!config.contains("Config.Settings.CashRewardPerDollar")){
            getConfig().addDefault("Config.Settings.CashRewardPerDollar", 1);
        }
        if(!config.contains("Config.Settings.Announce")){
            getConfig().addDefault("Config.Settings.Announce", false);
        }
        if(!config.contains("Config.Settings.Mode.LifeTimeRanksEnabled")){
            getConfig().addDefault("Config.Settings.Mode.LifeTimeRanksEnabled", true);
        }
        if(!config.contains("Config.Settings.Mode.PackagesEnabled")){
            getConfig().addDefault("Config.Settings.Mode.PackagesEnabled", false);
        }
        if(!config.contains("Config.Settings.IgnoreRanks")){
        	String[] ignore = {"Moderator","Admin"};
        	List<String> ignorelist = Arrays.asList(ignore);
            getConfig().addDefault("Config.Settings.IgnoreRanks", ignorelist);
        }
        if(defaul){
	        if(!config.contains("Config.Ranks.Rank1.Name")){
	            getConfig().addDefault("Config.Ranks.Rank1.Name", "Coal_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank1.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank1.MoneyReward", 50);
	        }
	        if(!config.contains("Config.Ranks.Rank1.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank1.Minimum_Donation", 5);
	        }
	        if(!config.contains("Config.Ranks.Rank1.Commands.Command1")){
	        	getConfig().addDefault("Config.Ranks.Rank1.Commands.Command1", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank1.Commands.Command2")){
	        	getConfig().addDefault("Config.Ranks.Rank1.Commands.Command2", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank2.Name")){
	            getConfig().addDefault("Config.Ranks.Rank2.Name", "Iron_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank2.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank2.MoneyReward", 300);
	        }
	        if(!config.contains("Config.Ranks.Rank2.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank2.Minimum_Donation", 30);
	        }
	        if(!config.contains("Config.Ranks.Rank2.Commands.Command1")){
	        	getConfig().addDefault("Config.Ranks.Rank2.Commands.Command1", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank2.Commands.Command2")){
	        	getConfig().addDefault("Config.Ranks.Rank2.Commands.Command2", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank3.Name")){
	            getConfig().addDefault("Config.Ranks.Rank3.Name", "Gold_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank3.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank3.MoneyReward", 600);
	        }
	        if(!config.contains("Config.Ranks.Rank3.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank3.Minimum_Donation", 60);
	        }
	        if(!config.contains("Config.Ranks.Rank3.Commands.Command1")){
	        	getConfig().addDefault("Config.Ranks.Rank3.Commands.Command1", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank3.Commands.Command2")){
	        	getConfig().addDefault("Config.Ranks.Rank3.Commands.Command2", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank4.Name")){
	            getConfig().addDefault("Config.Ranks.Rank4.Name", "Emerald_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank4.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank4.MoneyReward", 1000);
	        }
	        if(!config.contains("Config.Ranks.Rank4.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank4.Minimum_Donation", 100);
	        }
	        if(!config.contains("Config.Ranks.Rank4.Commands.Command1")){
	        	getConfig().addDefault("Config.Ranks.Rank4.Commands.Command1", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank4.Commands.Command2")){
	        	getConfig().addDefault("Config.Ranks.Rank4.Commands.Command2", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank5.Name")){
	            getConfig().addDefault("Config.Ranks.Rank5.Name", "Diamond_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank5.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank5.MoneyReward", 2000);
	        }
	        if(!config.contains("Config.Ranks.Rank5.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank5.Minimum_Donation", 200);
	        }
	        if(!config.contains("Config.Ranks.Rank5.Commands.Command1")){
	        	getConfig().addDefault("Config.Ranks.Rank5.Commands.Command1", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank5.Commands.Command2")){
	        	getConfig().addDefault("Config.Ranks.Rank5.Commands.Command2", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank6.Name")){
	            getConfig().addDefault("Config.Ranks.Rank6.Name", "Obsidian_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank6.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank6.MoneyReward", 5000);
	        }
	        if(!config.contains("Config.Ranks.Rank6.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank6.Minimum_Donation", 500);
	        }
	        if(!config.contains("Config.Ranks.Rank6.Commands.Command1")){
	        	getConfig().addDefault("Config.Ranks.Rank6.Commands.Command1", "op %player");
	        }
	        if(!config.contains("Config.Ranks.Rank6.Commands.Command2")){
	        	getConfig().addDefault("Config.Ranks.Rank6.Commands.Command2", "op %player");
	        }
        }
        if(!config.contains("Config.Packages.ExamplePackage.Days")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Days", 30);
        }
        if(!config.contains("Config.Packages.ExamplePackage.RunOnLogin")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.RunOnLogin", false);
        }
        if(!config.contains("Config.Packages.ExamplePackage.Promote.Enabled")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Promote.Enabled", true);
        }
        if(!config.contains("Config.Packages.ExamplePackage.Promote.ToRank")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Promote.ToRank", "Coal_Donator");
        }
        if(!config.contains("Config.Packages.ExamplePackage.Money")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Money", 300);
        }
        if(!config.contains("Config.Packages.ExamplePackage.Items.IDs")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Items.IDs", "57/351_15/302");
        }
        if(!config.contains("Config.Packages.ExamplePackage.Items.Amount")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Items.Amounts", "2/5/1");
        }
        if(!config.contains("Config.Packages.ExamplePackage.Items.Enchants")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Items.Enchants", "0/0/5_3&0_4");
        }
        if(!config.contains("Config.Packages.ExamplePackage.Commands.Activation.Command1")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Commands.Activation.Command1", "op %player");
        }
        if(!config.contains("Config.Packages.ExamplePackage.Commands.Activation.Command2")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Commands.Activation.Command2", "op %player");
        }
        if(!config.contains("Config.Packages.ExamplePackage.Commands.Expiration.Command1")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Commands.Expiration.Command1", "deop %player");
        }
        if(!config.contains("Config.Packages.ExamplePackage.Commands.Expiration.Command2")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.Commands.Expiration.Command2", "deop %player");
        }
        if(!config.contains("Config.Packages.ExamplePackage.DemoteOnExpire.Enabled")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.DemoteOnExpire.Enabled", true);
        }
        if(!config.contains("Config.Packages.ExamplePackage.DemoteOnExpire.ToRank")){
        	getConfig().addDefault("Config.Packages.ExamplePackage.DemoteOnExpire.ToRank", "Resident");
        }
        if(config.getConfigurationSection("Config.Packages")!=null){
        	for(String Package: config.getConfigurationSection("Config.Packages").getKeys(false)){
        		if(!config.contains("Config.Packages."+Package+".RunOnLogin")){
        			config.addDefault("Config.Packages."+Package+".RunOnLogin", false);
        		}
        	}
        }
        configG.options().copyDefaults(true);
        this.saveConfig();
        url = "jdbc:mysql://"+config.getString("Config.MySQL.URL.IP")+":"+config.getString("Config.MySQL.URL.PORT")+"/"+config.getString("Config.MySQL.URL.DATABASE");     
        pass = config.getString("Config.MySQL.Password");
        user = config.getString("Config.MySQL.Username");
        lifetimeranks = config.getBoolean("Config.Settings.Mode.LifeTimeRanksEnabled");
        packages = config.getBoolean("Config.Settings.Mode.PackagesEnabled");
        onlinemode = config.getBoolean("Config.Settings.OnlyUpdateOnlinePlayers");
        tiers = config.getConfigurationSection("Config.Ranks").getKeys(false).size();
        if(defaul||url.equals("jdbc:mysql://LOCALHOST:3306/DATABASE")){
        	this.log.warning("SQL database info not found, disabling...");
        	getServer().getPluginManager().disablePlugin(this);
        }
        if(init){
        	setupPermissions();
        	setupEconomy();
        }
    }
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public void CreateTables(){
    	try{
    		CreateMainTable();
    		CreatePackagesTable();
    	} catch (SQLException e){
    		error(e);
    	}
    }
	public void CreateMainTable() throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		DatabaseMetaData dbm = conn.getMetaData();
		// check if table is there
		ResultSet tables = dbm.getTables(null, null, "toothtransaction", null);
		this.log.info("Checking for toothtransaction database table....");
		if (!tables.next()) {
			this.log.info("Table not found, creating table");
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE toothtransaction(id INT AUTO_INCREMENT KEY, player VARCHAR(50), amount FLOAT, timestamp TIMESTAMP, used INT, ipn VARCHAR(20))";
			stmt.executeUpdate(sql);
		}
		conn.close();
	}
	public void CreatePackagesTable() throws SQLException { 
		Connection conn = DriverManager.getConnection(url, user, pass);
		DatabaseMetaData dbm = conn.getMetaData();
		ResultSet tables = dbm.getTables(null, null, "toothpackages", null);
		this.log.info("Checking for toothpackages database table....");
		if (!tables.next()) {
			this.log.info("Table not found, creating table");
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE toothpackages(id INT AUTO_INCREMENT KEY, player VARCHAR(50), cost FLOAT, package VARCHAR(100), status INT DEFAULT 0, activated LONG DEFAULT NULL, expired LONG DEFAULT NULL)";
			stmt.executeUpdate(sql);
		}
		conn.close();
	}
	public void DropTables() throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		Statement stmt = conn.createStatement();
		String sql = "DROP TABLE toothtransaction";
		stmt.executeUpdate(sql);
		sql = "DROP TABLE toothpackages";
		stmt.executeUpdate(sql);
		conn.close();
	}
	public String escape(String string){
		String result = StringEscapeUtils.escapeJava(string.replaceAll("'", "").replaceAll("\\\\", "").replaceAll("\"", ""));
		return result;
	}
	public void Insert(String player, float amount, Timestamp timestamp, int used) throws SQLException {
		player = escape(player);
		Connection conn = DriverManager.getConnection(url, user, pass);
		Statement stmt = conn.createStatement();
		String sql = "INSERT INTO toothtransaction (player, amount, timestamp, used, ipn) VALUES ('"+player+"', "+amount+", '"+timestamp+"', "+used+", null)";
		stmt.executeUpdate(sql);
		stmt.close();
		conn.close();
	}
	public void Delete(int id) throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		Statement stmt = conn.createStatement();
		String sql = "DELETE FROM toothtransaction WHERE id='"+id+"'";
		stmt.executeUpdate(sql);
		stmt.close();
		conn.close();
	}
	public void GetAllBalances(Player player, int page) throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		int[] range = {page*8-7, page*8-6, page*8-5, page*8-4, page*8-3, page*8-2, page*8-1, page*8};
		try {
			String query = "SELECT player as player, SUM(amount) as amount FROM toothtransaction GROUP BY player";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int result = 0;
			player.sendMessage(ChatColor.GOLD+"Page: "+page);
		    while (rs.next()) {
		    	result++;
		    	if(rs.getObject("player") != null){
		    		for(int i = 0; i<8;i++){
		    			if(range[i]==result){
		    				player.sendMessage(gold+rs.getString("player")+ " has donated: $"+rs.getFloat("amount"));
		    			}
		    		}
	    	    }
	    	}
		    stmt.close();
		    conn.close();
		} catch (SQLException e) {
			error(e);
		}
		return;
	}
	public void UpdateRanks() throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		this.log.info("Running Promotion check!");
		try {
			String query = "SELECT player as player, SUM(amount) as amount FROM toothtransaction GROUP BY player";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		String player = rs.getString("player");
		    		World world = null;
		    		float amount = rs.getFloat("amount");
		    		int tier = 0;
		    		DonationPromotionEvent event;
		    		boolean promote = false;
		    		for(int current = 1; current <= tiers-1; current++){
		    			if((amount >= (float)config.getDouble("Config.Ranks.Rank"+current+".Minimum_Donation")) && amount < ((float)config.getDouble("Config.Ranks.Rank"+(current+1)+".Minimum_Donation")) && !perms.playerInGroup(world, player, config.getString("Config.Ranks.Rank"+current+".Name"))){
		    				tier = current;
		    				promote = true;
		    			}
		    		}
		    	    if((amount >= (float)config.getDouble("Config.Ranks.Rank"+tiers+".Minimum_Donation")) && !perms.playerInGroup(world, player, config.getString("Config.Ranks.Rank"+tiers+".Name"))){
			    		tier = tiers;
			    		promote = true;
		    	    }
		    	    if(player.equals("")||player.equals(null)){
		    	    	log.warning("Empty playername in donation database");
		    	    	promote = false;
		    	    }
		    	    if(promote&&!(onlinemode&&getServer().getPlayer(player)==null)){
			    		List<String> ignore = config.getStringList("Config.Settings.IgnoreRanks");
			    		for(String group: perms.getPlayerGroups((World)null, player)){
			    			if(!ignore.contains(group)){
			    				perms.playerRemoveGroup((World)null, player, group);
			    			}
			    		}
			    		perms.playerAddGroup((World)null, player, config.getString("Config.Ranks.Rank"+tier+".Name"));
			    		event = new DonationPromotionEvent(player, amount, tier, config.getString("Config.Ranks.Rank"+tier+".Name"));
			    		Double reward = config.getDouble("Config.Ranks.Rank"+tier+".MoneyReward");
			    		performCommands(player, parseCommandsRanks(player, tier));
			    		if(reward>0&&econ!=null){
			    			econ.depositPlayer(player, reward);
			    		}
			    		this.log.info(player+" was promoted to "+ config.getString("Config.Ranks.Rank"+tier+".Name"));
			    	    this.getServer().getPluginManager().callEvent(event);
		    	    	if(config.getBoolean("Config.Settings.Announce")){
		    	    		getServer().broadcastMessage(player + " has been promoted to "+config.getString("Config.Ranks.Rank"+tier+".Name"));
		    	    	}
		    	    }
		    	}
		    }
		    stmt.close();
		    conn.close();
		} catch (SQLException e) {
			error(e);
		}
		return;
	}
	/**
	 * @return the total donation amount for player
	 */
	public static float getTotal(String player){ 
		float total = 0;
		try {
			Connection conn = DriverManager.getConnection(url, user, pass); 
			String query = "SELECT SUM(amount) FROM toothtransaction WHERE player = '"+player+"'";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    while (rs.next()) {
		    	if(rs.getObject("SUM(amount)") != null){
		    	    total = rs.getFloat("SUM(amount)");  
		    	}
		    }
			conn.close();
		} catch (SQLException e) {
			System.out.println("There is a problem with T00thTransaction's database connection");
			System.out.println("Either there is network problems between you and your database or");
			System.out.println("Your database info is incorrect in config");
			e.printStackTrace();
		}
		return total;	
	}
	public void getList(String player, Player sender) throws SQLException { 
		Connection conn = DriverManager.getConnection(url, user, pass); 
		try {
			String query = "SELECT * FROM toothtransaction WHERE player = '"+player+"'";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		if(sender!=null){
		    			sender.sendMessage(white+""+rs.getInt("id")+gold+" Player: "+white+rs.getString("player")+" | "+gold+"Amount: $"+white+rs.getFloat("amount")+" | "+gold+"Time: "+white+rs.getTimestamp("timestamp"));  
		    		} else {
		    			this.log.info(rs.getInt("id")+" Player: "+rs.getString("player")+" | Amount: $"+rs.getFloat("amount")+" | Time: "+rs.getTimestamp("timestamp"));
		    		}
		    	}
		    }
		} catch (SQLException e) {
			error(e);
		}

		conn.close(); 
		return;
		
	}
	public static HashSet<Donation> getAllDonations() throws SQLException { 
		Connection conn = DriverManager.getConnection(url, user, pass); 
		HashSet<Donation> donations = new HashSet<Donation>();
		try {
			String query = "SELECT * FROM toothtransaction";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		donations.add(new Donation(rs.getInt("id"), rs.getString("player"), rs.getFloat("amount"), rs.getTimestamp("timestamp"), rs.getInt("used")));
		    	}
		    }
		} catch (SQLException e) {
			System.out.println("There is a problem with T00thTransaction's database connection");
			System.out.println("Either there is network problems between you and your database or");
			System.out.println("Your database info is incorrect in config");
			e.printStackTrace();
		}
		conn.close(); 
		return donations;
	}
	public void UpdateNewDonations() throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		this.log.info("Running Donation check!");
		try {
			String query = "SELECT id, player, amount, used, timestamp FROM toothtransaction WHERE NOT used = 1 ORDER BY player";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    query = "UPDATE toothtransaction SET used = 1 WHERE used = 0";
		    if(onlinemode){
		    	query=query+" AND NOT ('1'='0'";
		    }
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		String player = rs.getString("player");
		    		if(!(getServer().getPlayer(player)==null&&onlinemode)){
		    			float Donation = Math.round(rs.getFloat("amount"));
		    			Donation donation = new Donation(rs.getInt("id"), player, Donation, rs.getTimestamp("timestamp"), 1);
		    			DonationEvent event = new DonationEvent(donation);
		    			this.getServer().getPluginManager().callEvent(event);
		    			Double amount = (double)Donation*config.getDouble("Config.Settings.CashRewardPerDollar");
		    			String announce = player+" has donated "+Donation;
		    			if(amount>0 && econ!=null){
		    				this.log.info(amount+" paid to " +player + " for donation!");
		    				econ.depositPlayer(player, amount);
		    				announce = announce + " and recieved "+amount+ " " + econ.currencyNamePlural();
		    			}
	    				if(config.getBoolean("Config.Settings.Announce")){
	    					getServer().broadcastMessage(announce);
	    				}
		    		} else {
		    			query = query + " OR player = '"+player+"'";
		    		}
		    	}
		    }
		    if(onlinemode){
		    	query = query + ")";
		    }
		    stmt.executeUpdate(query);
		} catch (SQLException e) {
			error(e);
		}
		return;
	}
	public void UpdateNewPackages() throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		this.log.info("Running Package check!");
		try {
			String query = "SELECT player, package FROM toothpackages WHERE status = 0 ORDER BY player";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    query = "UPDATE toothpackages SET status = 1 WHERE status = 0";
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		String player = rs.getString("player");
		    		String announce = player+" has purchased: "+rs.getString("package");
	    			if(config.getBoolean("Config.Settings.Announce")){
	    				getServer().broadcastMessage(announce);
	    			}
		    	}
		    }
		    stmt.executeUpdate(query);
		} catch (SQLException e) {
			error(e);
		}
		return;
	}
	public void CheckExpirations() throws SQLException {
		if(!packages){
			return;
		}
		Connection conn = DriverManager.getConnection(url, user, pass);
		this.log.info("Running Expiration check!");
		try {
			String query = "SELECT id, player, cost, package, status, activated FROM toothpackages WHERE status = '2' ORDER BY player";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    query = "UPDATE toothpackages SET status = '3', expired = '"+System.currentTimeMillis()+ "' WHERE status = '2'";
		    query=query+" AND NOT ('1'='0'";
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		String player = rs.getString("player");
		    		String packageoption = rs.getString("package");
		    		float amount = rs.getFloat("cost");
		    		long active = rs.getLong("activated");
		    		long current = System.currentTimeMillis();
		    		long expire = (((long)config.getInt("Config.Packages."+packageoption+".Days"))*24*60*60*1000);
		    		int id = rs.getInt("id");
		    		if(!(getServer().getPlayer(player)!=null&&onlinemode)&&(current-active)>=expire&&expire!=0){
		    			PackageExpiresEvent event = new PackageExpiresEvent(player, packageoption, amount, active);
		    			this.getServer().getPluginManager().callEvent(event);
		    			packageExpiration(player, packageoption);
		    			if(getServer().getPlayer(player)!=null){
		    				getServer().getPlayer(player).sendMessage("Your donation package has expired!");
		    			}
		    		} else {
		    			query = query + " OR (id = '"+id+"' AND player = '"+player+"' AND package = '"+packageoption+"')";
		    		}
		    	}
		    }
		    query = query + ")";
		    stmt.executeUpdate(query);
		} catch (SQLException e) {
			error(e);
		}
		return;
	}
	public boolean hasPackage(Player player, String packageoption){
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			String query = "SELECT player, cost, package, status, activated FROM toothpackages WHERE status = '1' AND LOWER( player ) = LOWER( '"+player.getName()+"') AND package ='"+packageoption+"'";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		return true;
		    	}
		    }
		} catch (SQLException e) {
			error(e);
		}
		return false;
	}
	public boolean usePackage(Player player, String packageoption){
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			String query = "SELECT id, player, cost, package, status, activated FROM toothpackages WHERE status = '1' AND LOWER( player ) = LOWER( '"+player.getName()+"') AND package ='"+packageoption+"'";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int id = -1;
		    while (rs.next()&&id==-1) {
		    	if(rs.getObject("player") != null){
		    		id = rs.getInt("id");
		    	}
		    }
		    if(id!=-1){
		    	query = "UPDATE toothpackages SET status = '2', activated = '"+System.currentTimeMillis()+"' WHERE id = '"+id+"'";
		    	stmt.executeUpdate(query);
		    	return true;
		    } else {
		    	return false;
		    }
		} catch (SQLException e) {
			error(e);
		}
		return false;
	}
	public boolean isPackage(String packageoption){
		if(config.getConfigurationSection("Config.Packages").getKeys(false).contains(packageoption)){
			return true;
		}
		return false;
	}
	public boolean packageActivation(Player player, String packageoption){
		if(!packages){
			return false;
		}
		if(!isPackage(packageoption)){
			player.sendMessage("Invalid package");
			return false;
		}
		if(!hasPackage(player, packageoption)){
			player.sendMessage("You do not have that package");
			return false;
		}
		if(hasAnotherPackage(player)){
			player.sendMessage("Tienes disponible: "+packageoption+". Se te acreditara cuando se te termine el pack actual.");
			return false;
		}
		if(!hasRoom(player, packageoption)){
			player.sendMessage("No Tienes espacio en tu Inventario Para acreditarte: "+packageoption);
			return false;
		}
		if(!usePackage(player, packageoption)){
			return false;
		}
		double give = config.getDouble("Config.Packages."+packageoption+".Money");
		if(give>0&&econ!=null){
			econ.depositPlayer(player.getName(), give);
		}
		long expire = System.currentTimeMillis()+(config.getInt("Config.Packages."+packageoption+".Days")*24*60*60*1000);
		PackageActivationEvent event = new PackageActivationEvent(player, packageoption, expire);
		this.getServer().getPluginManager().callEvent(event);
		giveItems(player, packageoption, parseItems(player, packageoption));
		List<String> ignore = config.getStringList("Config.Settings.IgnoreRanks");
		if(config.getBoolean("Config.Packages."+packageoption+".Promote.Enabled")){
			for(String group: perms.getPlayerGroups(player)){
				if(!ignore.contains(group)){
					perms.playerRemoveGroup(player, group);
				}
			}
			perms.playerAddGroup(player, config.getString("Config.Packages."+packageoption+".Promote.ToRank"));
		}
		performCommands(player.getName(), parseCommands(player.getName(), packageoption, "Activation"));
		player.sendMessage("Package successfully activated!");
		if(!(config.getInt("Config.Packages."+packageoption+".Days")==0)){
			player.sendMessage("Package will expire in " +config.getInt("Config.Packages."+packageoption+".Days")+" days.");
		}
		return true;
	}
	public boolean hasAnotherPackage(Player player) {
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			String query = "SELECT player, cost, package, status, activated FROM toothpackages WHERE status = '2' AND LOWER( player ) = LOWER('"+player.getName()+"')";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		return true;
		    	}
		    }
		} catch (SQLException e) {
			error(e);
		}
		return false;
	}
	public boolean packageExpiration(String player, String packageoption){
		if((config.getInt("Config.Packages."+packageoption+".Days")==0)){
			return false;
		}
		if(config.getBoolean("Config.Packages."+packageoption+".DemoteOnExpire.Enabled")){
			List<String> ignore = config.getStringList("Config.Settings.IgnoreRanks");
			for(String group: perms.getPlayerGroups((World)null, player)){
				if(!ignore.contains(group)){
					perms.playerRemoveGroup((World)null, player, group);
				}
			}
			perms.playerAddGroup((World)null, player, config.getString("Config.Packages."+packageoption+".DemoteOnExpire.ToRank"));
		}
		if(getServer().getPlayer(player)!=null){
			getServer().getPlayer(player).sendMessage("Package has expired!");
		}
		performCommands(player, parseCommands(player, packageoption, "Expiration"));
		return true;
	}
	public Object[] parseCommands(String player, String packageoption, String when){
		Object[] coms = config.getConfigurationSection("Config.Packages."+packageoption+".Commands."+when).getKeys(false).toArray();
		String[] commands = new String[coms.length];
		int i = 0;
		for(Object com: coms){
			String command = config.getString("Config.Packages."+packageoption+".Commands."+when+"."+com).replace("%player", player);
			if(getServer().getPluginCommand(command.split(" ")[0]) != null){
				this.log.warning("Invalid Command: "+command + " in package: "+packageoption);
				command = null;
			}
			commands[i]=command;
			i++;
		}
		return commands;
	}
	public Object[] parseCommandsRanks(String player, int Ranktier){
		if(config.getConfigurationSection("Config.Ranks.Rank"+Ranktier+".Commands")!=null){
			Object[] coms = config.getConfigurationSection("Config.Ranks.Rank"+Ranktier+".Commands").getKeys(false).toArray();
			String[] commands = new String[coms.length];
			int i = 0;
			for(Object com: coms){
				String command = config.getString("Config.Ranks.Rank"+Ranktier+".Commands."+com).replace("%player", player);
				if(getServer().getPluginCommand(command.split(" ")[0]) != null){
					this.log.warning("Invalid Command: "+command + " in rank: "+Ranktier);
					command = null;
				}
				commands[i]=command;
				i++;
			}
			return commands;
		}
		return null;
	}
	public void performCommands(String player, Object[] commands){
		if(commands!=null){
			for(Object command: commands){
				if(command!=null){
					System.out.println(command.toString());
					getServer().dispatchCommand(getServer().getConsoleSender(), command.toString());
				}
			}
		}
	}
	public boolean hasRoom(Player player, String packageoption){
		String packageis = config.getString("Config.Packages."+packageoption+".Items.IDs");
		String packageias = config.getString("Config.Packages."+packageoption+".Items.Amounts");
		String packageies = config.getString("Config.Packages."+packageoption+".Items.Enchants");
		if(packageis.length()==0&&packageias.length()==0){
			return false;
		}
		String[] items = packageis.split("/");
		String[] ias = packageias.split("/");
		String[] ies = packageies.split("/");
		if(items.length!=ias.length||ias.length!=ies.length){
			player.sendMessage("The configuration for this package is off, Contact your server administrator");
			return false;
		}
        int amount = 0;
        for(int i = 0; i<36; i++){
        	if(player.getInventory().getContents()[i]==null){
        		amount++;
        	}
        }
		if(amount<items.length){
			player.sendMessage("Not enough inventory space for your items");
			return false;
		}
		return true;
	}
	public ItemStack[] parseItems(Player player, String packageoption){
		String packageis = config.getString("Config.Packages."+packageoption+".Items.IDs");
		String packageias = config.getString("Config.Packages."+packageoption+".Items.Amounts");
		String packagees = config.getString("Config.Packages."+packageoption+".Items.Enchants");
		String[] items = packageis.split("/");
		String[] ias = packageias.split("/");
		String[] enchantments = packagees.split("/");
        ItemStack[] itemstacks = new ItemStack[ias.length];
		for(int i = 0; i<ias.length; i++){
			Map<Enchantment,Integer> enchs = Collections.synchronizedMap(new HashMap<Enchantment,Integer>());
			String[] item = new String[2];
			item[0]=items[i].split("_")[0];
			if(!items[i].contains("_")){
				item[1]="0";
			} else {
				item[1]=items[i].split("_")[1];
			}
			if(!enchantments[i].equals("0")){
				for(String enchant: enchantments[i].split("&")){
					try{
						Enchantment ench = Enchantment.getById(Integer.valueOf(enchant.split("_")[0]));
						int enchlvl = Integer.valueOf(enchant.split("_")[1]);
						if (enchlvl>127){
							enchlvl =127;
						}
						if(ench!=null){
							enchs.put(ench,enchlvl);
						} else {
							this.log.warning("Invalid enchantment id: "+Integer.valueOf(enchant.split("_")[0])+ " on item: "+i+" for package: "+packageoption);
						}
					} catch (Exception e){
						this.log.warning("Missing parts of enchantment settings for package: "+packageoption);
					}
				}
			}
			ItemStack itemstack = new ItemStack(Material.getMaterial(Integer.valueOf(item[0])));
			itemstack.setDurability(Short.valueOf(item[1]));
			itemstack.setAmount(Integer.valueOf(ias[i]));
			for(Enchantment ench: enchs.keySet()){
				itemstack.addUnsafeEnchantment(ench, enchs.get(ench));
			}
			itemstacks[i]=itemstack;
		}
		return itemstacks;
	}
	public void giveItems(Player player, String packageoption, ItemStack[] itemstacks){
		for(ItemStack itemstack: itemstacks){
			player.getInventory().setItem(player.getInventory().firstEmpty(), itemstack);
		}
	}
	private void ListPackages(Player player, String name, int page) {
		name = escape(name);
		if(page==0){
			page=1;
		}
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			String query = "SELECT player, package FROM toothpackages WHERE status = '1' AND LOWER( player ) = LOWER( '"+name+"')";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			player.sendMessage(gold+"Available packages for "+name);
			int[] range = {page*8-7, page*8-6, page*8-5, page*8-4, page*8-3, page*8-2, page*8-1, page*8};
			int result = 0;
			boolean something = false;
		    while (rs.next()) {
			    result++;
		    	if(rs.getObject("player") != null){
		    		for(int i = 0; i<8;i++){
		    			if(range[i]==result){
		    				player.sendMessage(" -"+red+rs.getString("package"));
		    				something = true;
		    			}
		    		}
		    	}
		    }
		    if(!something){
				player.sendMessage(" -"+green+"No Available Packages");
		    } else if (player.getName()==name) {
		    	player.sendMessage("Type /package PACKAGENAME to activate");
		    }
		} catch (SQLException e) {
			error(e);
		}
		
	}
	public void error(SQLException e){
		System.out.println("There is a problem with T00thTransaction's database connection");
		System.out.println("Either there is network problems between you and your database or");
		System.out.println("Your database info is incorrect in config");
		e.printStackTrace();
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
			if(cmd.getName().equalsIgnoreCase("packages") && isPackagesOn()){
				int page = 0;
				String name = player.getName();
				if(args.length >=1) {
					try{
					page = Integer.parseInt(args[0]);
					} catch (NumberFormatException e){
						name = args[0];
						if(args.length==2){
							try{
								page = Integer.parseInt(args[1]);
							} catch (NumberFormatException e1){
							}
						}
					}		
				}
				ListPackages(player, name, page);
			}
			if(cmd.getName().equalsIgnoreCase("package") && isPackagesOn()){
				if(args.length==1){
					if(isPackage(args[0])){
						packageActivation(player, args[0]);
						return true;
					}
				}
				player.sendMessage("Must choose a package, type /packages for a list of your available packages.");
				return true;
			}
		}
		if(cmd.getName().equalsIgnoreCase("ttr")){
			if(args.length==0){
				player.sendMessage(gold+"Commands:");
				player.sendMessage(red+"    - ttr add PLAYER AMOUNT (MM-DD-YYYY)");
				player.sendMessage(red+"    - ttr check (PLAYER)");
				player.sendMessage(red+"    - ttr listcheck PLAYER (PAGE)");
				player.sendMessage(red+"    - ttr checkall (PAGE)");
				player.sendMessage(red+"    - ttr updateaccounts");
				player.sendMessage(red+"    - ttr delete ENTRYID");
				return true;
			}
			if(args[0].equalsIgnoreCase("reloadconfig")){
				if(player!=null){
					if(!player.hasPermission("t00thtransaction.admin")){
						player.sendMessage(gold+"No soup for you");
						return true;
					}
				}
				loadDefaults(false);
				return true;
			}
			if(args[0].equalsIgnoreCase("add") && (args.length==3 || args.length==4)){
				if(player!=null){
					if(!player.hasPermission("t00thtransaction.admin")){
						player.sendMessage(gold+"No soup for you");
						return true;
					}
				}
				try{
					Timestamp ts;
					if(args.length==3){
						ts = new Timestamp(System.currentTimeMillis());
					} else {
						SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
						java.util.Date parsedDate;
						try {
							parsedDate = dateFormat.parse(args[3]);
						} catch (ParseException e) {
							e.printStackTrace();
							if(sender instanceof Player){
								sender.sendMessage("/ttr add PLAYER AMOUNT MM-DD-YYYY");
							} else {
								System.out.println("/ttr add PLAYER AMOUNT MM-DD-YYYY");
							}
							return false;
						}
						ts = new java.sql.Timestamp(parsedDate.getTime());
					}
					Insert(args[1], Float.valueOf(args[2]), ts, 1);
					if(player!=null){
						player.sendMessage(gold+"You have added a "+ChatColor.DARK_GREEN+"$"+args[2]+gold+" donation from "+args[1]);
					}
					this.log.info(args[1]+" has been added with the amount: $"+args[2]);
					return true;
				} catch (SQLException e){
					error(e);
				}		
			}
			if(args[0].equalsIgnoreCase("addnew") && (args.length==3 || args.length==4)){
				if(player!=null){
					if(!player.hasPermission("t00thtransaction.admin")){
						player.sendMessage(gold+"No soup for you");
						return true;
					}
				}
				try{
					Timestamp ts;
					if(args.length==3){
						ts = new Timestamp(System.currentTimeMillis());
					} else {
						SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
						java.util.Date parsedDate;
						try {
							parsedDate = dateFormat.parse(args[3]);
						} catch (ParseException e) {
							e.printStackTrace();
							if(sender instanceof Player){
								sender.sendMessage("/ttr add PLAYER AMOUNT MM-DD-YYYY");
							} else {
								System.out.println("/ttr add PLAYER AMOUNT MM-DD-YYYY");
							}
							return false;
						}
						ts = new java.sql.Timestamp(parsedDate.getTime());
					}
					Insert(args[1], Float.valueOf(args[2]), ts, 0);
					if(player!=null){
						player.sendMessage(gold+"You have added a new "+ChatColor.DARK_GREEN+"$"+args[2]+gold+" donation from "+args[1]);
					}
					this.log.info(args[1]+" has been added with the amount: $"+args[2]);
					return true;
				} catch (SQLException e){
					error(e);
				}		
			}
			if(args[0].equalsIgnoreCase("delete") && (args.length==2 || args.length==3) && !(sender instanceof Player)){
				if(args.length==2){
					System.out.println("Are you sure you want to delete entry: "+args[1]);
					System.out.println("\"/ttr delete ENTRYID confirm\"  to confirm");
					return false;
				}
				if(args.length==3&&args[2].equalsIgnoreCase("confirm"))
				try{
					Delete(Integer.valueOf(args[1]));
					player.sendMessage(gold+"Entry "+args[1]+" has been deleted.");
					this.log.info("Entry "+args[1]+" has been deleted.");
					return true;
				} catch (SQLException e){
					error(e);
				}		
			}
			if(args[0].equalsIgnoreCase("check")){
				if(sender instanceof Player){
					if(!player.hasPermission("t00thtransaction.admin")&&args.length>1){
						player.sendMessage(gold+"No soup for you");
						return true;
					}
					String playername;
					if(args.length==2){
						if(!player.hasPermission("t00thtransaction.check")&&!player.getName().equals(args[1])){
							player.sendMessage(gold+"No soup for you");
							return true;
						}
						playername = args[1];
					} else {
						playername = player.getName();
					}
					float total = 0;
					total = getTotal(playername);
					player.sendMessage(gold+playername+": $"+white+total);
					return true;
				} else if(args.length==2){
					float total = 0;
					total = getTotal(args[1]);
					this.log.info(args[1]+": $"+total);
					return true;
				} else {
					this.log.info("Not enough arguments");
				}
			}
			if(args[0].equalsIgnoreCase("listcheck")){
				if(sender instanceof Player){
					if(!player.hasPermission("t00thtransaction.admin")&&args.length>1){
						player.sendMessage(gold+"No soup for you");
						return true;
					}
					String playername;
					if(args.length==2){
						if(!player.hasPermission("t00thtransaction.check")&&!player.getName().equals(args[1])){
							player.sendMessage(gold+"No soup for you");
							return true;
						}
						playername = args[1];
					} else {
						playername = player.getName();
					}
					try{				
						getList(playername, player);
						return true;
					} catch (SQLException e){
						error(e);
					}
				} else if (args.length==2) { 
					try{				
						getList(args[1], player);
						return true;
					} catch (SQLException e){
						error(e);
					}
				} else {
					this.log.info("Not enough arguments");
				}
			}
			if(args[0].equalsIgnoreCase("droptable")&& !(sender instanceof Player)){
				if(args.length==1){
					System.out.println("\"ttr droptable confirm\" to confirm");
				} else if(args[1].equalsIgnoreCase("confirm")) {
					try{
						DropTables();
						CreateTables();
						return true;
					} catch (SQLException e){
						error(e);
					}
				}
			}	
			if(args[0].equalsIgnoreCase("checkall")&&(args.length==1||args.length==2)){
				if(player!=null){
					if(!player.hasPermission("t00thtransaction.admin")){
						player.sendMessage(gold+"No soup for you");
						return true;
					}
				}
				try{
					if(player!=null){
						if(args.length==1){
							GetAllBalances(player, 1);
						} else {
							GetAllBalances(player, Integer.valueOf(args[1]));
						}
					} else {
						this.log.info("Can only be run in game!");
					}
					return true;
				} catch (SQLException e){
					error(e);
				}
			}
			if(args[0].equalsIgnoreCase("updateaccounts")){
				if(player!=null){
					if(!player.hasPermission("t00thtransaction.admin")){
						player.sendMessage(gold+"No soup for you");
						return true;
					}
				}
				try{
					if(lifetimeranks){
						UpdateRanks();
					}
					if(packages){
						UpdateNewPackages();
						CheckExpirations();
					}
					UpdateNewDonations();
					if(player!=null){
						player.sendMessage(gold+"Donation check has been run!");
					} else {
						this.log.info("Donation check has been run!");
					}
					return true;
				} catch (SQLException e){
					error(e);
				}
			}
		}
		if(player!=null){
			player.sendMessage("Command syntax incorrect");
		} else {
			this.log.info("Command failed");
		}
		return false;
	}
	/**
	 * @return Whether or not OnlineMode feature is on
	 */
	public static boolean isOnlinemode() {
		return onlinemode;
	}
	/**
	 * @param Toggles online mode
	 */
	public static void setOnlinemode(boolean bool) {
		onlinemode = bool;
	}
	/**
	 * @return Whether or not package feature is on
	 */
	public static boolean isPackagesOn() {
		return packages;
	}
	/**
	 * @return Whether or not lifetimeranks is on
	 */
	public static boolean isLifetimeranks() {
		return lifetimeranks;
	}
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
    	if(!isPackagesOn()){
    		return;
    	}
        Player player = event.getPlayer();
        for(String Package: config.getConfigurationSection("Config.Packages").getKeys(false)){
        	if(hasPackage(player, Package)){
        		if(config.getBoolean("Config.Packages."+Package+".RunOnLogin")){
        			if(packageActivation(player, Package)){
        				return;
        			}		
        		}
        		Players.put(player, Package);
            	getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
         		   public void run() {
         		       for(Player player: Players.keySet()){
         		    	   if(player.isOnline()){
         		    		   player.sendMessage(ChatColor.DARK_RED+"You have a Donation Package Available: "+Players.get(player));
         		    	   }
         		    	   Players.remove(player);
         		       }
         		   }
            	}, 120L);
        	}
        	
        }
    }
}
