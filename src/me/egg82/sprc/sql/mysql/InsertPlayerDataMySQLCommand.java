package me.egg82.sprc.sql.mysql;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.Location;

import me.egg82.sprc.Config;
import me.egg82.sprc.enums.PlayerDataType;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class InsertPlayerDataMySQLCommand extends Command {
	//vars
	private ISQL sql = ServiceLocator.getService(ISQL.class);
	
	private UUID query = null;
	
	private UUID playerUuid = null;
	private String world = null;
	private Double x = null;
	private Double y = null;
	private Double z = null;
	private Boolean isLogin = null;
	private Boolean isLogout = null;
	private Boolean isWorldChange = null;
	
	private BiConsumer<Object, SQLEventArgs> sqlError = (s, e) -> onSQLError(e);
	private BiConsumer<Object, SQLEventArgs> sqlData = (s, e) -> onSQLData(e);
	
	//constructor
	public InsertPlayerDataMySQLCommand(UUID playerUuid, Location playerLocation, PlayerDataType dataType) {
		super();
		
		this.playerUuid = playerUuid;
		this.world = playerLocation.getWorld().getName();
		this.x = Double.valueOf(playerLocation.getX());
		this.y = Double.valueOf(playerLocation.getY());
		this.z = Double.valueOf(playerLocation.getZ());
		this.isLogin = (dataType == PlayerDataType.LOGIN) ? Boolean.TRUE : Boolean.FALSE;
		this.isLogout = (dataType == PlayerDataType.LOGOUT) ? Boolean.TRUE : Boolean.FALSE;
		this.isWorldChange = (dataType == PlayerDataType.WORLD_CHANGE) ? Boolean.TRUE : Boolean.FALSE;
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		query = sql.query("INSERT INTO `spruce_ " + Config.prefix + "player_data` (`uuid`, `world`, `x`, `y`, `z`, `isLogin`, `isLogout`, `isWorldChange`) VALUES(?, ?, ?, ?, ?, ?, ?, ?);", playerUuid.toString(), world, x, y, z, isLogin, isLogout, isWorldChange);
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
