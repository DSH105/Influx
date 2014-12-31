/*
 * This file is part of Influx.
 *
 * Influx is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Influx is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Influx.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsh105.influx.registration.bukkit;

import com.dsh105.commodus.reflection.Reflection;
import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxBukkitManager;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.registration.Registry;
import com.dsh105.influx.util.Affirm;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public class BukkitRegistry extends Registry {

    static {
        Bukkit.getHelpMap().registerHelpTopicFactory(InfluxBukkitCommand.class, new InfluxBukkitCommandHelpTopicFactory());
    }

    private static Field SERVER_COMMAND_MAP;

    private CommandMap commandMap;
    
    private Plugin plugin;
    private CommandExecutor executor;

    public BukkitRegistry(InfluxManager<?> manager, Plugin plugin, CommandExecutor executor) {
        super(manager);
        this.plugin = plugin;
        this.executor = executor;
    }

    public CommandMap getCommandMap() {
        if (commandMap == null) {
            try {
                if (SERVER_COMMAND_MAP == null) {
                    SERVER_COMMAND_MAP = Reflection.getField(Bukkit.getServer().getPluginManager().getClass(), "commandMap");
                }
                commandMap = (CommandMap) SERVER_COMMAND_MAP.get(Bukkit.getPluginManager());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to retrieve CommandMap! Using fallback instead...");

                commandMap = new SimpleCommandMap(Bukkit.getServer());
                Bukkit.getPluginManager().registerEvents(new BukkitFallbackCommandListener(commandMap), plugin);
            }
        }

        return commandMap;
    }

    @Override
    public boolean register(Controller controller) {
        return super.register(controller) && register(new InfluxBukkitCommand(getManager(), plugin, executor, controller));
    }

    public boolean register(InfluxBukkitCommand command) {
        if (!getCommandMap().register(plugin.getName(), command)) {
            unregister(command);
            return false;
        }
        return true;
    }

    public boolean unregister(InfluxBukkitCommand command) {
        Affirm.notNull(command, "Command must not be null.");
        return command.unregister(getCommandMap());
    }

    @Override
    public boolean unregister(String command) {
        if (super.unregister(command)) {
            org.bukkit.command.Command bukkitCommand = getCommandMap().getCommand(command);
            if (bukkitCommand != null && bukkitCommand instanceof InfluxBukkitCommand) {
                return unregister((InfluxBukkitCommand) bukkitCommand);
            }
        }
        return false;
    }
}