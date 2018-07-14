package me.egg82.sprc.sql.sqlite;

import java.util.UUID;
import java.util.function.BiConsumer;

import me.egg82.sprc.Config;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
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
		playerChatQuery = sql.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;", "spruce_" + Config.prefix + "player_chat");
		playerDataQuery = sql.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;", "spruce_" + Config.prefix + "player_data");
		blockDataQuery = sql.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;", "spruce_" + Config.prefix + "block_data");
		entityDataQuery = sql.query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?;", "spruce_" + Config.prefix + "entity_data");
	}
	
	private void onSQLData(SQLEventArgs e) {
		if (e.getUuid().equals(playerChatQuery)) {
			if (e.getData().data.length > 0 && e.getData().data[0].length > 0 && ((Number) e.getData().data[0][0]).intValue() != 0) {
				return;
			}
			
			sql.query(
				"CREATE TABLE `spruce_" + Config.prefix + "player_chat` ("
						+ "`uuid` TEXT(36) NOT NULL,"
						+ "`chat` TEXT(65535) NOT NULL," // "Chat" - Encoded as a String with max length of 32767
						+ "`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
				+ ");"
			);
		} else if (e.getUuid().equals(playerDataQuery)) {
			if (e.getData().data.length > 0 && e.getData().data[0].length > 0 && ((Number) e.getData().data[0][0]).intValue() != 0) {
				return;
			}
			
			sql.query(
				"CREATE TABLE `spruce_" + Config.prefix + "player_data` ("
						+ "`uuid` TEXT(36) NOT NULL,"
						+ "`world` TEXT(55) NOT NULL,"
						+ "`x` REAL NOT NULL,"
						+ "`y` REAL NOT NULL,"
						+ "`z` REAL NOT NULL,"
						+ "`isLogin` INTEGER(1) NOT NULL,"
						+ "`isLogout` INTEGER(1) NOT NULL,"
						+ "`isWorldChange` INTEGER(1) NOT NULL,"
						// Not storing player inv data. No reason to. Other plugins do that already and it's far too complex when considering use-cases will be affecting an area
						+ "`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
				+ ");"
			);
		} else if (e.getUuid().equals(blockDataQuery)) {
			if (e.getData().data.length > 0 && e.getData().data[0].length > 0 && ((Number) e.getData().data[0][0]).intValue() != 0) {
				return;
			}
			
			finalQuery = sql.query(
				"CREATE TABLE `spruce_" + Config.prefix + "block_data` ("
						+ "`actorUuid` TEXT(36) NOT NULL,"
						+ "`type` TEXT(25) NOT NULL,"
						+ "`blockData` INTEGER(1) NOT NULL,"
						+ "`world` TEXT(55) NOT NULL,"
						+ "`x` INTEGER NOT NULL,"
						+ "`y` INTEGER NOT NULL,"
						+ "`z` INTEGER NOT NULL,"
						+ "`compressedData` BLOB,"
						+ "`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
						+ "`rolledBack` INTEGER(1) NOT NULL DEFAULT 0"
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
