package me.egg82.sprc.sql.sqlite;

import java.util.UUID;
import java.util.function.BiConsumer;

import ninja.egg82.bukkit.services.ConfigRegistry;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IVariableRegistry;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class CreateTablesSQLiteCommand extends Command {
	//vars
	private ISQL sql = ServiceLocator.getService(ISQL.class);
	
	private UUID playerChatQuery = null;
	private UUID playerDataQuery = null;
	private UUID blockDataQuery = null;
	private UUID entityDataQuery = null;
	
	private UUID finalQuery = null;
	
	private BiConsumer<Object, SQLEventArgs> sqlError = (s, e) -> onSQLError(e);
	private BiConsumer<Object, SQLEventArgs> sqlData = (s, e) -> onSQLData(e);
	
	//constructor
	public CreateTablesSQLiteCommand() {
		super();
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		IVariableRegistry<String> configRegistry = ServiceLocator.getService(ConfigRegistry.class);
		String prefix = configRegistry.getRegister("sql.prefix", String.class);
		
		playerChatQuery = sql.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;", "spruce_" + prefix + "player_chat");
		playerDataQuery = sql.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;", "spruce_" + prefix + "player_data");
		blockDataQuery = sql.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;", "spruce_" + prefix + "block_data");
		entityDataQuery = sql.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;", "spruce_" + prefix + "entity_data");
	}
	
	private void onSQLData(SQLEventArgs e) {
		if (e.getUuid().equals(query)) {
			if (e.getData().data.length > 0 && e.getData().data[0].length > 0 && ((Number) e.getData().data[0][0]).intValue() != 0) {
				sql.onError().detatch(sqlError);
				sql.onData().detatch(sqlError);
				return;
			}
			
			finalQuery = sql.query(
				"CREATE TABLE `antivpn` ("
						+ "`ip` TEXT(45) NOT NULL,"
						+ "`value` INTEGER(1) NOT NULL,"
						+ "`created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
						+ "UNIQUE(`ip`)"
				+ ");"
			);
		} else if (e.getUuid().equals(finalQuery)) {
			sql.onError().detatch(sqlError);
			sql.onData().detatch(sqlError);
		}
	}
	private void onSQLError(SQLEventArgs e) {
		if (!e.getUuid().equals(playerChatQuery) && !e.getUuid().equals(playerDataQuery) && !e.getUuid().equals(blockDataQuery) && !e.getUuid().equals(entityDataQuery) && !e.getUuid().equals(finalQuery)) {
			return;
		}
		
		ServiceLocator.getService(IExceptionHandler.class).silentException(e.getSQLError().ex);
		// Wrap in a new exception and print to console. We wrap so we know where the error actually comes from
		new Exception(e.getSQLError().ex).printStackTrace();
		
		if (e.getUuid().equals(entityDataQuery) || e.getUuid().equals(finalQuery)) {
			sql.onError().detatch(sqlError);
			sql.onData().detatch(sqlError);
		}
		
		throw new RuntimeException(e.getSQLError().ex);
	}
}
