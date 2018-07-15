package me.egg82.sprc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.logging.Level;

import org.bukkit.ChatColor;

import me.egg82.sprc.buffers.BlockDataBuffer;
import me.egg82.sprc.buffers.PlayerChatBuffer;
import me.egg82.sprc.buffers.PlayerDataBuffer;
import me.egg82.sprc.core.BlockDataInsertContainer;
import me.egg82.sprc.core.PlayerChatInsertContainer;
import me.egg82.sprc.core.PlayerDataInsertContainer;
import me.egg82.sprc.sql.mysql.InsertBlockDataMySQLCommand;
import me.egg82.sprc.sql.mysql.InsertPlayerChatMySQLCommand;
import me.egg82.sprc.sql.mysql.InsertPlayerDataMySQLCommand;
import me.egg82.sprc.sql.mysql.PurgeDataMySQLCommand;
import me.egg82.sprc.sql.sqlite.InsertBlockDataSQLiteCommand;
import me.egg82.sprc.sql.sqlite.InsertPlayerChatSQLiteCommand;
import me.egg82.sprc.sql.sqlite.InsertPlayerDataSQLiteCommand;
import me.egg82.sprc.sql.sqlite.PurgeDataSQLiteCommand;
import ninja.egg82.bukkit.BasePlugin;
import ninja.egg82.bukkit.processors.CommandProcessor;
import ninja.egg82.bukkit.processors.EventProcessor;
import ninja.egg82.bukkit.services.ConfigRegistry;
import ninja.egg82.bukkit.utils.YamlUtil;
import ninja.egg82.enums.BaseSQLType;
import ninja.egg82.events.CompleteEventArgs;
import ninja.egg82.exceptionHandlers.GameAnalyticsExceptionHandler;
import ninja.egg82.exceptionHandlers.IExceptionHandler;
import ninja.egg82.exceptionHandlers.RollbarExceptionHandler;
import ninja.egg82.exceptionHandlers.builders.GameAnalyticsBuilder;
import ninja.egg82.exceptionHandlers.builders.RollbarBuilder;
import ninja.egg82.patterns.DoubleBuffer;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.plugin.messaging.IMessageHandler;
import ninja.egg82.plugin.utils.PluginReflectUtil;
import ninja.egg82.sql.ISQL;
import ninja.egg82.utils.FileUtil;
import ninja.egg82.utils.ThreadUtil;
import ninja.egg82.utils.TimeUtil;

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
		PluginReflectUtil.addServicesFromPackage("me.egg82.sprc.buffers", true);
		
		ServiceLocator.getService(ConfigRegistry.class).load(YamlUtil.getOrLoadDefaults(getDataFolder().getAbsolutePath() + FileUtil.DIRECTORY_SEPARATOR_CHAR + "config.yml", "config.yml", true));
		Config.prefix = ServiceLocator.getService(ConfigRegistry.class).getRegister("sql.prefix", String.class);
		Config.purgePlayer = TimeUtil.getTime(ServiceLocator.getService(ConfigRegistry.class).getRegister("autoPurge.player", String.class));
		Config.purgeWorld = TimeUtil.getTime(ServiceLocator.getService(ConfigRegistry.class).getRegister("autoPurge.world", String.class));
		
		Config.playerConfig.chat = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.player.chat", Boolean.class).booleanValue();
		Config.playerConfig.command = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.player.command", Boolean.class).booleanValue();
		Config.playerConfig.login = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.player.login", Boolean.class).booleanValue();
		Config.playerConfig.logout = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.player.logout", Boolean.class).booleanValue();
		Config.playerConfig.worldChange = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.player.worldChange", Boolean.class).booleanValue();
		
		Config.blockConfig.player = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.player", Boolean.class).booleanValue();
		Config.blockConfig.explode = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.explode", Boolean.class).booleanValue();
		Config.blockConfig.sign = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.sign", Boolean.class).booleanValue();
		Config.blockConfig.ignite = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.ignite", Boolean.class).booleanValue();
		Config.blockConfig.grow = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.grow", Boolean.class).booleanValue();
		Config.blockConfig.burn = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.burn", Boolean.class).booleanValue();
		Config.blockConfig.inventory = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.inventory", Boolean.class).booleanValue();
		Config.blockConfig.entity = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.entity", Boolean.class).booleanValue();
		Config.blockConfig.piston = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.piston", Boolean.class).booleanValue();
		Config.blockConfig.teleport = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.teleport", Boolean.class).booleanValue();
		Config.blockConfig.damage = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.damage", Boolean.class).booleanValue();
		Config.blockConfig.dispense = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.dispense", Boolean.class).booleanValue();
		Config.blockConfig.form = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.form", Boolean.class).booleanValue();
		Config.blockConfig.spread = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.spread", Boolean.class).booleanValue();
		Config.blockConfig.cauldron = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.cauldron", Boolean.class).booleanValue();
		Config.blockConfig.decay = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.decay", Boolean.class).booleanValue();
		Config.blockConfig.worldedit = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.block.worldedit", Boolean.class).booleanValue();
		
		Config.entityConfig.spawn = ServiceLocator.getService(ConfigRegistry.class).getRegister("log.entity.spawn", Boolean.class).booleanValue();
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
		ThreadUtil.schedule(onFlushDataThread, 1L * 1000L); // The longer we wait, the more "off" actual insert times become
		ThreadUtil.schedule(onPurgeDataThread, 60L * 1000L);
	}
	public void onDisable() {
		super.onDisable();
		
		ThreadUtil.shutdown(1000L);
		
		onFlushDataThread.run();
		
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
	private Runnable onFlushDataThread = new Runnable() {
		public void run() {
			flushPlayerChat();
			flushPlayerData();
			flushBlockData();
			
			ThreadUtil.schedule(onFlushDataThread, 1L * 1000L);
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
	
	private void flushPlayerChat() {
		CountDownLatch latch = new CountDownLatch(1);
		
		BiConsumer<Object, CompleteEventArgs<?>> complete = (s, e) -> {
			ISQL sql = ServiceLocator.getService(ISQL.class);
			if (sql.getType() == BaseSQLType.MySQL) {
				InsertPlayerChatMySQLCommand c = (InsertPlayerChatMySQLCommand) s;
				c.onComplete().detatchAll();
			} else if (sql.getType() == BaseSQLType.SQLite) {
				InsertPlayerChatSQLiteCommand c = (InsertPlayerChatSQLiteCommand) s;
				c.onComplete().detatchAll();
			}
			
			latch.countDown();
		};
		
		DoubleBuffer<PlayerChatInsertContainer> buffer = ServiceLocator.getService(PlayerChatBuffer.class);
		buffer.swapBuffers();
		
		if (buffer.getBackBuffer().size() == 0) {
			return;
		}
		
		List<PlayerChatInsertContainer> data = new ArrayList<PlayerChatInsertContainer>(buffer.getBackBuffer());
		buffer.getBackBuffer().clear();
		
		ISQL sql = ServiceLocator.getService(ISQL.class);
		if (sql.getType() == BaseSQLType.MySQL) {
			InsertPlayerChatMySQLCommand command = new InsertPlayerChatMySQLCommand(data);
			command.onComplete().attach(complete);
			command.start();
		} else if (sql.getType() == BaseSQLType.SQLite) {
			InsertPlayerChatSQLiteCommand command = new InsertPlayerChatSQLiteCommand(data);
			command.onComplete().attach(complete);
			command.start();
		}
		
		try {
			latch.await();
		} catch (Exception ex) {
			ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
		}
	}
	private void flushPlayerData() {
		CountDownLatch latch = new CountDownLatch(1);
		
		BiConsumer<Object, CompleteEventArgs<?>> complete = (s, e) -> {
			ISQL sql = ServiceLocator.getService(ISQL.class);
			if (sql.getType() == BaseSQLType.MySQL) {
				InsertPlayerDataMySQLCommand c = (InsertPlayerDataMySQLCommand) s;
				c.onComplete().detatchAll();
			} else if (sql.getType() == BaseSQLType.SQLite) {
				InsertPlayerDataSQLiteCommand c = (InsertPlayerDataSQLiteCommand) s;
				c.onComplete().detatchAll();
			}
			
			latch.countDown();
		};
		
		DoubleBuffer<PlayerDataInsertContainer> buffer = ServiceLocator.getService(PlayerDataBuffer.class);
		buffer.swapBuffers();
		
		if (buffer.getBackBuffer().size() == 0) {
			return;
		}
		
		List<PlayerDataInsertContainer> data = new ArrayList<PlayerDataInsertContainer>(buffer.getBackBuffer());
		buffer.getBackBuffer().clear();
		
		ISQL sql = ServiceLocator.getService(ISQL.class);
		if (sql.getType() == BaseSQLType.MySQL) {
			InsertPlayerDataMySQLCommand command = new InsertPlayerDataMySQLCommand(data);
			command.onComplete().attach(complete);
			command.start();
		} else if (sql.getType() == BaseSQLType.SQLite) {
			InsertPlayerDataSQLiteCommand command = new InsertPlayerDataSQLiteCommand(data);
			command.onComplete().attach(complete);
			command.start();
		}
		
		try {
			latch.await();
		} catch (Exception ex) {
			ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
		}
	}
	private void flushBlockData() {
		CountDownLatch latch = new CountDownLatch(1);
		
		BiConsumer<Object, CompleteEventArgs<?>> complete = (s, e) -> {
			ISQL sql = ServiceLocator.getService(ISQL.class);
			if (sql.getType() == BaseSQLType.MySQL) {
				InsertBlockDataMySQLCommand c = (InsertBlockDataMySQLCommand) s;
				c.onComplete().detatchAll();
			} else if (sql.getType() == BaseSQLType.SQLite) {
				InsertBlockDataSQLiteCommand c = (InsertBlockDataSQLiteCommand) s;
				c.onComplete().detatchAll();
			}
			
			latch.countDown();
		};
		
		DoubleBuffer<BlockDataInsertContainer> buffer = ServiceLocator.getService(BlockDataBuffer.class);
		buffer.swapBuffers();
		
		if (buffer.getBackBuffer().size() == 0) {
			return;
		}
		
		List<BlockDataInsertContainer> data = new ArrayList<BlockDataInsertContainer>(buffer.getBackBuffer());
		buffer.getBackBuffer().clear();
		
		ISQL sql = ServiceLocator.getService(ISQL.class);
		if (sql.getType() == BaseSQLType.MySQL) {
			InsertBlockDataMySQLCommand command = new InsertBlockDataMySQLCommand(data);
			command.onComplete().attach(complete);
			command.start();
		} else if (sql.getType() == BaseSQLType.SQLite) {
			InsertBlockDataSQLiteCommand command = new InsertBlockDataSQLiteCommand(data);
			command.onComplete().attach(complete);
			command.start();
		}
		
		try {
			latch.await();
		} catch (Exception ex) {
			ServiceLocator.getService(IExceptionHandler.class).silentException(ex);
		}
	}
}
