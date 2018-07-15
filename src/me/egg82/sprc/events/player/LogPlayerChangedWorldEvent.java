package me.egg82.sprc.events.player;

import org.bukkit.event.player.PlayerChangedWorldEvent;

import me.egg82.sprc.Config;
import me.egg82.sprc.buffers.PlayerDataBuffer;
import me.egg82.sprc.core.PlayerDataInsertContainer;
import me.egg82.sprc.enums.PlayerDataType;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.MonitorEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogPlayerChangedWorldEvent extends MonitorEventHandler<PlayerChangedWorldEvent> {
	//vars
	private DoubleBuffer<PlayerDataInsertContainer> buffer = ServiceLocator.getService(PlayerDataBuffer.class);
	
	//constructor
	public LogPlayerChangedWorldEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (!Config.playerConfig.worldChange) {
			return;
		}
		
		// Create the container beforehand so we don't have stale data
		PlayerDataInsertContainer container = new PlayerDataInsertContainer(event.getPlayer().getUniqueId(), event.getPlayer().getLocation().clone(), PlayerDataType.WORLD_CHANGE);
		ThreadUtil.submit(new Runnable() {
			public void run() {
				// getCurrentBuffer has the potential to lock the current thread
				buffer.getCurrentBuffer().add(container);
			}
		});
	}
}
