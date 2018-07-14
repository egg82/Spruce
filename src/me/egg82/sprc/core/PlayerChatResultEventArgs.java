package me.egg82.sprc.core;

import java.util.List;
import java.util.UUID;

import ninja.egg82.patterns.events.EventArgs;

public class PlayerChatResultEventArgs extends EventArgs {
	//vars
	public static PlayerChatResultEventArgs EMPTY = new PlayerChatResultEventArgs(null, null);
	
	private UUID playerUuid = null;
	private List<PlayerChatSelectContainer> results = null;
	
	//constructor
	public PlayerChatResultEventArgs(UUID playerUuid, List<PlayerChatSelectContainer> results) {
		this.playerUuid = playerUuid;
		this.results = results;
	}
	
	//public
	public UUID getPlayerUuid() {
		return playerUuid;
	}
	public List<PlayerChatSelectContainer> getResults() {
		return results;
	}
	
	//private
	
}
