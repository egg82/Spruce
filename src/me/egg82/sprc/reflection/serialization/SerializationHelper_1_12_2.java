package me.egg82.sprc.reflection.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.Structure;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.block.structure.UsageMode;
import org.bukkit.util.BlockVector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;

public class SerializationHelper_1_12_2 implements ISerializationHelper {
	//vars
	private SerializationHelper_1_12 down = new SerializationHelper_1_12();
	
	//constructor
	public SerializationHelper_1_12_2() {
		
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
		if (newState instanceof Structure) {
			Structure structure = (Structure) newState;
			structure.setAuthor(in.readUTF());
			structure.setIntegrity(in.readFloat());
			structure.setMetadata(in.readUTF());
			structure.setMirror(Mirror.valueOf(in.readUTF()));
			structure.setRelativePosition((BlockVector) in.readObject());
			structure.setRotation(StructureRotation.valueOf(in.readUTF()));
			structure.setSeed(in.readLong());
			structure.setStructureName(in.readUTF());
			structure.setStructureSize((BlockVector) in.readObject());
			structure.setUsageMode(UsageMode.valueOf(in.readUTF()));
			structure.setBoundingBoxVisible(in.readBoolean());
			structure.setIgnoreEntities(in.readBoolean());
			structure.setShowAir(in.readBoolean());
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
		if (state instanceof Structure) {
			Structure structure = (Structure) state;
			out.writeUTF(structure.getAuthor());
			out.writeFloat(structure.getIntegrity());
			out.writeUTF(structure.getMetadata());
			out.writeUTF(structure.getMirror().name());
			out.writeObject(structure.getRelativePosition());
			out.writeUTF(structure.getRotation().name());
			out.writeLong(structure.getSeed());
			out.writeUTF(structure.getStructureName());
			out.writeObject(structure.getStructureSize());
			out.writeUTF(structure.getUsageMode().name());
			out.writeBoolean(structure.isBoundingBoxVisible());
			out.writeBoolean(structure.isIgnoreEntities());
			out.writeBoolean(structure.isShowAir());
		} else {
			down.toCompressedBytes(state, out);
		}
	}
	
	//private
	
}
