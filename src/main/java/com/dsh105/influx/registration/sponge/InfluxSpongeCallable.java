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

package com.dsh105.influx.registration.sponge;

import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.InfluxSpongeManager;
import com.dsh105.influx.dispatch.Authorization;
import com.dsh105.influx.dispatch.Dispatcher;
import com.dsh105.influx.dispatch.SpongeCommandDispatcher;
import com.google.common.base.Optional;
import org.spongepowered.api.Game;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;

import java.util.ArrayList;
import java.util.List;

public class InfluxSpongeCallable implements CommandCallable {

    private final InfluxManager<?> manager;
    private final Controller controller;
    private Object plugin;
    private Game game;
    private SpongeCommandDispatcher dispatcher;
    private Authorization<CommandSource> authorization;

    public InfluxSpongeCallable(InfluxManager<?> manager, Object plugin, Game game, SpongeCommandDispatcher dispatcher, Authorization<CommandSource> authorization, Controller controller) {
        this.manager = manager;
        this.controller = controller;
        this.plugin = plugin;
        this.game = game;
        this.dispatcher = dispatcher;
        this.authorization = authorization;
    }

    public InfluxManager<?> getManager() {
        return manager;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public boolean call(CommandSource source, String arguments, List<String> parents) throws CommandException {
        // TODO: include the parents perhaps?
        return dispatcher.dispatch(source, arguments);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        for (String permission : controller.getCommand().getPermissions()) {
            if (!authorization.authorize(source, controller, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Optional<String> getShortDescription() {
        return Optional.of(controller.getDescription().getShortDescription());
    }

    @Override
    public Optional<String> getHelp() {
        return Optional.absent();
    }

    @Override
    public String getUsage() {
        return controller.getDescription().getUsage()[0];
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        // TODO: implement this?
        return new ArrayList<>();
    }
}