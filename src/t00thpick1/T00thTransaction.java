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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import t00thpick1.DonationEvent;
import t00thpick1.DonationPromotionEvent;

public class T00thTransaction extends JavaPlugin{
	ChatColor red = ChatColor.DARK_RED;
	ChatColor gold = ChatColor.GOLD;
	ChatColor white = ChatColor.WHITE;
	ChatColor green = ChatColor.GREEN;
	Logger log;
	public Configuration config;
	int GiftBox;
    private static String user;
    private static String pass;
    private static String url;
    private static boolean lifetimeranks;
    private static boolean giftbox;
    private static boolean packages;
    private static boolean onlinemode;
    private int tiers;
    private static Economy econ = null;
    private static Permission perms = null;
    private static Map<String,String> Giftbox;
    public void onEnable(){	
    	Plugin T00thTransaction = getServer().getPluginManager().getPlugin("T00thTransaction");
    	this.log = getLogger();
        File file = this.getDataFolder();
        if (!file.isDirectory()){
            if (!file.mkdirs()) {
            	this.log.severe("Failed to create T00thTransacction directory folder!");
            	getServer().getPluginManager().disablePlugin(T00thTransaction);
            	return;
            }
        }
        loadDefaults();
        if(!this.isEnabled()){
        	return;
    	}	
        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }
        if(isGiftboxOn()){
        	Giftbox = new HashMap<String, String>();
        	if(config.getBoolean("Config.Settings.GiftBox.Enchant")){
        		registerGiftBoxGift("enchant", "Will enchant currently held item");
        	}
        	if(config.getBoolean("Config.Settings.GiftBox.Chainmail")){
        		registerGiftBoxGift("chain", "Will give you a set of chainmail");
        	}
        }
        this.log.info(perms.getName());	
		CreateTables();
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
			   public void run() {   
			       try {
			    	   UpdateNewDonations();
			    	   if(packages){
			    		   UpdateNewPackages();
			    		   CheckExpirations();
			    	   }
			    	   if(lifetimeranks){
			    		   UpdateAccounts();
			    	   }
			       } catch (SQLException e) {
			    	   error(e);
			       }
			   }
		}, 60L, 6000L);
	}
    public void loadDefaults(){
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
        if(!config.contains("Config.Settings.GiftBox.Enabled")){
            getConfig().addDefault("Config.Settings.GiftBox.Enabled", true);
        }
        if(!config.contains("Config.Settings.GiftBox.CostPer")){
            getConfig().addDefault("Config.Settings.GiftBox.CostPer", 30);
        }
        if(!config.contains("Config.Settings.GiftBox.Enchant")){
            getConfig().addDefault("Config.Settings.GiftBox.Enchant", true);
        }
        if(!config.contains("Config.Settings.GiftBox.Chainmail")){
            getConfig().addDefault("Config.Settings.GiftBox.Chainmail", true);
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
	        if(!config.contains("Config.Ranks.Rank2.Name")){
	            getConfig().addDefault("Config.Ranks.Rank2.Name", "Iron_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank2.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank2.MoneyReward", 300);
	        }
	        if(!config.contains("Config.Ranks.Rank2.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank2.Minimum_Donation", 30);
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
	        if(!config.contains("Config.Ranks.Rank4.Name")){
	            getConfig().addDefault("Config.Ranks.Rank4.Name", "Emerald_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank4.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank4.MoneyReward", 1000);
	        }
	        if(!config.contains("Config.Ranks.Rank4.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank4.Minimum_Donation", 100);
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
	        if(!config.contains("Config.Ranks.Rank6.Name")){
	            getConfig().addDefault("Config.Ranks.Rank6.Name", "Obsidian_Donator");
	        }
	        if(!config.contains("Config.Ranks.Rank6.MoneyReward")){
	            getConfig().addDefault("Config.Ranks.Rank6.MoneyReward", 5000);
	        }
	        if(!config.contains("Config.Ranks.Rank6.Minimum_Donation")){
	            getConfig().addDefault("Config.Ranks.Rank6.Minimum_Donation", 500);
	        }
	        if(!config.contains("Config.Packages.ExamplePackage.Days")){
	            getConfig().addDefault("Config.Packages.ExamplePackage.Days", 30);
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
    	}
        configG.options().copyDefaults(true);
        this.saveConfig();
        url = "jdbc:mysql://"+config.getString("Config.MySQL.URL.IP")+":"+config.getString("Config.MySQL.URL.PORT")+"/"+config.getString("Config.MySQL.URL.DATABASE");     
        pass = config.getString("Config.MySQL.Password");
        user = config.getString("Config.MySQL.Username");
        giftbox = config.getBoolean("Config.Settings.GiftBox.Enabled");
        lifetimeranks = config.getBoolean("Config.Settings.Mode.LifeTimeRanksEnabled");
        packages = config.getBoolean("Config.Settings.Mode.PackagesEnabled");
        onlinemode = config.getBoolean("Config.Settings.OnlyUpdateOnlinePlayers");
        tiers = config.getConfigurationSection("Config.Ranks").getKeys(false).size();
        if(defaul||url.equals("jdbc:mysql://LOCALHOST:3306/DATABASE")){
        	this.log.warning("SQL database info not found, disabling...");
        	getServer().getPluginManager().disablePlugin(this);
        }
        if(packages!=false&&lifetimeranks!=false){
        	this.log.info("Must choose either lifetimeranks OR packages.  Disabling packages by default.");
        	config.set("Config.Settings.Mode.PackagesEnabled", false);
        }
        if(config.getInt("Config.Settings.GiftBox.CostPer")<=0){
        	this.log.info("Invalid value for GiftBox Costper, disabling giftbox feature");
        	config.set("Config.Settings.GiftBox.Enabled", false);
        }
        setupPermissions();
        setupEconomy();
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
	/**
	 * @param Registers Gift with Description for plugin giftbox list.  Returns false if either value is null or same name gift already registered 
	 */
    public static boolean registerGiftBoxGift(String gift, String descript){
    	if(gift!=null && descript!=null){
    		if(Giftbox.containsKey(gift)){
    			return false;
    		}
    		Giftbox.put(gift, descript);
    		return true;
    	}
    	return false;
    }
    public void CreateTables(){
    	try{
    		CreateMainTable();
    		CreateGiftBoxTable();
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
	public void CreateGiftBoxTable() throws SQLException { 
		Connection conn = DriverManager.getConnection(url, user, pass);
		DatabaseMetaData dbm = conn.getMetaData();
		ResultSet tables = dbm.getTables(null, null, "toothgiftbox", null);
		this.log.info("Checking for toothgiftbox database table....");
		if (!tables.next()) {
			this.log.info("Table not found, creating table");
			Statement stmt = conn.createStatement();
			String sql = "CREATE TABLE toothgiftbox(id INT AUTO_INCREMENT KEY, player VARCHAR(50), timestamp TIMESTAMP, milestone INT)";
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
	public int TotalGiftBox(String player){
		int totaldon = 0;
		totaldon = (int)getTotal(player);
		int current = 0;
		try {
			current = getCurrentMilestone(player);
		} catch (SQLException e) {
			error(e);
			return 0;
		}
		int total = (totaldon-current)/config.getInt("Config.Settings.GiftBox.CostPer");
		return total;
	}
	public boolean GiftBoxCreate(String player) throws SQLException{
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection 
    	Statement stmt = conn.createStatement();
    	stmt.executeUpdate("INSERT INTO toothgiftbox (player, milestone) VALUES ('"+player+"', 0)"); //Executes the query
    	stmt.close(); //Closes the query
    	conn.close(); //Closes the connection
        return true;
	}
	public int getCurrentMilestone(String player) throws SQLException{
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection 
    	Statement stmt = conn.createStatement();
    	int milestone = 0;
    	boolean there = false;
    	ResultSet rs = stmt.executeQuery("SELECT player, milestone FROM toothgiftbox WHERE player = '"+player+"'"); //Executes the query
    	while(rs.next()){
    		if(rs.getString("player")!=null){
    			milestone = rs.getInt("milestone");
    			there = true;
    		}
    	}
    	stmt.close(); //Closes the query
    	conn.close(); //Closes the connection
    	if(!there){
    		GiftBoxCreate(player);
    	}
		return milestone;
	}
	public boolean GiftBoxUsed(String player){
		if(TotalGiftBox(player)<1){
			return false;
		}
		int milestone;
		try {
			milestone = getCurrentMilestone(player);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
		int newmilestone = milestone+config.getInt("Config.Settings.GiftBox.CostPer");
		try{
			Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection 
    		Statement stmt = conn.createStatement();	
    		stmt.executeUpdate("UPDATE toothgiftbox SET milestone = "+newmilestone+" WHERE player = '"+player+"'"); //Executes the query
    		stmt.close(); //Closes the query
    		conn.close(); //Closes the connection
    		return true;
		} catch (SQLException e){
			error(e);
		}
		return false;
	}
	public boolean GiftBoxAdd(String player){
		player = escape(player);
		int milestone;
		try {
			milestone = getCurrentMilestone(player);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
		int newmilestone = 0;
		if(milestone>0){
			newmilestone = milestone-config.getInt("Config.Settings.GiftBox.CostPer");
		}
		try{
			Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection 
    		Statement stmt = conn.createStatement();	
    		stmt.executeUpdate("UPDATE toothgiftbox SET milestone = "+newmilestone+" WHERE player = '"+player+"'"); //Executes the query
    		stmt.close(); //Closes the query
    		conn.close(); //Closes the connection
    		return true;
		} catch (SQLException e){
			error(e);
		}
		return false;
	}
	public boolean GiftBoxRemove(String player){
		player = escape(player);
		int milestone;
		try {
			milestone = getCurrentMilestone(player);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return false;
		}
		int newmilestone = milestone+config.getInt("Config.Settings.GiftBox.CostPer");
		try{
			Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection 
    		Statement stmt = conn.createStatement();	
    		stmt.executeUpdate("UPDATE toothgiftbox SET milestone = "+newmilestone+" WHERE player = '"+player+"'"); //Executes the query
    		stmt.close(); //Closes the query
    		conn.close(); //Closes the connection
    		return true;
		} catch (SQLException e){
			error(e);
		}
		return false;
	}
	public boolean GiftBoxEnchant(Player player, String enchantment){
		ItemStack item = player.getItemInHand();
		Enchantment ench = Enchantment.OXYGEN;
		int level = 0;
		boolean cannot = true;
		if(enchantment.equalsIgnoreCase("protection")){
			if(item.getType()==Material.CHAINMAIL_BOOTS||item.getType()==Material.CHAINMAIL_CHESTPLATE||item.getType()==Material.CHAINMAIL_HELMET||item.getType()==Material.CHAINMAIL_LEGGINGS||item.getType()==Material.IRON_BOOTS||item.getType()==Material.IRON_CHESTPLATE||item.getType()==Material.IRON_HELMET||item.getType()==Material.IRON_LEGGINGS||item.getType()==Material.GOLD_BOOTS||item.getType()==Material.GOLD_CHESTPLATE||item.getType()==Material.GOLD_HELMET||item.getType()==Material.GOLD_LEGGINGS||item.getType()==Material.LEATHER_BOOTS||item.getType()==Material.LEATHER_CHESTPLATE||item.getType()==Material.LEATHER_HELMET||item.getType()==Material.LEATHER_LEGGINGS||item.getType()==Material.DIAMOND_BOOTS||item.getType()==Material.DIAMOND_CHESTPLATE||item.getType()==Material.DIAMOND_HELMET||item.getType()==Material.DIAMOND_LEGGINGS){
				ench = Enchantment.PROTECTION_ENVIRONMENTAL;
				level = 4;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("fireprotection")){
			if(item.getType()==Material.CHAINMAIL_BOOTS||item.getType()==Material.CHAINMAIL_CHESTPLATE||item.getType()==Material.CHAINMAIL_HELMET||item.getType()==Material.CHAINMAIL_LEGGINGS||item.getType()==Material.IRON_BOOTS||item.getType()==Material.IRON_CHESTPLATE||item.getType()==Material.IRON_HELMET||item.getType()==Material.IRON_LEGGINGS||item.getType()==Material.GOLD_BOOTS||item.getType()==Material.GOLD_CHESTPLATE||item.getType()==Material.GOLD_HELMET||item.getType()==Material.GOLD_LEGGINGS||item.getType()==Material.LEATHER_BOOTS||item.getType()==Material.LEATHER_CHESTPLATE||item.getType()==Material.LEATHER_HELMET||item.getType()==Material.LEATHER_LEGGINGS||item.getType()==Material.DIAMOND_BOOTS||item.getType()==Material.DIAMOND_CHESTPLATE||item.getType()==Material.DIAMOND_HELMET||item.getType()==Material.DIAMOND_LEGGINGS){
				ench = Enchantment.PROTECTION_FIRE;
				level = 4;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("blastprotection")){
			if(item.getType()==Material.CHAINMAIL_BOOTS||item.getType()==Material.CHAINMAIL_CHESTPLATE||item.getType()==Material.CHAINMAIL_HELMET||item.getType()==Material.CHAINMAIL_LEGGINGS||item.getType()==Material.IRON_BOOTS||item.getType()==Material.IRON_CHESTPLATE||item.getType()==Material.IRON_HELMET||item.getType()==Material.IRON_LEGGINGS||item.getType()==Material.GOLD_BOOTS||item.getType()==Material.GOLD_CHESTPLATE||item.getType()==Material.GOLD_HELMET||item.getType()==Material.GOLD_LEGGINGS||item.getType()==Material.LEATHER_BOOTS||item.getType()==Material.LEATHER_CHESTPLATE||item.getType()==Material.LEATHER_HELMET||item.getType()==Material.LEATHER_LEGGINGS||item.getType()==Material.DIAMOND_BOOTS||item.getType()==Material.DIAMOND_CHESTPLATE||item.getType()==Material.DIAMOND_HELMET||item.getType()==Material.DIAMOND_LEGGINGS){
				ench = Enchantment.PROTECTION_EXPLOSIONS;
				level = 4;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("projectileprotection")){
			if(item.getType()==Material.CHAINMAIL_BOOTS||item.getType()==Material.CHAINMAIL_CHESTPLATE||item.getType()==Material.CHAINMAIL_HELMET||item.getType()==Material.CHAINMAIL_LEGGINGS||item.getType()==Material.IRON_BOOTS||item.getType()==Material.IRON_CHESTPLATE||item.getType()==Material.IRON_HELMET||item.getType()==Material.IRON_LEGGINGS||item.getType()==Material.GOLD_BOOTS||item.getType()==Material.GOLD_CHESTPLATE||item.getType()==Material.GOLD_HELMET||item.getType()==Material.GOLD_LEGGINGS||item.getType()==Material.LEATHER_BOOTS||item.getType()==Material.LEATHER_CHESTPLATE||item.getType()==Material.LEATHER_HELMET||item.getType()==Material.LEATHER_LEGGINGS||item.getType()==Material.DIAMOND_BOOTS||item.getType()==Material.DIAMOND_CHESTPLATE||item.getType()==Material.DIAMOND_HELMET||item.getType()==Material.DIAMOND_LEGGINGS){
				ench = Enchantment.PROTECTION_PROJECTILE;
				level = 4;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("silktouch")){
			if(item.getType()==Material.DIAMOND_PICKAXE||item.getType()==Material.DIAMOND_SPADE||item.getType()==Material.DIAMOND_AXE||item.getType()==Material.WOOD_PICKAXE||item.getType()==Material.WOOD_SPADE||item.getType()==Material.WOOD_AXE||item.getType()==Material.IRON_PICKAXE||item.getType()==Material.IRON_SPADE||item.getType()==Material.IRON_AXE||item.getType()==Material.GOLD_PICKAXE||item.getType()==Material.GOLD_SPADE||item.getType()==Material.GOLD_AXE){
				ench = Enchantment.SILK_TOUCH;
				level = 1;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("fortune")){
			if(item.getType()==Material.STONE_PICKAXE||item.getType()==Material.STONE_SPADE||item.getType()==Material.STONE_AXE||item.getType()==Material.DIAMOND_PICKAXE||item.getType()==Material.DIAMOND_SPADE||item.getType()==Material.DIAMOND_AXE||item.getType()==Material.WOOD_PICKAXE||item.getType()==Material.WOOD_SPADE||item.getType()==Material.WOOD_AXE||item.getType()==Material.IRON_PICKAXE||item.getType()==Material.IRON_SPADE||item.getType()==Material.IRON_AXE||item.getType()==Material.GOLD_PICKAXE||item.getType()==Material.GOLD_SPADE||item.getType()==Material.GOLD_AXE){
				ench = Enchantment.LOOT_BONUS_BLOCKS;
				level = 3;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("efficiency")){
			if(item.getType()==Material.STONE_PICKAXE||item.getType()==Material.STONE_SPADE||item.getType()==Material.STONE_AXE||item.getType()==Material.DIAMOND_PICKAXE||item.getType()==Material.DIAMOND_SPADE||item.getType()==Material.DIAMOND_AXE||item.getType()==Material.WOOD_PICKAXE||item.getType()==Material.WOOD_SPADE||item.getType()==Material.WOOD_AXE||item.getType()==Material.IRON_PICKAXE||item.getType()==Material.IRON_SPADE||item.getType()==Material.IRON_AXE||item.getType()==Material.GOLD_PICKAXE||item.getType()==Material.GOLD_SPADE||item.getType()==Material.GOLD_AXE){
				ench = Enchantment.DIG_SPEED;
				level = 5;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("unbreaking")){
			if(item.getType()==Material.STONE_PICKAXE||item.getType()==Material.STONE_SPADE||item.getType()==Material.STONE_AXE||item.getType()==Material.DIAMOND_PICKAXE||item.getType()==Material.DIAMOND_SPADE||item.getType()==Material.DIAMOND_AXE||item.getType()==Material.WOOD_PICKAXE||item.getType()==Material.WOOD_SPADE||item.getType()==Material.WOOD_AXE||item.getType()==Material.IRON_PICKAXE||item.getType()==Material.IRON_SPADE||item.getType()==Material.IRON_AXE||item.getType()==Material.GOLD_PICKAXE||item.getType()==Material.GOLD_SPADE||item.getType()==Material.GOLD_AXE){
				ench = Enchantment.DURABILITY;
				level = 3;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("sharpness")){
			if(item.getType()==Material.DIAMOND_SWORD||item.getType()==Material.GOLD_SWORD||item.getType()==Material.IRON_SWORD||item.getType()==Material.STONE_SWORD||item.getType()==Material.WOOD_SWORD){
				ench = Enchantment.DAMAGE_ALL;
				level = 5;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("smite")){
			if(item.getType()==Material.DIAMOND_SWORD||item.getType()==Material.GOLD_SWORD||item.getType()==Material.IRON_SWORD||item.getType()==Material.STONE_SWORD||item.getType()==Material.WOOD_SWORD){
				ench = Enchantment.DAMAGE_UNDEAD;
				level = 5;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("looting")){
			if(item.getType()==Material.DIAMOND_SWORD||item.getType()==Material.GOLD_SWORD||item.getType()==Material.IRON_SWORD||item.getType()==Material.STONE_SWORD||item.getType()==Material.WOOD_SWORD){
				ench = Enchantment.LOOT_BONUS_MOBS;
				level = 3;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("baneofarthropods")){
			if(item.getType()==Material.DIAMOND_SWORD||item.getType()==Material.GOLD_SWORD||item.getType()==Material.IRON_SWORD||item.getType()==Material.STONE_SWORD||item.getType()==Material.WOOD_SWORD){
				ench = Enchantment.DAMAGE_ARTHROPODS;
				level = 5;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("knockback")){
			if(item.getType()==Material.DIAMOND_SWORD||item.getType()==Material.GOLD_SWORD||item.getType()==Material.IRON_SWORD||item.getType()==Material.STONE_SWORD||item.getType()==Material.WOOD_SWORD){
				ench = Enchantment.KNOCKBACK;
				level = 2;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("fireaspect")){
			if(item.getType()==Material.DIAMOND_SWORD||item.getType()==Material.GOLD_SWORD||item.getType()==Material.IRON_SWORD||item.getType()==Material.STONE_SWORD||item.getType()==Material.WOOD_SWORD){
				ench = Enchantment.FIRE_ASPECT;
				level = 2;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("respiration")){
			if(item.getType()==Material.CHAINMAIL_HELMET||item.getType()==Material.DIAMOND_HELMET||item.getType()==Material.GOLD_HELMET||item.getType()==Material.IRON_HELMET||item.getType()==Material.LEATHER_HELMET){
				ench = Enchantment.OXYGEN;
				level = 3;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("aquaaffinity")){
			if(item.getType()==Material.CHAINMAIL_HELMET||item.getType()==Material.DIAMOND_HELMET||item.getType()==Material.GOLD_HELMET||item.getType()==Material.IRON_HELMET||item.getType()==Material.LEATHER_HELMET){
				ench = Enchantment.WATER_WORKER;
				level = 1;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("infinity")){
			if(item.getType()==Material.BOW){
				ench = Enchantment.ARROW_INFINITE;
				level = 1;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("flame")){
			if(item.getType()==Material.BOW){
				ench = Enchantment.ARROW_FIRE;
				level = 1;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("punch")){
			if(item.getType()==Material.BOW){
				ench = Enchantment.ARROW_KNOCKBACK;
				level = 2;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(enchantment.equalsIgnoreCase("power")){
			if(item.getType()==Material.BOW){
				ench = Enchantment.ARROW_DAMAGE;
				level = 5;
				cannot = false;
			} else {
				cannot = true;
			}
		}
		if(cannot){
			player.sendMessage(green+"You cannot enchant that item with that enchantment!");
			player.sendMessage(gold+"  Swords: "+white+"smite, fireaspect, knockback, baneofarthropods, sharpness, looting");
			player.sendMessage(gold+"  Armor: "+white+"protection, fireprotection, projectileprotection, blastprotection ");
			player.sendMessage(gold+"  Helmets: "+white+"aquaaffinity, respiration");
			player.sendMessage(gold+"  Tools: "+white+"silktouch, efficiency, fortune");
			player.sendMessage(gold+"  Bows: "+white+"infinity, power, punch, flame");
			return false;
		}
		if(!GiftBoxUsed(player.getName())){
			player.sendMessage(gold+"You do not have any available gifts");
			return false;
		}

		player.getItemInHand().addEnchantment(ench, level);
		player.sendMessage("Enchanted");
		return true;
	}
	public boolean GiftBoxChain(Player player){
		if(player.getInventory().getBoots()!=null||player.getInventory().getLeggings()!=null||player.getInventory().getChestplate()!=null||player.getInventory().getHelmet()!=null){
			player.sendMessage(green+"Must empty your armor slots for chainmail");
			return false;
		}
		if(!GiftBoxUsed(player.getName())){
			player.sendMessage(gold +"You do not have any available gifts");
			return false;
		}
		player.getInventory().setHelmet(new ItemStack(302, 1));
        player.getInventory().setChestplate(new ItemStack(303, 1));
        player.getInventory().setLeggings(new ItemStack(304, 1));
        player.getInventory().setBoots(new ItemStack(305, 1));
		player.sendMessage(gold + "Your armor is now chainmail");
		return true;
	}
	public void DropTables() throws SQLException { //Change "SampleFunction" to your own function name (Can be anything, unless it already exists)
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection
		Statement stmt = conn.createStatement();
		String sql = "DROP TABLE toothtransaction";
		stmt.executeUpdate(sql);
		sql = "DROP TABLE toothgiftbox";
		stmt.executeUpdate(sql);
		conn.close(); //Closes the connection
	}
	public String escape(String string){
		String result = StringEscapeUtils.escapeJava(string.replaceAll("'", "").replaceAll("\\\\", "").replaceAll("\"", ""));
		return result;
	}
	public void Insert(String player, float amount, Timestamp timestamp) throws SQLException {
		player = escape(player);
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection
		Statement stmt = conn.createStatement();
		String sql = "INSERT INTO toothtransaction (player, amount, timestamp, used, ipn) VALUES ('"+player+"', "+amount+", '"+timestamp+"', 0, null)";
		stmt.executeUpdate(sql); //Executes the query
		stmt.close(); //Closes the query
		conn.close(); //Closes the connection
	}
	public void Delete(int id) throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass); //Creates the connection
		Statement stmt = conn.createStatement();
		String sql = "DELETE * FROM toothtransaction WHERE id='"+id+"'";
		stmt.executeUpdate(sql); //Executes the query
		stmt.close(); //Closes the query
		conn.close(); //Closes the connection
	}
	public void GetAllBalances(Player player, int page) throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		this.log.info("Running Promotion check!");
		int[] range = {page*8-7, page*8-6, page*8-5, page*8-4, page*8-3, page*8-2, page*8-1, page*8};
		try {
			String query = "SELECT player as player, SUM(amount) as amount FROM toothtransaction GROUP BY player";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int result = 0;
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
	public void GetGifts(Player player, int page) throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		this.log.info("Running Promotion check!");
		int[] range = {page*8-7, page*8-6, page*8-5, page*8-4, page*8-3, page*8-2, page*8-1, page*8};
		try {
			String query = "SELECT player, milestone FROM toothgiftbox GROUP BY player";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int result = 0;
		    while (rs.next()) {
		    	result++;
		    	if(rs.getObject("player") != null){
		    		for(int i = 0; i<8;i++){
		    			if(range[i]==result){
		    				player.sendMessage(gold+rs.getString("player")+ " is at the $"+rs.getInt("milestone")+ " milestone!");
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
	public void UpdateAccounts() throws SQLException {
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
		    		for(int current = 1; current < tiers-1; current++){
		    			if((amount>=(float)config.getDouble("Config.Ranks.Rank"+current+".Minimum_Donation"))&&amount<((float)config.getDouble("Config.Ranks.Rank"+(current+1)+".Minimum_Donation")) && !perms.playerInGroup(world, player, config.getString("Config.Ranks.Rank"+current+".Name"))){
		    				tier = current;
		    				promote = true;
		    			}
		    		}
		    	    if((amount>=(float)config.getDouble("Config.Ranks.Rank"+tiers+".Minimum_Donation"))&& !perms.playerInGroup(world, player, config.getString("Config.Ranks.Rank"+tiers+".Name"))){
			    		tier = tiers;
			    		promote = true;
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
	public void UpdateNewDonations() throws SQLException {
		Connection conn = DriverManager.getConnection(url, user, pass);
		this.log.info("Running Donation check!");
		try {
			String query = "SELECT player, amount, used FROM toothtransaction WHERE NOT used = 1 GROUP BY player";
		    Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(query);
		    query = "UPDATE toothtransaction SET used = 1 WHERE used = 0";
		    if(onlinemode){
		    	query=query+" AND NOT ('1'='0'";
		    }
		    while (rs.next()) {
		    	if(rs.getObject("player") != null){
		    		String player = rs.getString("player");
		    		if(!(getServer().getPlayer(player)!=null&&onlinemode)){
		    			float Donation = Math.round(rs.getFloat("amount"));
		    			DonationEvent event = new DonationEvent(player, Donation);
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
		    		if(!(getServer().getPlayer(player)!=null&&onlinemode)&&(current-active)>=expire){
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
			String query = "SELECT player, cost, package, status, activated FROM toothpackages WHERE status = '1' AND player = '"+player.getName()+"' AND package ='"+packageoption+"'";
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
			String query = "SELECT id, player, cost, package, status, activated FROM toothpackages WHERE status = '1' AND player = '"+player.getName()+"' AND package ='"+packageoption+"'";
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
	public void packageActivation(Player player, String packageoption){
		if(!packages){
			return;
		}
		if(!isPackage(packageoption)){
			player.sendMessage("Invalid package");
			return;
		}
		if(!hasPackage(player, packageoption)){
			player.sendMessage("You do not have that package");
			return;
		}
		if(!hasRoom(player, packageoption)){
			return;
		}
		if(!usePackage(player, packageoption)){
			return;
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
		performCommands(player.getName(), packageoption, parseCommands(player.getName(), packageoption, "Activation"));
		player.sendMessage("Package successfully activated!");
		player.sendMessage("Package will expire in " +config.getInt("Config.Packages."+packageoption+".Days")+" days.");
	}
	public boolean packageExpiration(String player, String packageoption){
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
		performCommands(player, packageoption, parseCommands(player, packageoption, "Expiration"));
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
	public void performCommands(String player, String packageoption, Object[] commands){
		for(Object command: commands){
			if(command!=null){
				System.out.println(command.toString());
				getServer().dispatchCommand(getServer().getConsoleSender(), command.toString());
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
	public void CustomGiftBox(String gift, Player player, String args1, String args2, String args3){
		if(TotalGiftBox(player.getName())<1){
			player.sendMessage(gold+"You do not have any available gifts!");
			return;
		}
		CustomGiftBoxEvent event = new CustomGiftBoxEvent(player, gift, args1, args2, args3);
		this.getServer().getPluginManager().callEvent(event);
		if(!event.isCancelled()){
			GiftBoxUsed(player.getName());
		}	
	}
	private void ListPackages(Player player, String name, int page) {
		name = escape(name);
		if(page==0){
			page=1;
		}
		try {
			Connection conn = DriverManager.getConnection(url, user, pass);
			String query = "SELECT player, package FROM toothpackages WHERE status = '1' AND player = '"+name+"'";
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
				player.sendMessage(" -"+green+rs.getString("No Available Gifts"));
		    }
		} catch (SQLException e) {
			error(e);
		}
		
	}
	public void error(SQLException e){
		System.out.println("There is a problem with T00thTransaction's database connection");
		e.printStackTrace();
	}
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
			if(cmd.getName().equalsIgnoreCase("packages") && isPackagesOn()){
				int page = 0;
				String name = player.getName();
				if(args.length ==1) {
					try{
					page = Integer.parseInt(args[0]);
					} catch (NumberFormatException e){
						name = args[0];
						try{
							page = Integer.parseInt(args[1]);
						} catch (NumberFormatException e1){
						}	
					}		
				}
				ListPackages(player, name, page);
			}
			if(cmd.getName().equalsIgnoreCase("giftbox") && isGiftboxOn()){
				player.sendMessage("You have "+TotalGiftBox(player.getName())+" gifts available");
				Object[] go = Giftbox.keySet().toArray();
				for(int temp=0; temp<go.length;temp++){
					player.sendMessage(" - \"/gift "+go[temp].toString()+"\"  "+Giftbox.get(go[temp]));
				}
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

			if(cmd.getName().equalsIgnoreCase("gift") && isGiftboxOn() && args.length>=1){
				if(args[0].equalsIgnoreCase("chain")&&args.length==1&&config.getBoolean("Config.Settings.GiftBox.Chainmail")==true){
					if(GiftBoxChain(player)){
						player.sendMessage("Your chain has been placed in your armor slots");
						return true;
					}
					return false;
				}
				if(args[0].equalsIgnoreCase("enchant")&&args.length==2&&config.getBoolean("Config.Settings.GiftBox.Enchant")==true){
					if(GiftBoxEnchant(player, args[1])){
						player.sendMessage("Your item has been enchanted");
						return true;
					}
					return false;
				}
				if(Giftbox.containsKey(args[0])){
					if(args.length==4){
						CustomGiftBox(args[0], player, args[1], args[2], args[3]);
					} else if (args.length==3){
						CustomGiftBox(args[0], player, args[1], args[2], null);
					} else if (args.length==2){
						CustomGiftBox(args[0], player, args[1], null, null);
					} else {
						CustomGiftBox(args[0], player, null, null, null);
					}
					return true;
				}
				player.sendMessage(green+"Not a valid gift!");
				return false;
			}
		}
		if(args.length==0){
			return false;
		}
		if(cmd.getName().equalsIgnoreCase("ttr")){
			if(args[0].equalsIgnoreCase("add") && (args.length==3 || args.length==4)){
				if(!player.hasPermission("t00thtransaction.admin")){
					player.sendMessage(gold+"No soup for you");
					return true;
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
					Insert(args[1], Float.valueOf(args[2]), ts);
					this.log.info(args[1]+" has been added with the amount: $"+args[2]);
					return true;
				} catch (SQLException e){
					error(e);
				}		
			}
			if(args[0].equalsIgnoreCase("addgift") && args.length==2){
				if(!player.hasPermission("t00thtransaction.admin")){
					player.sendMessage(gold+"No soup for you");
					return true;
				}
				if(sender instanceof Player){
					if(GiftBoxAdd(args[1])){
						sender.sendMessage(args[1]+" has had a gift added");
					} else {
						sender.sendMessage("Failed to add gift");
					}
				} else {
					if(GiftBoxAdd(args[1])){
						this.log.info(args[1]+" has had a gift added");
					} else {
						this.log.info("Failed to add gift");
					}
				}
				return true;		
			}
			if(args[0].equalsIgnoreCase("removegift") && args.length==2){
				if(!player.hasPermission("t00thtransaction.admin")){
					player.sendMessage(gold+"No soup for you");
					return true;
				}
				if(sender instanceof Player){
					if(GiftBoxRemove(args[1])){
						sender.sendMessage(args[1]+" has had a gift removed");
					} else {
						sender.sendMessage("Failed to remove gift");
					}
				} else {
					if(GiftBoxRemove(args[1])){
						this.log.info(args[1]+" has had a gift removed");
					} else {
						this.log.info("Failed to remove gift");
					}
				}
				return true;		
			}
			if(args[0].equalsIgnoreCase("delete") && (args.length==1 || args.length==2) && !(sender instanceof Player)){
				if(args.length==2){
					System.out.println("Are you sure you want to delete entry: "+args[1]);
					System.out.println("\"/ttr delete ENTRYID confirm\"  to confirm");
					return false;
				}
				if(args.length==3&&args[2].equalsIgnoreCase("confirm"))
				try{
					Delete(Integer.valueOf(args[1]));
					this.log.info("Entry "+args[1]+" has been deleted.");
					return true;
				} catch (SQLException e){
					error(e);
				}		
			}
			if(args[0].equalsIgnoreCase("check")){
				if(sender instanceof Player){
					if(!player.hasPermission("t00thtransaction.admin")&&!player.hasPermission("t00thtransaction.check")&&!player.getName().equals("args[1]")&&args.length==2){
						player.sendMessage(gold+"No soup for you");
						return true;
					} 
					String playername;
					if(args.length==2){
						playername = args[1];
					} else {
						playername = player.getName();
					}
					float total = 0;
					total = getTotal(playername);
					player.sendMessage(gold+playername+": $"+white+total);
					return true;
				} else { 
					float total = 0;
					total = getTotal(args[1]);
					this.log.info(args[1]+": $"+total);
					return true;
				}
			}
			if(args[0].equalsIgnoreCase("listcheck")){
				if(sender instanceof Player){
					if(!player.hasPermission("t00thtransaction.admin")&&!player.hasPermission("t00thtransaction.check")&&!player.getName().equals("args[1]")&&args.length==2){
						player.sendMessage(gold+"No soup for you");
						return true;
					} 
					String playername;
					if(args.length==2){
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
			if(args[0].equalsIgnoreCase("checkgifts")&&(args.length==1||args.length==2)){
				if(player!=null){
					if(!player.hasPermission("t00thtransaction.admin")){
						player.sendMessage(gold+"No soup for you");
						return true;
					}
				}
				try{
					if(player!=null){
						if(args.length==1){
							GetGifts(player, 1);
						} else {
							GetGifts(player, Integer.valueOf(args[1]));
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
						UpdateAccounts();
					}
					UpdateNewDonations();
					if(player!=null){
						player.sendMessage(gold+"Donator rank check has been run!");
					} else {
						this.log.info("Donator rank check has been run!");
					}
					return true;
				} catch (SQLException e){
					error(e);
				}
			}
		}
		if(player!=null){
			player.sendMessage("Command failed");
		} else {
			this.log.info("Command failed");
		}
		return false;
	}
	/**
	 * @return Whether or not Giftbox feature is on
	 */
	public static boolean isGiftboxOn() {
		return giftbox;
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
}
