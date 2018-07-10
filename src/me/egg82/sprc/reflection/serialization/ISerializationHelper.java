package me.egg82.sprc.reflection.serialization;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

public interface ISerializationHelper {
	//functions
	void fromCompressedBytes(Location loc, Material type, byte[] data);
	byte[] toCompressedBytes(BlockState state);
}
