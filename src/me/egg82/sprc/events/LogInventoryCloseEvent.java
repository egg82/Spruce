package me.egg82.sprc.events;

import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.egg82.sprc.buffers.BlockDataBuffer;
import me.egg82.sprc.core.BlockDataInsertContainer;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.HighEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogInventoryCloseEvent extends HighEventHandler<InventoryCloseEvent> {
	//vars
	private DoubleBuffer<BlockDataInsertContainer> buffer = ServiceLocator.getService(BlockDataBuffer.class);
	
	//constructor
	public LogInventoryCloseEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (event.getInventory().getHolder() == null) {
			return;
		}
		
		if (event.getInventory().getHolder() instanceof BlockState) {
			// Create the container beforehand so we don't have stale data
			BlockDataInsertContainer container = new BlockDataInsertContainer(event.getPlayer().getUniqueId(), (BlockState) event.getInventory().getHolder());
			ThreadUtil.submit(new Runnable() {
				public void run() {
					// getCurrentBuffer has the potential to lock the current thread
					buffer.getCurrentBuffer().add(container);
				}
			});
		} else if (event.getInventory().getHolder() instanceof DoubleChest) {
			// Create the container beforehand so we don't have stale data
			BlockDataInsertContainer left = new BlockDataInsertContainer(event.getPlayer().getUniqueId(), (BlockState) ((DoubleChest) event.getInventory().getHolder()).getLeftSide());
			BlockDataInsertContainer right = new BlockDataInsertContainer(event.getPlayer().getUniqueId(), (BlockState) ((DoubleChest) event.getInventory().getHolder()).getRightSide());
			ThreadUtil.submit(new Runnable() {
				public void run() {
					// getCurrentBuffer has the potential to lock the current thread
					buffer.getCurrentBuffer().add(left);
					buffer.getCurrentBuffer().add(right);
				}
			});
		}
	}
}
