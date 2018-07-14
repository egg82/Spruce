package me.egg82.sprc.core;

import java.util.UUID;

public class PlayerChatInsertContainer {
	//vars
	private UUID playerUuid = null;
	private String chat = null;
	
	//constructor
	public PlayerChatInsertContainer(UUID playerUuid, String chat) {
		this.playerUuid = playerUuid;
		this.chat = chat;
	}
	
	//public
	public UUID getPlayerUuid() {
		return playerUuid;
	}
	public String getChat() {
		return chat;
	}
	
	//private
	
}
