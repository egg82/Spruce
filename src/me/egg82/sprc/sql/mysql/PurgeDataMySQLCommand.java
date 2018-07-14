package me.egg82.sprc.sql.mysql;

import java.util.UUID;
import java.util.function.BiConsumer;

import me.egg82.sprc.Config;
import ninja.egg82.enums.BaseSQLType;
import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class PurgeDataMySQLCommand extends Command {
	//vars
	private ISQL sql = ServiceLocator.getService(ISQL.class);
	
	private UUID playerChatQuery = null;
	private boolean playerChatComplete = false;
	private UUID playerDataQuery = null;
	private boolean playerDataComplete = false;
	private UUID blockDataQuery = null;
	private boolean blockDataComplete = false;
	private UUID entityDataQuery = null;
	private boolean entityDataComplete = false;
	
	private BiConsumer<Object, SQLEventArgs> sqlError = (s, e) -> onSQLError(e);
	private BiConsumer<Object, SQLEventArgs> sqlData = (s, e) -> onSQLData(e);
	
	//constructor
	public PurgeDataMySQLCommand() {
		super();
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		if (sql.getType() == BaseSQLType.SQLite) {
			sql.onError().detatch(sqlError);
			sql.onData().detatch(sqlError);
			
			onComplete().invoke(this, CompleteEventArgs.EMPTY);
			return;
		}
		
		playerChatQuery = sql.parallelQuery("DELETE FROM `spruce_" + Config.prefix + "player_chat` WHERE CURRENT_TIMESTAMP() > `time` + INTERVAL ? * 0.001 SECOND;", Long.valueOf(Config.purgePlayer));
		playerDataQuery = sql.parallelQuery("DELETE FROM `spruce_" + Config.prefix + "player_data` WHERE CURRENT_TIMESTAMP() > `time` + INTERVAL ? * 0.001 SECOND;", Long.valueOf(Config.purgePlayer));
		blockDataQuery = sql.parallelQuery("DELETE FROM `spruce_" + Config.prefix + "block_data` WHERE CURRENT_TIMESTAMP() > `time` + INTERVAL ? * 0.001 SECOND;", Long.valueOf(Config.purgeWorld));
		entityDataQuery = sql.parallelQuery("DELETE FROM `spruce_" + Config.prefix + "entity_data` WHERE CURRENT_TIMESTAMP() > `time` + INTERVAL ? * 0.001 SECOND;", Long.valueOf(Config.purgeWorld));
	}
	private void onSQLData(SQLEventArgs e) {
		if (e.getUuid().equals(playerChatQuery)) {
			playerChatComplete = true;
			checkComplete();
		} else if (e.getUuid().equals(playerDataQuery)) {
			playerDataComplete = true;
			checkComplete();
		} else if (e.getUuid().equals(blockDataQuery)) {
			blockDataComplete = true;
			checkComplete();
		} else if (e.getUuid().equals(entityDataQuery)) {
			entityDataComplete = true;
			checkComplete();
		}
	}
	private void onSQLError(SQLEventArgs e) {
		if (e.getUuid().equals(playerChatQuery)) {
			playerChatComplete = true;
		} else if (e.getUuid().equals(playerDataQuery)) {
			playerDataComplete = true;
		} else if (e.getUuid().equals(blockDataQuery)) {
			blockDataComplete = true;
		} else if (e.getUuid().equals(entityDataQuery)) {
			entityDataComplete = true;
		} else {
			return;
		}
		
		ServiceLocator.getService(IExceptionHandler.class).silentException(e.getSQLError().ex);
		// Wrap in a new exception and print to console. We wrap so we know where the error actually comes from
		new Exception(e.getSQLError().ex).printStackTrace();
		
		checkComplete();
		
		throw new RuntimeException(e.getSQLError().ex);
	}
	
	private void checkComplete() {
		if (!playerChatComplete || !playerDataComplete || !blockDataComplete || !entityDataComplete) {
			return;
		}
		
		sql.onError().detatch(sqlError);
		sql.onData().detatch(sqlError);
		
		onComplete().invoke(this, CompleteEventArgs.EMPTY);
	}
}
