package me.egg82.sprc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import ninja.egg82.bukkit.BasePlugin;
import ninja.egg82.bukkit.processors.CommandProcessor;
import ninja.egg82.bukkit.processors.EventProcessor;
import ninja.egg82.bukkit.services.ConfigRegistry;
import ninja.egg82.bukkit.utils.VersionUtil;
import ninja.egg82.bukkit.utils.YamlUtil;
import ninja.egg82.enums.BaseSQLType;
import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.exceptionHandlers.GameAnalyticsExceptionHandler;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.exceptionHandlers.RollbarExceptionHandler;
import ninja.egg82.exceptionHandlers.builders.GameAnalyticsBuilder;
import ninja.egg82.exceptionHandlers.builders.RollbarBuilder;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.messaging.IMessageHandler;
import ninja.egg82.plugin.utils.PluginReflectUtil;
import ninja.egg82.sql.ISQL;
import ninja.egg82.utils.FileUtil;
import ninja.egg82.utils.ThreadUtil;

public class Spruce extends BasePlugin {
	//vars
	private int numCommands = 0;
	private int numEvents = 0;
	private int numTicks = 0;
	
	private IExceptionHandler exceptionHandler = null;
	private String version = getDescription().getVersion();
	
	//constructor
	public Spruce() {
		super();
		
		getLogger().setLevel(Level.WARNING);
		IExceptionHandler oldExceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
		ServiceLocator.removeServices(IExceptionHandler.class);
		
		ServiceLocator.provideService(RollbarExceptionHandler.class, false);
		exceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
		oldExceptionHandler.disconnect();
		exceptionHandler.connect(new RollbarBuilder("6720974ccccc42aaabcb8689cc9a415e", "production", version, getServerId()), "Spruce");
		exceptionHandler.setUnsentExceptions(oldExceptionHandler.getUnsentExceptions());
		exceptionHandler.setUnsentLogs(oldExceptionHandler.getUnsentLogs());
	}
	
	//public
	public void onLoad() {
		super.onLoad();
		
		PluginReflectUtil.addServicesFromPackage("me.egg82.sprc.registries", true);
		PluginReflectUtil.addServicesFromPackage("me.egg82.sprc.lists", true);
		
		ServiceLocator.getService(ConfigRegistry.class).load(YamlUtil.getOrLoadDefaults(getDataFolder().getAbsolutePath() + FileUtil.DIRECTORY_SEPARATOR_CHAR + "config.yml", "config.yml", true));
		Config.prefix = ServiceLocator.getService(ConfigRegistry.class).getRegister("sql.prefix", String.class);
	}
	
	public void onEnable() {
		super.onEnable();
		
		List<IMessageHandler> services = ServiceLocator.removeServices(IMessageHandler.class);
		for (IMessageHandler handler : services) {
			try {
				handler.close();
			} catch (Exception ex) {
				
			}
		}
		
		Loaders.loadStorage();
		
		numCommands = ServiceLocator.getService(CommandProcessor.class).addHandlersFromPackage("me.egg82.sprc.commands", PluginReflectUtil.getCommandMapFromPackage("me.egg82.sprc.commands", false, null, "Command"), false);
		numEvents = ServiceLocator.getService(EventProcessor.class).addHandlersFromPackage("me.egg82.sprc.events");
		numTicks = PluginReflectUtil.addServicesFromPackage("me.egg82.sprc.ticks", false);
		
		enableMessage();
		
		ThreadUtil.rename(getName());
		ThreadUtil.schedule(checkExceptionLimitReached, 60L * 60L * 1000L);
		ThreadUtil.schedule(onPurgeDataThread, 60L * 1000L);
	}
	public void onDisable() {
		super.onDisable();
		
		ThreadUtil.shutdown(1000L);
		
		List<ISQL> sqls = ServiceLocator.removeServices(ISQL.class);
		for (ISQL sql : sqls) {
			sql.disconnect();
		}
		
		ServiceLocator.getService(CommandProcessor.class).clear();
		ServiceLocator.getService(EventProcessor.class).clear();
		
		disableMessage();
	}
	
	//private
	private Runnable onPurgeDataThread = new Runnable() {
		public void run() {
			CountDownLatch latch = new CountDownLatch(1);
			
			BiConsumer<Object, CompleteEventArgs<?>> complete = (s, e) -> {
				ISQL sql = ServiceLocator.getService(ISQL.class);
				if (sql.getType() == BaseSQLType.MySQL) {
					PurgeDataMySQLCommand c = (PurgeDataMySQLCommand) s;
					c.onComplete().detatchAll();
				} else if (sql.getType() == BaseSQLType.SQLite) {
					PurgeDataSQLiteCommand c = (PurgeDataSQLiteCommand) s;
					c.onComplete().detatchAll();
				}
				
				latch.countDown();
			};
			
			ISQL sql = ServiceLocator.getService(ISQL.class);
			if (sql.getType() == BaseSQLType.MySQL) {
				PurgeDataMySQLCommand command = new PurgeDataMySQLCommand();
				command.onComplete().attach(complete);
				command.start();
			} else if (sql.getType() == BaseSQLType.SQLite) {
				PurgeDataSQLiteCommand command = new PurgeDataSQLiteCommand();
				command.onComplete().attach(complete);
				command.start();
			}
			
			try {
				latch.await();
			} catch (Exception ex) {
				ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
			}
			
			ThreadUtil.schedule(onPurgeDataThread, 60L * 1000L);
		}
	};
	private Runnable checkExceptionLimitReached = new Runnable() {
		public void run() {
			if (exceptionHandler.isLimitReached()) {
				IExceptionHandler oldExceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
				ServiceLocator.removeServices(IExceptionHandler.class);
				
				ServiceLocator.provideService(GameAnalyticsExceptionHandler.class, false);
				exceptionHandler = ServiceLocator.getService(IExceptionHandler.class);
				oldExceptionHandler.disconnect();
				exceptionHandler.connect(new GameAnalyticsBuilder("cc91f0d0506cb43891d02bc09ce30108", "5659d33b32a81a79b205602a2ea7de78f10909b8", version, getServerId()), getName());
				exceptionHandler.setUnsentExceptions(oldExceptionHandler.getUnsentExceptions());
				exceptionHandler.setUnsentLogs(oldExceptionHandler.getUnsentLogs());
			}
			
			ThreadUtil.schedule(checkExceptionLimitReached, 60L * 60L * 1000L);
		}
	};
	
	private void enableMessage() {
		printInfo(ChatColor.AQUA + "Spruce enabled.");
		printInfo(ChatColor.GREEN + "[Version " + getDescription().getVersion() + "] " + ChatColor.RED + numCommands + " commands " + ChatColor.LIGHT_PURPLE + numEvents + " events " + ChatColor.YELLOW + numTicks + " tick handlers");
		printInfo(ChatColor.WHITE + "[Spruce] " + ChatColor.GRAY + "Attempting to load compatibility with Bukkit version " + getGameVersion());
	}
	private void disableMessage() {
		printInfo(ChatColor.GREEN + "--== " + ChatColor.LIGHT_PURPLE + "Spruce Disabled" + ChatColor.GREEN + " ==--");
	}
}
