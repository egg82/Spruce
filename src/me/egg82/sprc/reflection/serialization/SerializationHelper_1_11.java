package me.egg82.sprc.reflection.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Comparator;
import org.bukkit.block.DaylightDetector;
import org.bukkit.block.EnchantingTable;
import org.bukkit.block.EnderChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;

public class SerializationHelper_1_11 implements ISerializationHelper {
	//vars
	private SerializationHelper_1_10 down = new SerializationHelper_1_10();
	
	//constructor
	public SerializationHelper_1_11() {
		
	}
	
	//public
	public void fromCompressedBytes(Location loc, Material type, byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("data cannot be null.");
		}
		
		loc.getBlock().setType(type, true);
		BlockState newState = loc.getBlock().getState();
		
		try (ByteArrayInputStream stream = new ByteArrayInputStream(data); GZIPInputStream gzip = new GZIPInputStream(stream); BukkitObjectInputStream in = new BukkitObjectInputStream(gzip)) {
			fromCompressedBytes(newState, in);
		} catch (Exception ex) {
			ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
			return;
		}
	}
	@SuppressWarnings("deprecation")
	public void fromCompressedBytes(BlockState newState, BukkitObjectInputStream in) throws IOException, ClassNotFoundException {
		if (newState instanceof Comparator) {
			// Do nothing
		} else if (newState instanceof DaylightDetector) {
			// Do nothing
		} else if (newState instanceof EnchantingTable) {
			EnchantingTable enchantingTable = (EnchantingTable) newState;
			enchantingTable.setCustomName(in.readUTF());
		} else if (newState instanceof EnderChest) {
			// Do nothing
		} else if (newState instanceof ShulkerBox) {
			ShulkerBox shulkerBox = (ShulkerBox) newState;
			shulkerBox.setData(new MaterialData(newState.getType(), DyeColor.getByColor((Color) in.readObject()).getDyeData())); // Only way to do this is by using deprecated functions
			shulkerBox.setCustomName(in.readUTF());
			boolean isLocked = in.readBoolean();
			if (isLocked) {
				shulkerBox.setLock(in.readUTF());
			}
			
			List<ItemStack> items = new ArrayList<ItemStack>();
			for (int i = in.readInt(); i > 0; i--) {
				items.add((ItemStack) in.readObject());
			}
			shulkerBox.getInventory().setContents(items.toArray(new ItemStack[0]));
		} else {
			down.fromCompressedBytes(newState, in);
		}
	}
	public byte[] toCompressedBytes(BlockState state) {
		if (state == null) {
			throw new IllegalArgumentException("state cannot be null.");
		}
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(stream); BukkitObjectOutputStream out = new BukkitObjectOutputStream(gzip)) {
			toCompressedBytes(state, out);
		} catch (Exception ex) {
			ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
			return null;
		}
		return stream.toByteArray();
	}
	public void toCompressedBytes(BlockState state, BukkitObjectOutputStream out) throws IOException {
		if (state instanceof Comparator) {
			// Do nothing
		} else if (state instanceof DaylightDetector) {
			// Do nothing
		} else if (state instanceof EnchantingTable) {
			EnchantingTable enchantingTable = (EnchantingTable) state;
			out.writeUTF(enchantingTable.getCustomName());
		} else if (state instanceof EnderChest) {
			// Do nothing
		} else if (state instanceof ShulkerBox) {
			ShulkerBox shulkerBox = (ShulkerBox) state;
			out.writeObject(shulkerBox.getColor().getColor());
			out.writeUTF(shulkerBox.getCustomName());
			out.writeBoolean(shulkerBox.isLocked());
			if (shulkerBox.isLocked()) {
				out.writeUTF(shulkerBox.getLock());
			}
			
			ItemStack[] items = shulkerBox.getInventory().getContents();
			out.writeInt(items.length);
			for (ItemStack i : items) {
				out.writeObject(i);
			}
		} else {
			down.toCompressedBytes(state, out);
		}
	}
	
	//private
	
}
