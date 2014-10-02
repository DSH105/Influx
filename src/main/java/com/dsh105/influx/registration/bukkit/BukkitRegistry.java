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
import com.dsh105.influx.registration.Registry;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;

public class BukkitRegistry extends Registry {

    static {
        Bukkit.getHelpMap().registerHelpTopicFactory(InfluxCommand.class, new InfluxCommandHelpTopicFactory());
    }

    private static Field SERVER_COMMAND_MAP;

    private CommandMap commandMap;

    public BukkitRegistry(InfluxBukkitManager manager) {
        super(manager);
    }

    public CommandMap getCommandMap() {
        if (commandMap == null) {
            try {
                if (SERVER_COMMAND_MAP == null) {
                    SERVER_COMMAND_MAP = Reflection.getField(Bukkit.getServer().getPluginManager().getClass(), "commandMap");
                }
                commandMap = (CommandMap) SERVER_COMMAND_MAP.get(Bukkit.getPluginManager());
            } catch (Exception e) {
                getManager().getPlugin().getLogger().warning("Failed to retrieve CommandMap! Using fallback instead...");

                commandMap = new SimpleCommandMap(Bukkit.getServer());
                Bukkit.getPluginManager().registerEvents(new FallbackCommandListener(commandMap), getManager().getPlugin());
            }
        }

        return commandMap;
    }

    @Override
    public InfluxBukkitManager getManager() {
        return (InfluxBukkitManager) super.getManager();
    }

    @Override
    public boolean register(Controller controller) {
        return super.register(controller) && register(new InfluxCommand(getManager(), controller));
    }

    public boolean register(InfluxCommand command) {
        if (!getCommandMap().register(getManager().getPlugin().getName(), command)) {
            unregister(command);
            return false;
        }
        return true;
    }

    public boolean unregister(InfluxCommand command) {
        Preconditions.checkNotNull(command, "Command must not be null.");
        return command.unregister(getCommandMap());
    }

    @Override
    public boolean unregister(String command) {
        if (super.unregister(command)) {
            org.bukkit.command.Command bukkitCommand = getCommandMap().getCommand(command);
            if (bukkitCommand != null && bukkitCommand instanceof InfluxCommand) {
                return unregister((InfluxCommand) bukkitCommand);
            }
        }
        return false;
    }
}