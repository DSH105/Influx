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
import com.dsh105.influx.Manager;
import com.dsh105.influx.registration.DefaultRegistry;
import com.dsh105.influx.registration.Registry;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BukkitRegistry extends DefaultRegistry {

    static {
        Bukkit.getHelpMap().registerHelpTopicFactory(InfluxCommand.class, new InfluxCommandHelpTopicFactory());
    }

    private static Field SERVER_COMMAND_MAP = Reflection.getField(Bukkit.getServer().getPluginManager().getClass(), "commandMap");

    private final ArrayList<String> REGISTERED_COMMANDS = new ArrayList<>();

    private CommandMap commandMap;

    public BukkitRegistry(Manager manager) {
        super(manager);
    }

    public CommandMap getCommandMap() {
        if (commandMap == null) {
            try {
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
    public List<String> getRegisteredCommands() {
        return Collections.unmodifiableList(REGISTERED_COMMANDS);
    }

    @Override
    public void register(Collection<Controller> queue) {
        if (!queue.isEmpty()) {
            for (Controller controller : queue) {
                register(new InfluxCommand(getManager(), controller));
            }
        }
    }

    @Override
    public void register(InfluxCommand command) {
        if (REGISTERED_COMMANDS.contains(command.getName().toLowerCase().trim())) {
            // Already registered -> no need to do so again
            return;
        }

        if (!getCommandMap().register(getManager().getPlugin().getName(), command)) {
            // More of a backup for above
            unregister(command);
        } else {
            REGISTERED_COMMANDS.add(command.getName().toLowerCase().trim());
        }
    }

    @Override
    public void unregister(InfluxCommand command) {
        command.unregister(getCommandMap());
        REGISTERED_COMMANDS.remove(command.getName().toLowerCase().trim());
    }

    @Override
    public void unregister(String command) {
        org.bukkit.command.Command bukkitCommand = getCommandMap().getCommand(command);
        if (bukkitCommand != null && bukkitCommand instanceof InfluxCommand) {
            unregister((InfluxCommand) bukkitCommand);
        }
    }
}