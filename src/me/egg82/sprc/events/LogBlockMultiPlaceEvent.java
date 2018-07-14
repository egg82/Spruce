package me.egg82.sprc.events;

import org.bukkit.block.BlockState;
import org.bukkit.event.block.BlockMultiPlaceEvent;

import me.egg82.sprc.buffers.BlockDataBuffer;
import me.egg82.sprc.core.BlockDataInsertContainer;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.HighEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogBlockMultiPlaceEvent extends HighEventHandler<BlockMultiPlaceEvent> {
	//vars
	private DoubleBuffer<BlockDataInsertContainer> buffer = ServiceLocator.getService(BlockDataBuffer.class);
	
	//constructor
	public LogBlockMultiPlaceEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (event.isCancelled()) {
			return;
		}
		
		for (BlockState state : event.getReplacedBlockStates()) {
			// Create the container beforehand so we don't have stale data
			BlockDataInsertContainer container = new BlockDataInsertContainer(event.getPlayer().getUniqueId(), state);
			ThreadUtil.submit(new Runnable() {
				public void run() {
					// getCurrentBuffer has the potential to lock the current thread
					buffer.getCurrentBuffer().add(container);
				}
			});
		}
	}
}
