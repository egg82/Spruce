package me.egg82.sprc.sql.mysql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.zip.Deflater;

import org.bukkit.Location;

import me.egg82.sprc.Config;
import me.egg82.sprc.core.BlockDataInsertContainer;
import ninja.egg82.bukkit.reflection.block.serialization.ISerializationHelper;
import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class InsertBlockDataMySQLCommand extends Command {
	//vars
	private ISQL sql = ServiceLocator.getService(ISQL.class);
	
	private UUID query = null;
	
	private Collection<BlockDataInsertContainer> data = null;
	
	private BiConsumer<Object, SQLEventArgs> sqlError = (s, e) -> onSQLError(e);
	private BiConsumer<Object, SQLEventArgs> sqlData = (s, e) -> onSQLData(e);
	
	private ISerializationHelper serializationHelper = ServiceLocator.getService(ISerializationHelper.class);
	
	//constructor
	public InsertBlockDataMySQLCommand(Collection<BlockDataInsertContainer> data) {
		super();
		
		this.data = data;
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		query = sql.query("INSERT INTO `spruce_" + Config.prefix + "block_data` (`actorUuid`, `type`, `blockData`, `world`, `x`, `y`, `z`, `compressedData`) VALUES " + getValues() + ";", getData());
	}
	private void onSQLData(SQLEventArgs e) {
		if (e.getUuid().equals(query)) {
			sql.onError().detatch(sqlError);
			sql.onData().detatch(sqlError);
			
			onComplete().invoke(this, CompleteEventArgs.EMPTY);
		}
	}
	private void onSQLError(SQLEventArgs e) {
		if (!e.getUuid().equals(query)) {
			return;
		}
		
		ServiceLocator.getService(IExceptionHandler.class).silentException(e.getSQLError().ex);
		// Wrap in a new exception and print to console. We wrap so we know where the error actually comes from
		new Exception(e.getSQLError().ex).printStackTrace();
		
		sql.onError().detatch(sqlError);
		sql.onData().detatch(sqlError);
		
		onComplete().invoke(this, CompleteEventArgs.EMPTY);
		
		throw new RuntimeException(e.getSQLError().ex);
	}
	
	private String getValues() {
		String retVal = "";
		
		for (int i = 0; i < data.size(); i++) {
			retVal += "(?, ?, ?, ?, ?, ?, ?, ?), ";
		}
		retVal = retVal.substring(0, retVal.length() - 2);
		
		return retVal;
	}
	@SuppressWarnings("deprecation")
	private Object[] getData() {
		List<Object> retVal = new ArrayList<Object>();
		
		for (BlockDataInsertContainer d : data) {
			Location location = d.getBlockState().getLocation();
			retVal.add(d.getActorUuid().toString());
			retVal.add(d.getBlockState().getType().name());
			retVal.add(Byte.valueOf(d.getBlockState().getRawData()));
			retVal.add(location.getWorld().getName());
			retVal.add(Integer.valueOf(location.getBlockX()));
			retVal.add(Integer.valueOf(location.getBlockY()));
			retVal.add(Integer.valueOf(location.getBlockZ()));
			retVal.add(serializationHelper.toCompressedBytes(d.getBlockState(), d.getInventory(), Deflater.BEST_COMPRESSION));
		}
		
		return retVal.toArray();
	}
}
