package me.egg82.sprc.core;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import me.egg82.sprc.enums.EntityDataType;

public class EntityDataInsertContainer {
	//vars
	private UUID actorUuid = null;
	private UUID entityUuid = null;
	private EntityType type = null;
	private Location entityLocation = null;
	private EntityDataType dataType = null;
	private ItemStack[] inventory = null;
	
	//constructor
	public EntityDataInsertContainer(UUID entityUuid, EntityType type, Location entityLocation, EntityDataType dataType) {
		this(new UUID(0L, 0L), entityUuid, type, entityLocation, dataType, null);
	}
	public EntityDataInsertContainer(UUID actorUuid, UUID entityUuid, EntityType type, Location entityLocation, EntityDataType dataType) {
		this(actorUuid, entityUuid, type, entityLocation, dataType, null);
	}
	public EntityDataInsertContainer(UUID entityUuid, EntityType type, Location entityLocation, EntityDataType dataType, ItemStack[] inventory) {
		this(new UUID(0L, 0L), entityUuid, type, entityLocation, dataType, inventory);
	}
	public EntityDataInsertContainer(UUID actorUuid, UUID entityUuid, EntityType type, Location entityLocation, EntityDataType dataType, ItemStack[] inventory) {
		this.actorUuid = actorUuid;
		this.entityUuid = entityUuid;
		this.type = type;
		this.entityLocation = entityLocation;
		this.dataType = dataType;
		this.inventory = inventory;
	}
	
	//public
	public UUID getActorUuid() {
		return actorUuid;
	}
	public UUID getEntityUuid() {
		return entityUuid;
	}
	public EntityType getType() {
		return type;
	}
	public Location getEntityLocation() {
		return entityLocation;
	}
	public EntityDataType getDataType() {
		return dataType;
	}
	public ItemStack[] getInventory() {
		return inventory;
	}
	
	//private
	
}
