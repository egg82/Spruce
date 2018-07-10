package me.egg82.sprc.sql.mysql;

import java.util.UUID;
import java.util.function.BiConsumer;

import ninja.egg82.bukkit.services.ConfigRegistry;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IVariableRegistry;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class CreateTablesMySQLCommand extends Command {
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
	public CreateTablesMySQLCommand() {
		super();
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		IVariableRegistry<String> configRegistry = ServiceLocator.getService(ConfigRegistry.class);
		String database = configRegistry.getRegister("sql.mysql.database", String.class);
		String prefix = configRegistry.getRegister("sql.prefix", String.class);
		
		playerChatQuery = sql.query("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=? AND table_name=?;", database, "spruce_" + prefix + "player_chat");
		playerDataQuery = sql.query("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=? AND table_name=?;", database, "spruce_" + prefix + "player_data");
		blockDataQuery = sql.query("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=? AND table_name=?;", database, "spruce_" + prefix + "block_data");
		entityDataQuery = sql.query("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=? AND table_name=?;", database, "spruce_" + prefix + "entity_data");
	}
	private void onSQLData(SQLEventArgs e) {
		IVariableRegistry<String> configRegistry = ServiceLocator.getService(ConfigRegistry.class);
		String prefix = configRegistry.getRegister("sql.prefix", String.class);
		
		if (e.getUuid().equals(playerChatQuery)) {
			if (e.getData().data.length > 0 && e.getData().data[0].length > 0 && ((Number) e.getData().data[0][0]).intValue() != 0) {
				return;
			}
			
			sql.query(
				"CREATE TABLE `" + "spruce_" + prefix + "player_chat" + "` (" // Can't use prepared statements with things like "CREATE TABLE"
						+ "`id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
						+ "`uuid` VARCHAR(36) NOT NULL,"
						+ "`chat` TEXT NOT NULL," // "Chat" - Encoded as a String with max length of 32767. This will make lookups slower, which sucks, but oh well
						+ "`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
				+ ");"
			);
		} else if (e.getUuid().equals(playerDataQuery)) {
			if (e.getData().data.length > 0 && e.getData().data[0].length > 0 && ((Number) e.getData().data[0][0]).intValue() != 0) {
				return;
			}
			
			sql.query(
				"CREATE TABLE `" + "spruce_" + prefix + "player_data" + "` (" // Can't use prepared statements with things like "CREATE TABLE"
						+ "`id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
						+ "`uuid` VARCHAR(36) NOT NULL,"
						+ "`world` VARCHAR(55) NOT NULL,"
						+ "`x` DOUBLE NOT NULL,"
						+ "`y` DOUBLE NOT NULL,"
						+ "`z` DOUBLE NOT NULL,"
						+ "`isLogin` BOOLEAN NOT NULL,"
						+ "`isLogout` BOOLEAN NOT NULL,"
						+ "`isWorldChange` BOOLEAN NOT NULL,"
						// Not storing player inv data. No reason to. Other plugins do that already and it's far too complex when considering use-cases will be affecting an area
						+ "`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
				+ ");"
			);
		} else if (e.getUuid().equals(blockDataQuery)) {
			if (e.getData().data.length > 0 && e.getData().data[0].length > 0 && ((Number) e.getData().data[0][0]).intValue() != 0) {
				return;
			}
			
			sql.query(
				"CREATE TABLE `" + "spruce_" + prefix + "block_data" + "` (" // Can't use prepared statements with things like "CREATE TABLE"
						+ "`id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
						+ "`actorUuid` VARCHAR(36) NOT NULL," // UUID of acting player, or zeroes if system/Bukkit/plugin
						+ "`world` VARCHAR(55) NOT NULL,"
						+ "`x` INTEGER NOT NULL,"
						+ "`y` INTEGER NOT NULL,"
						+ "`z` INTEGER NOT NULL,"
						+ "`inventory` BLOB," // Not MEDIUMBLOB or LONGBLOB because 64k of data should be more than enough, especially compressed
						+ "`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
						+ "`rolledBack` BOOLEAN NOT NULL DEFAULT 0"
				+ ");"
			);
		} else if (e.getUuid().equals(entityDataQuery)) {
			if (e.getData().data.length > 0 && e.getData().data[0].length > 0 && ((Number) e.getData().data[0][0]).intValue() != 0) {
				sql.onError().detatch(sqlError);
				sql.onData().detatch(sqlError);
				return;
			}
			
			finalQuery = sql.query(
				"CREATE TABLE `" + "spruce_" + prefix + "entity_data" + "` (" // Can't use prepared statements with things like "CREATE TABLE"
						+ "`id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
						+ "`uuid` VARCHAR(36) NOT NULL," // Entity UUID, in case it never actually despawned
						+ "`actorUuid` VARCHAR(36) NOT NULL," // UUID of acting player, or zeroes if system/Bukkit/plugin
						+ "`type` VARCHAR(25) NOT NULL,"
						+ "`world` VARCHAR(55) NOT NULL,"
						+ "`x` DOUBLE NOT NULL,"
						+ "`y` DOUBLE NOT NULL,"
						+ "`z` DOUBLE NOT NULL,"
						+ "`isSpawn` BOOLEAN NOT NULL,"
						+ "`isDeath` BOOLEAN NOT NULL,"
						+ "`isWorldChange` BOOLEAN NOT NULL,"
						// isSpawn, isDeath, and isWorldChange can all be false if the only difference is an inv change
						+ "`inventory` BLOB," // Not MEDIUMBLOB or LONGBLOB because 64k of data should be more than enough, especially compressed
						+ "`time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
						+ "`rolledBack` BOOLEAN NOT NULL DEFAULT 0"
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
