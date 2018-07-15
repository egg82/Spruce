package me.egg82.sprc.events;

import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryHolder;

import me.egg82.sprc.Config;
import me.egg82.sprc.buffers.BlockDataBuffer;
import me.egg82.sprc.core.BlockDataInsertContainer;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.MonitorEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogBlockBreakEvent extends MonitorEventHandler<BlockBreakEvent> {
	//vars
	private DoubleBuffer<BlockDataInsertContainer> buffer = ServiceLocator.getService(BlockDataBuffer.class);
	
	//constructor
	public LogBlockBreakEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (!Config.blockConfig.player) {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		// Create the container beforehand so we don't have stale data
		BlockState state = event.getBlock().getState();
		if (state instanceof InventoryHolder) {
			if (state instanceof DoubleChest) {
				BlockDataInsertContainer left = new BlockDataInsertContainer(event.getPlayer().getUniqueId(), (BlockState) ((DoubleChest) state).getLeftSide().getInventory().getHolder(), ((DoubleChest) state).getLeftSide().getInventory().getContents());
				BlockDataInsertContainer right = new BlockDataInsertContainer(event.getPlayer().getUniqueId(), (BlockState) ((DoubleChest) state).getRightSide().getInventory().getHolder(), ((DoubleChest) state).getRightSide().getInventory().getContents());
				ThreadUtil.submit(new Runnable() {
					public void run() {
						// getCurrentBuffer has the potential to lock the current thread
						buffer.getCurrentBuffer().add(left);
						buffer.getCurrentBuffer().add(right);
					}
				});
			} else {
				BlockDataInsertContainer container = new BlockDataInsertContainer(event.getPlayer().getUniqueId(), state, ((InventoryHolder) state).getInventory().getContents());
				ThreadUtil.submit(new Runnable() {
					public void run() {
						// getCurrentBuffer has the potential to lock the current thread
						buffer.getCurrentBuffer().add(container);
					}
				});
			}
		} else {
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
