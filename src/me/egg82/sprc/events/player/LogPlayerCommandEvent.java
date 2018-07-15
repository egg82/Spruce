package me.egg82.sprc.events.player;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.egg82.sprc.Config;
import me.egg82.sprc.buffers.PlayerChatBuffer;
import me.egg82.sprc.core.PlayerChatInsertContainer;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.MonitorEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogPlayerCommandEvent extends MonitorEventHandler<PlayerCommandPreprocessEvent> {
	//vars
	private DoubleBuffer<PlayerChatInsertContainer> buffer = ServiceLocator.getService(PlayerChatBuffer.class);
	
	//constructor
	public LogPlayerCommandEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (!Config.playerConfig.command) {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		// Create the container beforehand so we don't have stale data
		PlayerChatInsertContainer container = new PlayerChatInsertContainer(event.getPlayer().getUniqueId(), event.getMessage());
		ThreadUtil.submit(new Runnable() {
			public void run() {
				// getCurrentBuffer has the potential to lock the current thread
				buffer.getCurrentBuffer().add(container);
			}
		});
	}
}
