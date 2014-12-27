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
import com.dsh105.influx.help.BukkitHelpProvider;
import com.dsh105.influx.help.HelpProvision;
import com.dsh105.influx.registration.RegistrationStrategy;
import com.dsh105.influx.response.BukkitResponder;
import com.dsh105.influx.response.MessagePurpose;
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
        this.dispatcher = new BukkitDispatcher(this);
        this.setHelpTitle(plugin.getName());
        this.setResponseHandler(new BukkitResponder(""));
        this.setHelpProvision(HelpProvision.BUKKIT);
        this.setAuthorization(new Authorization<CommandSender>() {
            @Override
            public boolean authorize(CommandSender sender, Controller toExecute, String permission) {
                return sender.hasPermission(permission);
            }
        });
        this.setMessage(MessagePurpose.RESTRICTED_SENDER, "Please log in to perform that command.");
    }

    @Override
    public BukkitHelpProvider<CommandSender> getHelp() {
        try {
            return (BukkitHelpProvider<CommandSender>) super.getHelp();
        } catch (ClassCastException e) {
            throw new IllegalStateException("Help provider must be a BukkitHelpProvider");
        }
    }

    @Override
    public BukkitResponder getResponder() {
        try {
            return (BukkitResponder) super.getResponder();
        } catch (ClassCastException e) {
            throw new IllegalStateException("Responder must be a BukkitResponder");
        }
    }

    @Override
    public BukkitDispatcher getDispatcher() {
        return (BukkitDispatcher) super.getDispatcher();
    }

    @Override
    public void setHelpTitle(String helpTitle) {
        super.setHelpTitle(helpTitle);
        getHelp().buildHeader();
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}