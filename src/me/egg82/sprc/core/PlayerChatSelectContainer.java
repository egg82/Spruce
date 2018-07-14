package me.egg82.sprc.core;

public class PlayerChatSelectContainer {
	//vars
	private String chat = null;
	private long time = -1L;
	
	//constructor
	public PlayerChatSelectContainer(String chat, long time) {
		this.chat = chat;
		this.time = time;
	}
	
	//public
	public String getChat() {
		return chat;
	}
	public long getTime() {
		return time;
	}
	
	//private
	
}
