package me.jam.jamtools;

/*
 * JamTools
 * A number of tools for moderators and players on Mumbo Jumbo's patreon server
 * Developer: Stefan Cole
 * Date: 2016-JUN-06
 * 
 * Change Log
 * 1.0.0 = Created death command, with moderator option to teleport
 * 
 */

import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.sun.jndi.ldap.Connection;

public class JamTools extends JavaPlugin
{

	Player player = null;           // The player running the command
	Player targetPlayer = null;     // The player being targetted
	Location targetLocation = null; // The location where the target's death occurred
	
    //MySQL Connection variables
	private Connection connection;
    private String host, database, username, password;
    private int port;
    
    // Logger for chat messages
	public final Logger logger = Logger.getLogger("Minecraft");
	
	// Server Plug in Description File
	PluginDescriptionFile pdFile = getDescription();

	// Method called when the plug in is enabled.
	public void onEnable()
	{
        host = "localhost";
        port = 3306;
        database = "TestDatabase";
        username = "user";
        password = "pass";    
        try {     
            openConnection();
            Statement statement = connection.createStatement();          
        } catch (ClassNotFoundException e) {
            this.logger.info(pdFile.getName() + " WARNING: Unable to connect to MySQL database: Class Not Found");
            e.printStackTrace();
        } catch (SQLException e) {
            this.logger.info(pdFile.getName() + " WARNING: Unable to connect to MySQL database: SQL Exception");
            e.printStackTrace();
        }

        this.logger.info(pdFile.getName() + " ver " + pdFile.getVersion() + " Enabled");
	}	
	
	// Method called when plug in is disabled.
	public void onDisable()
	{
		this.logger.info(pdFile.getName() + " Disabled");
	}

	// Try to connect to the SQL Database
    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }
    
    // Function called when a command is entered into the MC chat
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) 
	{
		
		// Adds a log to the console
		this.logger.info(sender.getName() + " used JamTools: " + label);
		
		// Assigns the player from the sender of the command
		player = (Player) sender; // Grab the player class from the sender of the command
		
		/*
		 * 
		 * Check for the commands passed
		 * 
		 */
		
		// passed command runs the Death command
		if(label.equalsIgnoreCase("death")) // If the user passes the clc command
		{
			// Check to see if the player has permissions
			if(!player.hasPermission("jamtools.death")) 
			{
				player.sendMessage("Don't do that, it tickles!");
				return false;
			}

			/*
			 * All checks passed, run the Death command
			 */
			if (args.length == 0) {  // No args, so lets just show current player's death location
				
				// Checks to see if an actual player is sending the command
				if(!(sender instanceof Player)) 
				{
					sender.sendMessage("The Console never dies, so why do you think it would have a death location? Dumbass!");
					return false;
				}
				
				targetPlayer = player;
				targetLocation = getDeathLocation();
				if((targetLocation == null) || !(targetLocation instanceof Location)) 
				{
					displayIdiotError("You ain't died recently!");
					return false;
				}
				player.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " last died at:");
				player.sendMessage(ChatColor.RED + "World: " + ChatColor.GOLD + targetLocation.getWorld().toString());
				player.sendMessage(ChatColor.RED + "X: " + ChatColor.GOLD + targetLocation.getBlockX().toString());
				player.sendMessage(ChatColor.RED + "Y: " + ChatColor.GOLD + targetLocation.getBlockY().toString());
				player.sendMessage(ChatColor.RED + "Z: " + ChatColor.GOLD + targetLocation.getBlockZ().toString());
			}
			else if(args.length >= 1) // If we have arguments
			{
				String myArg = args[0].toString(); // Put the argument in a var
						
				// HELP
				// Display the help for the plug in. arg: help or h
				if(myArg.equalsIgnoreCase("help") || myArg.equalsIgnoreCase("h"))
				{
					player.sendMessage(ChatColor.GOLD + "Help for " + pdFile.getName() + " ver " + pdFile.getVersion());
					player.sendMessage(ChatColor.GOLD + "/death info" + ChatColor.WHITE + " : Displays plugin info");
					player.sendMessage(ChatColor.GOLD + "/death help" + ChatColor.WHITE + " : Displays this help page");
					player.sendMessage(ChatColor.GOLD + "/death" + ChatColor.WHITE + " : Displays your last death location");
					// Does the player have extra permissions?
					if(player.hasPermission("jamtools.death.others")) 
					{
						player.sendMessage(ChatColor.GOLD + "/death <player>" + ChatColor.WHITE + " : Returns the location of a players last death");
					}
					if(player.hasPermission("jamtools.death.tp")) 
					{
						player.sendMessage(ChatColor.GOLD + "/death tp <player>" + ChatColor.WHITE + " : Teleports you to the location of a players last death");
					}
						
					return false;
				}
						
				// INFO
				// Display Info arg: info or i
				else if(myArg.equalsIgnoreCase("info") || myArg.equalsIgnoreCase("i")) 
				{
					player.sendMessage(ChatColor.GREEN + pdFile.getName() + " ver " + pdFile.getVersion());
					player.sendMessage(ChatColor.GREEN + "Created for the Mumbo Jumbo Patreon Server by Jamdoggy");
					player.sendMessage(ChatColor.GREEN + "Written on 6th June 2016 for Spigot 1.9.4");
					return false;
				}
						
				// Plug-in LOVE
				else if(myArg.equalsIgnoreCase("<3")) 
				{
					player.sendMessage(ChatColor.AQUA + "Ahhh Jamdoggy <3's you too!!!");
					return false;
				}
				// Teleportation!
				else if(myArg.equalsIgnoreCase("tp")) 
				{
					// Check to see if the player has permissions
					if(!player.hasPermission("jamtools.death.tp")) 
					{
						player.sendMessage("Don't do that, it tickles!");
						return false;
					}

					targetPlayer = Bukkit.getPlayer(args[1].toString());  // 2nd arg should be a player

					if((targetPlayer == null) || !(targetPlayer instanceof Player)) 
					{
						displayIdiotError(args[1].toString() + " ain't bein' a player here!");
						sender.sendMessage("");
						return false;
					}					

					TeleportToDeath();
				}
				else   // Not one of the above - assume player name as 1st arg  
				{
					// Check to see if the player has permissions
					if(!player.hasPermission("jamtools.death.others")) 
					{
						player.sendMessage("Don't do that, it tickles!");
						return false;
					}
					
					targetPlayer = Bukkit.getPlayer(args[0].toString());
					if((targetPlayer == null) || !(targetPlayer instanceof Player)) 
					{
						displayIdiotError(args[0].toString() + " ain't bein' a player here!");
						return false;
					}
					
					targetLocation = getDeathLocation();
					if((targetLocation == null) || !(targetLocation instanceof Location)) 
					{
						displayIdiotError(args[0].toString() + " ain't died recently!");
						return false;
					}
					player.sendMessage(ChatColor.GREEN + targetPlayer.getName() + " last died at:");
					player.sendMessage(ChatColor.RED + "World: " + ChatColor.GOLD + targetLocation.getWorld().toString());
					player.sendMessage(ChatColor.RED + "X: " + ChatColor.GOLD + targetLocation.getBlockX().toString());
					player.sendMessage(ChatColor.RED + "Y: " + ChatColor.GOLD + targetLocation.getBlockY().toString());
					player.sendMessage(ChatColor.RED + "Z: " + ChatColor.GOLD + targetLocation.getBlockZ().toString());
					
				}
			}
			// If we get here, then all is good...
			
		}  // end of label == death
		
		return true;

	}
	
	// Display an ID-10-T Error in chat
	public void displayIdiotError(String err)
	{
		player.sendMessage(ChatColor.LIGHT_PURPLE + "ID-10-T Error: " + err);
	}
	
	/* TeleportToDeath
	 * Teleport player to last known death location of targetPlayer
	 */
	public void TeleportToDeath()
	{
	}
	
	/* getDeathLocation
	 * Get the last known location where a player died
	 */
	public Location getDeathLocation()
	{
	    Location locInfo;
		ResultSet result = statement.executeQuery("SELECT * FROM deathloc WHERE uuid='" + targetPlayer.getUniqueId().toString() + "';");
		while (result.next()) {
		    locInfo.setWorld(result.getString("world"));
		    locInfo.setX(Double.parseDouble((result.getString("x")));
		    locInfo.setY(Double.parseDouble((result.getString("y")));
		    locInfo.setZ(Double.parseDouble((result.getString("z")));
		}
	}

	/* setDeathLocation
	 * Store the last known location where a player died
	 */
	public Location setDeathLocation(Location locData)
	{
	    Location locInfo;
		ResultSet result = statement.executeQuery("SELECT * FROM deathloc WHERE uuid='" + targetPlayer.getUniqueId().toString() + "';");
		
		// If we have a result, then we need to update, not insert
		if ((result != null) && (result instanceof ResultSet)) {
		}
		    ResultSet result = statement.executeQuery("UPDATE deathloc SET (world=,x=,y=,z=) WHERE uuid='" + targetPlayer.getUniqueId().toString() + "';");
		} else {
		    ResultSet result = statement.executeQuery("INSERT INTO deathloc VALUES (uuid=,world=,x=,y=,z=);");
		}
		
		while (result.next()) {
		    locInfo.setWorld(result.getString("world"));
		    locInfo.setX(Double.parseDouble((result.getString("x")));
		    locInfo.setY(Double.parseDouble((result.getString("y")));
		    locInfo.setZ(Double.parseDouble((result.getString("z")));
		}
	}
}
