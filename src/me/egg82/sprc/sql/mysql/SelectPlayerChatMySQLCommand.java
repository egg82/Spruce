package me.egg82.sprc.sql.mysql;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import me.egg82.sprc.Config;
import me.egg82.sprc.core.PlayerChatSelectContainer;
import me.egg82.sprc.core.PlayerChatResultEventArgs;
import ninja.egg82.events.SQLEventArgs;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.events.EventHandler;
import ninja.egg82.patterns.Command;
import ninja.egg82.sql.ISQL;

public class SelectPlayerChatMySQLCommand extends Command {
	//vars
	private ISQL sql = ServiceLocator.getService(ISQL.class);
	
	private UUID query = null;
	
	private UUID playerUuid = null;
	private Long time = null;
	
	private BiConsumer<Object, SQLEventArgs> sqlError = (s, e) -> onSQLError(e);
	private BiConsumer<Object, SQLEventArgs> sqlData = (s, e) -> onSQLData(e);
	
	private EventHandler<PlayerChatResultEventArgs> onData = new EventHandler<PlayerChatResultEventArgs>();
	
	//constructor
	public SelectPlayerChatMySQLCommand(UUID playerUuid, long time) {
		super();
		
		this.playerUuid = playerUuid;
		this.time = Long.valueOf(time);
		
		sql.onError().attach(sqlError);
		sql.onData().attach(sqlData);
	}
	
	//public
	public EventHandler<PlayerChatResultEventArgs> onData() {
		return onData;
	}
	
	//private
	protected void onExecute(long elapsedMilliseconds) {
		query = sql.parallelQuery("SELECT `chat`, `time` FROM `spruce_" + Config.prefix + "player_chat` WHERE `uuid`=? AND CURRENT_TIMESTAMP() <= `time` + INTERVAL ? * 0.001 SECOND;", playerUuid.toString(), time);
	}
	
	private void onSQLData(SQLEventArgs e) {
		if (e.getUuid().equals(query)) {
			Exception lastEx = null;
			
			List<PlayerChatSelectContainer> retVal = new ArrayList<PlayerChatSelectContainer>();
			// Iterate rows
			for (Object[] o : e.getData().data) {
				try {
					// Grab all data and convert to more useful object types
					String chat = (String) o[0];
					long time = ((Timestamp) o[1]).getTime();
					
					// Add new data
					retVal.add(new PlayerChatSelectContainer(chat, time));
				} catch (Exception ex) {
					ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
					ex.printStackTrace();
					lastEx = ex;
				}
			}
			
			sql.onError().detatch(sqlError);
			sql.onData().detatch(sqlError);
			
			onData.invoke(this, new PlayerChatResultEventArgs(playerUuid, retVal));
			
			if (lastEx != null) {
				throw new RuntimeException(lastEx);
			}
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
		
		onData.invoke(this, PlayerChatResultEventArgs.EMPTY);
		
		throw new RuntimeException(e.getSQLError().ex);
	}
}
