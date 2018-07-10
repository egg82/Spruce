package me.egg82.sprc.reflection.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.EndGateway;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;

public class SerializationHelper_1_9 implements ISerializationHelper {
	//vars
	private SerializationHelper_1_8 down = new SerializationHelper_1_8();
	
	//constructor
	public SerializationHelper_1_9() {
		
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
	public void fromCompressedBytes(BlockState newState, BukkitObjectInputStream in) throws IOException, ClassNotFoundException {
		if (newState instanceof EndGateway) {
			EndGateway endGateway = (EndGateway) newState;
			endGateway.setExitLocation((Location) in.readObject());
			endGateway.setExactTeleport(in.readBoolean());
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
		if (state instanceof EndGateway) {
			EndGateway endGateway = (EndGateway) state;
			out.writeObject(endGateway.getExitLocation());
			out.writeBoolean(endGateway.isExactTeleport());
		} else {
			down.toCompressedBytes(state, out);
		}
	}
	
	//private
	
}
