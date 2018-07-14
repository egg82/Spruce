package me.egg82.sprc;

import java.io.File;

import me.egg82.sprc.sql.mysql.CreateTablesMySQLCommand;
import me.egg82.sprc.sql.sqlite.CreateTablesSQLiteCommand;
import ninja.egg82.bukkit.BasePlugin;
import ninja.egg82.bukkit.services.ConfigRegistry;
import ninja.egg82.patterns.ServiceLocator;
import ninja.egg82.patterns.registries.IVariableRegistry;
import ninja.egg82.sql.ISQL;
import ninja.egg82.sql.MySQL;
import ninja.egg82.sql.SQLite;

public class Loaders {
	//vars
	
	//constructor
	public Loaders() {
		
	}
	
	//public
	public static void loadStorage() {
		BasePlugin plugin = ServiceLocator.getService(BasePlugin.class);
		IVariableRegistry<String> configRegistry = ServiceLocator.getService(ConfigRegistry.class);
		ISQL sql = null;
		
		if (configRegistry.hasRegister("sql.type") && configRegistry.hasRegister("sql.threads")) {
			String type = configRegistry.getRegister("sql.type", String.class);
			int threads = configRegistry.getRegister("sql.threads", Number.class).intValue();
			
			if (type.equalsIgnoreCase("mysql")) {
				if (
					configRegistry.hasRegister("sql.mysql.address")
					&& configRegistry.hasRegister("sql.mysql.port")
					&& configRegistry.hasRegister("sql.mysql.user")
					&& configRegistry.hasRegister("sql.mysql.database")
				) {
					sql = new MySQL(threads, plugin.getName(), plugin.getClass().getClassLoader());
					sql.connect(
						configRegistry.getRegister("sql.mysql.address", String.class),
						configRegistry.getRegister("sql.mysql.port", Number.class).intValue(),
						configRegistry.getRegister("sql.mysql.user", String.class),
						configRegistry.hasRegister("sql.mysql.pass") ? configRegistry.getRegister("sql.mysql.pass", String.class) : "",
						configRegistry.getRegister("sql.mysql.database", String.class)
					);
					ServiceLocator.provideService(sql);
					new CreateTablesMySQLCommand().start();
				} else {
					throw new RuntimeException("\"sql.mysql.address\", \"sql.mysql.port\", \"sql.mysql.user\", or \"sql.mysql.database\" missing from config. Aborting plugin load.");
				}
			} else if (type.equalsIgnoreCase("sqlite")) {
				if (
					configRegistry.hasRegister("sql.sqlite.file")
				) {
					sql = new SQLite(threads, plugin.getName(), plugin.getClass().getClassLoader());
					sql.connect(new File(plugin.getDataFolder(), configRegistry.getRegister("sql.sqlite.file", String.class)).getAbsolutePath());
					ServiceLocator.provideService(sql);
					new CreateTablesSQLiteCommand().start();
				} else {
					throw new RuntimeException("\"sql.sqlite.file\" missing from config. Aborting plugin load.");
				}
			} else {
				throw new RuntimeException("\"sql.type\" was neither 'mysql' nor 'sqlite'. Aborting plugin load.");
			}
		} else {
			throw new RuntimeException("\"sql.type\" or \"sql.threads\" missing from config. Aborting plugin load.");
		}
	}
	
	//private
	
}
