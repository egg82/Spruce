package me.egg82.sprc.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;

public class ItemStackUtil {
	//vars
	private static final Encoder encoder = Base64.getEncoder();
	private static final Decoder decoder = Base64.getDecoder();
	
	//constructor
	public ItemStackUtil() {
		
	}
	
	//public
	public static ItemStack[] fromBase64(String encoded) {
		if (encoded == null) {
			throw new IllegalArgumentException("encoded cannot be null.");
		}
		
		byte[] bytes = null;
		try {
			bytes = decoder.decode(encoded);
		} catch (Exception ex) {
			ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
			return null;
		}
		
		return fromCompressedBytes(bytes);
	}
	public static String toBase64(ItemStack[] items) {
		if (items == null) {
			throw new IllegalArgumentException("items cannot be null.");
		}
		
		return encoder.encodeToString(toCompressedBytes(items));
	}
	
	public static ItemStack[] fromCompressedBytes(byte[] bytes) {
		if (bytes == null) {
			throw new IllegalArgumentException("bytes cannot be null.");
		}
		
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		List<ItemStack> retVal = new ArrayList<ItemStack>();
		try (GZIPInputStream gzip = new GZIPInputStream(stream); BukkitObjectInputStream in = new BukkitObjectInputStream(gzip)) {
			if (in.available() == 0) {
				return new ItemStack[0];
			}
			
			while (in.available() > 0) {
				retVal.add((ItemStack) in.readObject());
			}
		} catch (Exception ex) {
			ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
			return null;
		}
		return retVal.toArray(new ItemStack[0]);
	}
	public static byte[] toCompressedBytes(ItemStack[] items) {
		if (items == null) {
			throw new IllegalArgumentException("items cannot be null.");
		}
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try (GZIPOutputStream gzip = new GZIPOutputStream(stream); BukkitObjectOutputStream out = new BukkitObjectOutputStream(gzip)) {
			for (ItemStack i : items) {
				out.writeObject(i);
			}
		} catch (Exception ex) {
			ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
			return null;
		}
		return stream.toByteArray();
	}
	
	//private
	
}
