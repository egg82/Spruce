package me.egg82.sprc.core;

import java.util.UUID;

import org.bukkit.block.BlockState;

public class BlockDataInsertContainer {
	//vars
	private UUID actorUuid = null;
	private BlockState blockState = null;
	
	//constructor
	public BlockDataInsertContainer(BlockState blockState) {
		this(new UUID(0L, 0L), blockState);
	}
	public BlockDataInsertContainer(UUID actorUuid, BlockState blockState) {
		this.actorUuid = actorUuid;
		this.blockState = blockState;
	}
	
	//public
	public UUID getActorUuid() {
		return actorUuid;
	}
	public BlockState getBlockState() {
		return blockState;
	}
	
	//private
	
}
