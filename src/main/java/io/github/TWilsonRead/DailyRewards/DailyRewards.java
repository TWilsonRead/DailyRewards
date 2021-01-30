package io.github.TWilsonRead.DailyRewards;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import io.github.TWilsonRead.DailyRewards.PlayerListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;

public final class DailyRewards extends JavaPlugin {
	
	private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

	
	@Override
	public void onEnable() {
	    getServer().getPluginManager().registerEvents(new PlayerListener(), this);
	    getLogger().info("has been enabled!");
	    if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupPermissions();
        setupChat();
        Path path = Paths.get("plugins/DailyRewards/DailyRewards.txt");
        try {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        } catch (FileAlreadyExistsException e) {
        } catch (IOException e) {        	
		}
	}
	
	@Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
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
    
    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }
    
    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        ArrayList<PlayerReward> playerRewards = new ArrayList<PlayerReward>();
        if (sender instanceof Player) {
        	Player player = (Player) sender;
        	if (cmd.getName().equalsIgnoreCase("dailyrewards")) {
				if (args.length == 0) {
					player.sendMessage(ChatColor.DARK_BLUE + "[Daily Reward] " + ChatColor.WHITE + "Help Menu");
					player.sendMessage(ChatColor.GRAY + ">> Type /dailyrewards claim to claim your daily reward.");
					player.sendMessage(ChatColor.GRAY + ">> Type /dailyrewards status to display your weekly login stats.");
			        return true;
			    } 
				else if (args.length == 1) {
					if (args[0].equalsIgnoreCase("claim")) {
						//Method for claiming a reward.
						//If player exists in the database
						//If player has not already claimed their reward
		        		try {
		        			FileInputStream fileInput = new FileInputStream("plugins/DailyRewards/DailyRewards.txt");
			    	        BukkitObjectInputStream objectInput = new BukkitObjectInputStream(fileInput);
			    	        boolean found = false;
			    	        boolean changed = false;
			    	        while (!found) {
			    	        	PlayerReward playerReward = (PlayerReward) objectInput.readObject();	
			    	        	if (playerReward.getPlayerUUID().equals(player.getUniqueId())) {
			    	        		found = true;
			    	        		if (!playerReward.isCollectedTodaysReward()) {
			    	        			//If this player is due a reward
			    	        			if ((playerReward.getLastRewardDate().get(Calendar.WEEK_OF_YEAR) == playerReward.getNextRewardDate().get(Calendar.WEEK_OF_YEAR)) && (playerReward.getLastRewardDate().get(Calendar.YEAR) == playerReward.getNextRewardDate().get(Calendar.YEAR))) {
			    	        				//If the consecutive login bonus needs to be reset or added
			    	        				playerReward.setWeeklyLoginCount(playerReward.getWeeklyLoginCount() + 1);
			    	        			}
			    	        			else {
			    	        				playerReward.setWeeklyLoginCount(1);
			    	        			}	
			    	        			giveReward(player, playerReward);
			    	        			Calendar nextRewardDate = Calendar.getInstance();
			    	        			playerReward.setLastRewardDate(playerReward.getNextRewardDate());
			    	        			nextRewardDate.add(Calendar.DATE, 1);
			    	        			nextRewardDate.set(Calendar.HOUR, 0);
			    	        			nextRewardDate.set(Calendar.MINUTE, 0);
			    	        			nextRewardDate.set(Calendar.SECOND, 0);
			    	        			nextRewardDate.set(Calendar.AM_PM, Calendar.AM);
			    	        			playerReward.setNextRewardDate(nextRewardDate);
			    	        			playerReward.setCollectedTodaysReward(true);
			    	        			found = true;
			    	        			changed = true;
			    	        		}
			    	        		else {
			    	        			player.sendMessage(ChatColor.DARK_RED + "[Daily Reward] You have already claimed today's reward.");		
			    	        		}
			    	        	}
			    	        	playerRewards.add(playerReward);
			    	        }
			    	        fileInput.close();
			    	        objectInput.close();
			    	        if (changed) {
				    	        updateAndDelete(playerRewards);
			    	        }
		        		}
		        		catch (Exception e) {
		        			e.printStackTrace();
		        			System.out.println("[DailyRewards] Something went wrong when trying to claim a reward.");
		        		}
					}
					else if (args[0].equalsIgnoreCase("status")) {
						//Method for displaying current status
		        		try {
		        			FileInputStream fileInput = new FileInputStream("plugins/DailyRewards/DailyRewards.txt");
			    	        BukkitObjectInputStream objectInput = new BukkitObjectInputStream(fileInput);
			    	        SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy, 'at', HH:mm z");
			    	        String date;
			    	        boolean found = false;
			    	        while (!found) {
			    	        	PlayerReward playerReward = (PlayerReward) objectInput.readObject();	
			    	        	if (playerReward.getPlayerUUID().equals(player.getUniqueId())) {
			    	        		found = true;
			    	        		player.sendMessage(ChatColor.DARK_BLUE + "[Daily Reward]" + ChatColor.WHITE + " Status for " + player.getName());
			    	        		player.sendMessage(ChatColor.GRAY + ">> You have logged in " + playerReward.getWeeklyLoginCount() + " day(s) this week.");
			    	        		date = sdf.format(playerReward.getNextRewardDate().getTime());
			    	        		player.sendMessage(ChatColor.GRAY + ">> Next Reward: " + date);
			    	        	}
			    	        }
		        		}
		        		catch (Exception e) {
		        			e.printStackTrace();
		        			System.out.println("[DailyRewards] Something went wrong when trying to access a player status.");
		        		}
					}
					else {
						sender.sendMessage(ChatColor.DARK_RED + "[Daily Reward] Unknown Command.");
					    return false;
					}
				}
				else {
					sender.sendMessage(ChatColor.DARK_RED + "[Daily Reward] Unknown Command.");
				    return false;
				}
			} else {
				sender.sendMessage(ChatColor.DARK_RED + "[Daily Reward] Unknown Command.");
				return false;
			}
        }
        else {
        	sender.sendMessage(ChatColor.DARK_RED + "[Daily Reward] This command must be run by a player!");
			return false;
        }
		
		return false;
	}
	
	public void updateAndDelete(ArrayList<PlayerReward> playerRewards) {
		File oldFile = new File("DailyRewards.txt");
		oldFile.delete();
		try {
			FileOutputStream fileOutput = new FileOutputStream("plugins/DailyRewards/DailyRewards.txt");
			BukkitObjectOutputStream objectOutput = new BukkitObjectOutputStream(fileOutput);
			for (PlayerReward q : playerRewards) {
				objectOutput.writeObject(q);
			}
			fileOutput.close();
			objectOutput.close();
		} catch (Exception e) {
	    	System.out.println("[DailyRewards] Something went wrong when trying to update the player data file.");
		}
	}
	
	public void giveReward(Player player, PlayerReward playerReward) {
		if (!Arrays.asList(player.getInventory().getStorageContents()).contains(null)) {
			player.sendMessage(ChatColor.DARK_RED + "[Daily Reward] Your inventory is full!");
		}
		else {
			
			int dailyRewardNum = playerReward.getWeeklyLoginCount();
			String rewardType;
			Material rewardMaterial;
			int rewardEconomy;
			String rewardCommand;
			int rewardAmount;
			String rewardDescription;
			
			try {
				Scanner s = new Scanner(new File("plugins/DailyRewards/DailyRewardsConfig.txt"));
				ArrayList<String> list = new ArrayList<String>();
				while (s.hasNextLine()){
				    list.add(s.nextLine());
				}
				s.close();
				int startIndex = (dailyRewardNum * 6) + 6;
				
				rewardType = list.get(startIndex).substring(6);
				System.out.println(rewardType);
				rewardAmount = Integer.parseInt(list.get(startIndex + 2).substring(8));
				rewardDescription = list.get(startIndex + 3).substring(13);
				
				if (rewardType.equals("ITEM")) {
					rewardMaterial = Material.getMaterial(list.get(startIndex + 1).substring(6));
					player.getInventory().addItem(new ItemStack(rewardMaterial, rewardAmount));
					player.sendMessage(ChatColor.GREEN + "[Daily Reward] You have received " + rewardAmount + " " + rewardDescription + ".");
				}
				else if (rewardType.equals("ECONOMY")) {
					rewardEconomy = Integer.parseInt(list.get(startIndex + 1).substring(6));
					econ.depositPlayer(player, rewardEconomy);
					player.sendMessage(ChatColor.GREEN + "[Daily Reward] You have received $" + rewardAmount + " " + rewardDescription + ".");
				}
				else if (rewardType.equals("COMMAND_CRATEKEY")) {
					rewardCommand = list.get(startIndex + 1).substring(6) + " " + player.getName() + " " + rewardAmount;
					System.out.println("rewardCommand");
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCommand);
					player.sendMessage(ChatColor.GREEN + "[Daily Reward] You have received " + rewardAmount + " " + rewardDescription + ".");
				}
				else {
					System.out.println("[DailyRewards] Something went wrong whilst processing a reward.");
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Economy getEconomy() {
        return econ;
    }
    
    public static Permission getPermissions() {
        return perms;
    }
    
    public static Chat getChat() {
        return chat;
    }

}
