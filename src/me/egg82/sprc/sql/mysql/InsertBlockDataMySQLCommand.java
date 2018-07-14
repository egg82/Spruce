package me.egg82.sprc.sql.mysql;

import java.util.UUID;
import java.util.function.BiConsumer;

import me.egg82.sprc.Config;
import ninja.egg82.bukkit.core.BlockData;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class InsertBlockDataMySQLCommand extends Command {
	//vars
	private ISQL sql = ServiceLocator.getService(ISQL.class);
	
	private UUID query = null;
	
	private UUID actorUuid = null;
	private String type = null;
	private Byte blockData = null;
	private String world = null;
	private Integer x = null;
	private Integer y = null;
	private Integer z = null;
	private byte[] compressedData = null;
	
	private BiConsumer<Object, SQLEventArgs> sqlError = (s, e) -> onSQLError(e);
	private BiConsumer<Object, SQLEventArgs> sqlData = (s, e) -> onSQLData(e);
	
	//constructor
	public InsertBlockDataMySQLCommand(BlockData oldData) {
		this(new UUID(0L, 0L), oldData);
	}
	public InsertBlockDataMySQLCommand(UUID actorUuid, BlockData oldData) {
		super();
		
		this.actorUuid = actorUuid;
		this.type = oldData.getType().name();
		this.blockData = Byte.valueOf(oldData.getBlockData());
		this.world = oldData.getLocation().getWorld().getName();
		this.x = Integer.valueOf(oldData.getLocation().getBlockX());
		this.y = Integer.valueOf(oldData.getLocation().getBlockY());
		this.z = Integer.valueOf(oldData.getLocation().getBlockZ());
		this.compressedData = oldData.getCompressedData();
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		query = sql.query("INSERT INTO `spruce_ " + Config.prefix + "block_data` (`actorUuid`, `type`, `blockData`, `world`, `x`, `y`, `z`, `compressedData`) VALUES(?, ?, ?, ?, ?, ?, ?, ?);", actorUuid.toString(), type, blockData, world, x, y, z, compressedData);
	}
	private void onSQLData(SQLEventArgs e) {
		if (e.getUuid().equals(query)) {
			sql.onError().detatch(sqlError);
			sql.onData().detatch(sqlError);
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
		
		throw new RuntimeException(e.getSQLError().ex);
	}
}
