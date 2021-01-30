package io.github.TWilsonRead.DailyRewards;

import java.io.Serializable;
import java.util.Calendar;
import java.util.UUID;

import org.bukkit.entity.Player;

public class PlayerReward implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6333632556219384505L;
	private UUID playerUUID;
	private boolean collectedTodaysReward;
	private int weeklyLoginCount;
	private Calendar lastLoggedIn;
	private Calendar nextRewardDate;
	private Calendar lastRewardDate;
	
	public PlayerReward(UUID playerUUID) {
	    this.playerUUID = playerUUID;
	    this.collectedTodaysReward = false;
	    this.weeklyLoginCount = 1;
	    Calendar tempDate = Calendar.getInstance();
	    this.lastLoggedIn = tempDate;
	    tempDate.set(Calendar.DATE, 0);
	    tempDate.set(Calendar.MONTH, 0);
	    tempDate.set(Calendar.YEAR, 0);
	    tempDate.set(Calendar.HOUR, 0);
	    tempDate.set(Calendar.MINUTE, 0);
	    tempDate.set(Calendar.SECOND, 0);
	    tempDate.set(Calendar.AM_PM, Calendar.AM);
	    this.lastRewardDate = tempDate;
	    tempDate = Calendar.getInstance();
	    tempDate.add(Calendar.DATE, -1);
	    tempDate.set(Calendar.HOUR, 0);
	    tempDate.set(Calendar.MINUTE, 0);
	    tempDate.set(Calendar.SECOND, 0);
	    tempDate.set(Calendar.AM_PM, Calendar.AM);
	    this.nextRewardDate = tempDate;
	}

	public UUID getPlayerUUID() {
		return playerUUID;
	}

	public void setPlayerUUID(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}

	public boolean isCollectedTodaysReward() {
		return collectedTodaysReward;
	}

	public void setCollectedTodaysReward(boolean collectedTodaysReward) {
		this.collectedTodaysReward = collectedTodaysReward;
	}

	public int getWeeklyLoginCount() {
		return weeklyLoginCount;
	}

	public void setWeeklyLoginCount(int weeklyLoginCount) {
		this.weeklyLoginCount = weeklyLoginCount;
	}

	public Calendar getLastLoggedIn() {
		return lastLoggedIn;
	}

	public void setLastLoggedIn(Calendar lastLoggedIn) {
		this.lastLoggedIn = lastLoggedIn;
	}

	public Calendar getNextRewardDate() {
		return nextRewardDate;
	}

	public void setNextRewardDate(Calendar nextRewardDate) {
		this.nextRewardDate = nextRewardDate;
	}
	
	public Calendar getLastRewardDate() {
		return lastRewardDate ;
	}

	public void setLastRewardDate(Calendar lastRewardDate) {
		this.lastRewardDate = lastRewardDate;
	}
	
}
