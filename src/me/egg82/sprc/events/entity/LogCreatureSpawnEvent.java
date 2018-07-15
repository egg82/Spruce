package me.egg82.sprc.events.entity;

import org.bukkit.event.entity.CreatureSpawnEvent;

import me.egg82.sprc.Config;
import me.egg82.sprc.buffers.EntityDataBuffer;
import me.egg82.sprc.core.EntityDataInsertContainer;
import me.egg82.sprc.enums.EntityDataType;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.handlers.events.MonitorEventHandler;
import ninja.egg82.utils.ThreadUtil;

public class LogCreatureSpawnEvent extends MonitorEventHandler<CreatureSpawnEvent> {
	//vars
	private DoubleBuffer<EntityDataInsertContainer> buffer = ServiceLocator.getService(EntityDataBuffer.class);
	
	//constructor
	public LogCreatureSpawnEvent() {
		super();
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (!Config.entityConfig.spawn) {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		// Create the container beforehand so we don't have stale data
		EntityDataInsertContainer container = new EntityDataInsertContainer(event.getEntity().getUniqueId(), event.getEntityType(), event.getEntity().getLocation().clone(), EntityDataType.SPAWN);
		ThreadUtil.submit(new Runnable() {
			public void run() {
				// getCurrentBuffer has the potential to lock the current thread
				buffer.getCurrentBuffer().add(container);
			}
		});
	}
}
