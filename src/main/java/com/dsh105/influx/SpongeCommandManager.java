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

import com.dsh105.commodus.Affirm;
import com.dsh105.influx.dispatch.Authorization;
import com.dsh105.influx.dispatch.SpongeDispatcher;
import com.dsh105.influx.help.HelpProvision;
import com.dsh105.influx.help.SpongeHelpProvider;
import com.dsh105.influx.registration.RegistrationStrategy;
import com.dsh105.influx.registration.sponge.SpongeRegistry;
import com.dsh105.influx.response.MessagePurpose;
import com.dsh105.influx.response.SpongeResponder;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.util.command.CommandSource;

public class SpongeCommandManager extends CommandManager<CommandSource> implements InfluxSpongeManager {

    private Game game;
    private PluginContainer pluginContainer;

    public SpongeCommandManager(Object plugin, Game game) {
        this(null, plugin, game);
        this.setRegistrationStrategy(new SpongeRegistry(this, plugin, game, (SpongeDispatcher) dispatcher, getAuthorization()));
    }
    
    public SpongeCommandManager(SpongeRegistry registry, Object plugin, Game game) {
        super("/");
        Affirm.notNull(game);
        this.pluginContainer = game.getPluginManager().fromInstance(plugin).orNull();
        if (this.pluginContainer == null) {
            throw new IllegalArgumentException("Provided plugin instance must be annotated with @Plugin.");
        }
        this.game = game;
        this.dispatcher = new SpongeDispatcher(this);
        this.setHelpTitle(pluginContainer.getName());
        this.setResponseHandler(new SpongeResponder(""));
        this.setHelpProvision(HelpProvision.SPONGE);
        this.setAuthorization(new Authorization<CommandSource>() {
            @Override
            public boolean authorize(CommandSource sender, Controller toExecute, String permission) {
                if (sender instanceof Player) {
                    return getGame().getServiceManager().provideUnchecked(PermissionService.class).login((Player) sender).isPermitted(permission);
                }
                // console, command block, etc.
                return true;
            }
        });
        if (registry != null) {
            this.setRegistrationStrategy(registry);
        }
        this.setMessage(MessagePurpose.RESTRICTED_SENDER, "Please log in to perform that command.");
    }

    @Override
    public SpongeHelpProvider<CommandSource> getHelp() {
        try {
            return (SpongeHelpProvider<CommandSource>) super.getHelp();
        } catch (ClassCastException e) {
            throw new IllegalStateException("Help provider must be a SpongeHelpProvider");
        }
    }

    @Override
    public SpongeResponder getResponder() {
        try {
            return (SpongeResponder) super.getResponder();
        } catch (ClassCastException e) {
            throw new IllegalStateException("Responder must be a SpongeResponder");
        }
    }

    @Override
    public SpongeDispatcher getDispatcher() {
        return (SpongeDispatcher) super.getDispatcher();
    }

    @Override
    public void setHelpTitle(String helpTitle) {
        super.setHelpTitle(helpTitle);
        getHelp().buildHeader();
    }

    @Override
    public Game getGame() {
        return game;
    }

    @Override
    public Object getPlugin() {
        return pluginContainer.getInstance();
    }

    @Override
    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }
}