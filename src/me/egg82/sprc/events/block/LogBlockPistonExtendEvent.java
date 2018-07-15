package me.egg82.sprc.events.block;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPistonExtendEvent;

import me.egg82.sprc.Config;
import me.egg82.sprc.buffers.BlockDataBuffer;
import me.egg82.sprc.core.BlockDataInsertContainer;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.MonitorEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogBlockPistonExtendEvent extends MonitorEventHandler<BlockPistonExtendEvent> {
	//vars
	private DoubleBuffer<BlockDataInsertContainer> buffer = ServiceLocator.getService(BlockDataBuffer.class);
	
	//constructor
	public LogBlockPistonExtendEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (!Config.blockConfig.piston) {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		List<BlockDataInsertContainer> containers = new ArrayList<BlockDataInsertContainer>();
		
		// Create the container beforehand so we don't have stale data
		containers.add(new BlockDataInsertContainer(event.getBlock().getState()));
		
		for (Block block : event.getBlocks()) {
			// Create the container beforehand so we don't have stale data
			containers.add(new BlockDataInsertContainer(block.getState()));
		}
		
		ThreadUtil.submit(new Runnable() {
			public void run() {
				// getCurrentBuffer has the potential to lock the current thread
				for (BlockDataInsertContainer container : containers) {
					buffer.getCurrentBuffer().add(container);
				}
			}
		});
	}
}
