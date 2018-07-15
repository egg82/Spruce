package me.egg82.sprc.events;

import org.bukkit.event.block.SignChangeEvent;

import me.egg82.sprc.Config;
import me.egg82.sprc.buffers.BlockDataBuffer;
import me.egg82.sprc.core.BlockDataInsertContainer;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.MonitorEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogSignChangeEvent extends MonitorEventHandler<SignChangeEvent> {
	//vars
	private DoubleBuffer<BlockDataInsertContainer> buffer = ServiceLocator.getService(BlockDataBuffer.class);
	
	//constructor
	public LogSignChangeEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (!Config.blockConfig.sign) {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		// Create the container beforehand so we don't have stale data
		BlockDataInsertContainer container = new BlockDataInsertContainer(event.getPlayer().getUniqueId(), event.getBlock().getState());
		ThreadUtil.submit(new Runnable() {
			public void run() {
				// getCurrentBuffer has the potential to lock the current thread
				buffer.getCurrentBuffer().add(container);
			}
		});
	}
}
