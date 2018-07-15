package me.egg82.sprc.events.block;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.block.CauldronLevelChangeEvent;

import me.egg82.sprc.Config;
import me.egg82.sprc.buffers.BlockDataBuffer;
import me.egg82.sprc.core.BlockDataInsertContainer;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.MonitorEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogCauldronLevelChangeEvent extends MonitorEventHandler<CauldronLevelChangeEvent> {
	//vars
	private DoubleBuffer<BlockDataInsertContainer> buffer = ServiceLocator.getService(BlockDataBuffer.class);
	
	//constructor
	public LogCauldronLevelChangeEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (!Config.blockConfig.cauldron) {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		// Create the container beforehand so we don't have stale data
		BlockDataInsertContainer container = new BlockDataInsertContainer((event.getEntity() != null && event.getEntity() instanceof Player) ? event.getEntity().getUniqueId() : new UUID(0L, 0L), event.getBlock().getState());
		ThreadUtil.submit(new Runnable() {
			public void run() {
				// getCurrentBuffer has the potential to lock the current thread
				buffer.getCurrentBuffer().add(container);
			}
		});
	}
}
