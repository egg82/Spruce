package me.egg82.sprc.core;

import java.util.UUID;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

public class BlockDataInsertContainer {
	//vars
	private UUID actorUuid = null;
	private BlockState blockState = null;
	private ItemStack[] inventory = null;
	
	//constructor
	public BlockDataInsertContainer(BlockState blockState) {
		this(new UUID(0L, 0L), blockState, null);
	}
	public BlockDataInsertContainer(BlockState blockState, ItemStack[] inventory) {
		this(new UUID(0L, 0L), blockState, inventory);
	}
	public BlockDataInsertContainer(UUID actorUuid, BlockState blockState) {
		this(actorUuid, blockState, null);
	}
	public BlockDataInsertContainer(UUID actorUuid, BlockState blockState, ItemStack[] inventory) {
		this.actorUuid = actorUuid;
		this.blockState = blockState;
		this.inventory = inventory;
	}
	
	//public
	public UUID getActorUuid() {
		return actorUuid;
	}
	public BlockState getBlockState() {
		return blockState;
	}
	public ItemStack[] getInventory() {
		return inventory;
	}
	
	//private
	
}
