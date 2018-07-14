package me.egg82.sprc.events;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPistonRetractEvent;

import me.egg82.sprc.buffers.BlockDataBuffer;
import me.egg82.sprc.core.BlockDataInsertContainer;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.HighEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogBlockPistonRetractEvent extends HighEventHandler<BlockPistonRetractEvent> {
	//vars
	private DoubleBuffer<BlockDataInsertContainer> buffer = ServiceLocator.getService(BlockDataBuffer.class);
	
	//constructor
	public LogBlockPistonRetractEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (event.isCancelled()) {
			return;
		}
		
		// Create the container beforehand so we don't have stale data
			BlockDataInsertContainer container = new BlockDataInsertContainer(event.getBlock().getState());
			ThreadUtil.submit(new Runnable() {
				public void run() {
					// getCurrentBuffer has the potential to lock the current thread
					buffer.getCurrentBuffer().add(container);
				}
			});
		
		for (Block block : event.getBlocks()) {
			// Create the container beforehand so we don't have stale data
			BlockDataInsertContainer container2 = new BlockDataInsertContainer(block.getState());
			ThreadUtil.submit(new Runnable() {
				public void run() {
					// getCurrentBuffer has the potential to lock the current thread
					buffer.getCurrentBuffer().add(container2);
				}
			});
		}
	}
}
