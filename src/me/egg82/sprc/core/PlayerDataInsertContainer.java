package me.egg82.sprc.core;

import java.util.UUID;

import org.bukkit.Location;

import me.egg82.sprc.enums.PlayerDataType;

public class PlayerDataInsertContainer {
	//vars
	private UUID playerUuid = null;
	private Location playerLocation = null;
	private PlayerDataType dataType = null;
	
	//constructor
	public PlayerDataInsertContainer(UUID playerUuid, Location playerLocation, PlayerDataType dataType) {
		this.playerUuid = playerUuid;
		this.playerLocation = playerLocation;
		this.dataType = dataType;
	}
	
	//public
	public UUID getPlayerUuid() {
		return playerUuid;
	}
	public Location getPlayerLocation() {
		return playerLocation;
	}
	public PlayerDataType getDataType() {
		return dataType;
	}
	
	//private
	
}
