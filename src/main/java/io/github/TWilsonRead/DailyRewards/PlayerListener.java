package io.github.TWilsonRead.DailyRewards;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

public class PlayerListener implements Listener {
	
	//Commented lines are tests for output.
   
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent player)
	{
		//When they log in, check if they have already logged in today. If not, update their weekly login stats and last logged in date. Also set their claimed status to false.
        ArrayList<PlayerReward> playerRewards = new ArrayList<PlayerReward>();
		try {
			//Finish this
			File newFile = new File("plugins/DailyRewards/DailyRewards.txt");
			newFile.createNewFile();
			FileInputStream fileInput = new FileInputStream("plugins/DailyRewards/DailyRewards.txt");
	        BukkitObjectInputStream objectInput = new BukkitObjectInputStream(fileInput);
	        boolean found = false;
	        while (!found) {
	        	PlayerReward playerReward = (PlayerReward) objectInput.readObject();	
	        	playerRewards.add(playerReward);
	        	if (playerReward.getPlayerUUID().equals(player.getPlayer().getUniqueId())) {
	        		found = true;
	        	}
		    }       
	        fileInput.close();
	        objectInput.close();
	        for (PlayerReward p : playerRewards) {
	        	if (p.getPlayerUUID().equals(player.getPlayer().getUniqueId())) {
	        		if (p.getLastLoggedIn().after(p.getNextRewardDate())) {
	        			//This player is due a reward.
	        			p.setCollectedTodaysReward(false);
	        		}
	        		p.setLastLoggedIn(Calendar.getInstance());
	        		//Deletes file with old data in and writes a new one. SHOULD BE REPLACED WITH SQL (maybe) to improve efficiency
	        		updateAndDelete(playerRewards);
	        		//System.out.println("[DailyRewards_Test] Player data updated!");
	        	}	
	        }
		} catch (EOFException e) {
			//Expected if a player is not in the file.
			PlayerReward newPlayerReward = new PlayerReward(player.getPlayer().getUniqueId());
			playerRewards.add(newPlayerReward);
    		updateAndDelete(playerRewards);
    		//System.out.println("[DailyRewards_Test]
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    	System.out.println("[DailyRewards] Something went wrong when a player logged in.");
	    }
	}
	
	public void updateAndDelete(ArrayList<PlayerReward> playerRewards) {
		File oldFile = new File("plugins/DailyRewards/DailyRewards.txt");
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

}
