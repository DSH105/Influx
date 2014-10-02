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

package com.dsh105.influx;

import com.dsh105.influx.dispatch.Authorization;
import com.dsh105.influx.dispatch.BukkitDispatcher;
import com.dsh105.influx.dispatch.Dispatcher;
import com.dsh105.influx.help.HelpProvision;
import com.dsh105.influx.registration.RegistrationStrategy;
import com.dsh105.influx.response.BukkitResponder;
import com.dsh105.influx.response.Responder;
import com.dsh105.influx.response.ResponseLevel;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class BukkitCommandManager extends CommandManager<CommandSender> implements InfluxBukkitManager {

    private Plugin plugin;

    public BukkitCommandManager(Plugin plugin) {
        this(RegistrationStrategy.BUKKIT, plugin);
    }

    public BukkitCommandManager(RegistrationStrategy registrationStrategy, Plugin plugin) {
        super(registrationStrategy, "/");
        this.plugin = plugin;
        this.setResponseHandler(new BukkitResponder(""));
        this.setHelpProvision(HelpProvision.BUKKIT);
        this.setHelpTitle(plugin.getName());
        this.setAuthorization(new Authorization<CommandSender>() {
            @Override
            public boolean authorize(CommandSender sender, Controller toExecute, String permission) {
                return sender.hasPermission(permission);
            }
        });
    }

    @Override
    public BukkitDispatcher getDispatcher() {
        return (BukkitDispatcher) super.getDispatcher();
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}