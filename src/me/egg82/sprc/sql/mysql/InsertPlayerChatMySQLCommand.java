package me.egg82.sprc.sql.mysql;

import java.util.UUID;
import java.util.function.BiConsumer;

import me.egg82.sprc.Config;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class InsertPlayerChatMySQLCommand extends Command {
	//vars
	private ISQL sql = ServiceLocator.getService(ISQL.class);
	
	private UUID query = null;
	
	private UUID playerUuid = null;
	private String chat = null;
	
	private BiConsumer<Object, SQLEventArgs> sqlError = (s, e) -> onSQLError(e);
	private BiConsumer<Object, SQLEventArgs> sqlData = (s, e) -> onSQLData(e);
	
	//constructor
	public InsertPlayerChatMySQLCommand(UUID playerUuid, String chat) {
		super();
		
		this.playerUuid = playerUuid;
		this.chat = chat;
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		query = sql.query("INSERT INTO `spruce_ " + Config.prefix + "player_chat` (`uuid`, `chat`) VALUES(?, ?);", playerUuid.toString(), chat);
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
