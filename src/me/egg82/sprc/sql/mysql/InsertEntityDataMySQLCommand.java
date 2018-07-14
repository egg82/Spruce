package me.egg82.sprc.sql.mysql;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

import me.egg82.sprc.Config;
import me.egg82.sprc.enums.EntityDataType;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class InsertEntityDataMySQLCommand extends Command {
	//vars
	private ISQL sql = ServiceLocator.getService(ISQL.class);
	
	private UUID query = null;
	
	private UUID entityUuid = null;
	private UUID actorUuid = null;
	private String type = null;
	private String world = null;
	private Double x = null;
	private Double y = null;
	private Double z = null;
	private Boolean isSpawn = null;
	private Boolean isDeath = null;
	private Boolean isWorldChange = null;
	private byte[] inventory = null;
	
	private BiConsumer<Object, SQLEventArgs> sqlError = (s, e) -> onSQLError(e);
	private BiConsumer<Object, SQLEventArgs> sqlData = (s, e) -> onSQLData(e);
	
	//constructor
	public InsertEntityDataMySQLCommand(UUID entityUuid, EntityType type, Location entityLocation, EntityDataType dataType) {
		this(entityUuid, new UUID(0L, 0L), type, entityLocation, dataType, null);
	}
	public InsertEntityDataMySQLCommand(UUID entityUuid, EntityType type, Location entityLocation, EntityDataType dataType, byte[] inventory) {
		this(entityUuid, new UUID(0L, 0L), type, entityLocation, dataType, inventory);
	}
	public InsertEntityDataMySQLCommand(UUID entityUuid, UUID actorUuid, EntityType type, Location entityLocation, EntityDataType dataType) {
		this(entityUuid, actorUuid, type, entityLocation, dataType, null);
	}
	public InsertEntityDataMySQLCommand(UUID entityUuid, UUID actorUuid, EntityType type, Location entityLocation, EntityDataType dataType, byte[] inventory) {
		super();
		
		this.entityUuid = entityUuid;
		this.actorUuid = actorUuid;
		this.type = type.name();
		this.world = entityLocation.getWorld().getName();
		this.x = Double.valueOf(entityLocation.getX());
		this.y = Double.valueOf(entityLocation.getY());
		this.z = Double.valueOf(entityLocation.getZ());
		this.isSpawn = (dataType == EntityDataType.SPAWN) ? Boolean.TRUE : Boolean.FALSE;
		this.isDeath = (dataType == EntityDataType.DEATH) ? Boolean.TRUE : Boolean.FALSE;
		this.isWorldChange = (dataType == EntityDataType.WORLD_CHANGE) ? Boolean.TRUE : Boolean.FALSE;
		this.inventory = inventory;
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		query = sql.query("INSERT INTO `spruce_ " + Config.prefix + "player_data` (`uuid`, `actorUuid`, `type`, `world`, `x`, `y`, `z`, `isSpawn`, `isDeath`, `isWorldChange`, `inventory`) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", entityUuid.toString(), actorUuid.toString(), type, world, x, y, z, isSpawn, isDeath, isWorldChange, inventory);
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
