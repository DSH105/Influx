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
import com.dsh105.influx.dispatch.SpongeCommandDispatcher;
import com.dsh105.influx.registration.Registry;
import com.google.common.base.Optional;
import org.spongepowered.api.Game;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.api.util.command.CommandSource;

import java.util.HashMap;
import java.util.Map;

public class SpongeRegistry extends Registry {

    private final CommandService commandService;

    private final Map<String, CommandMapping> mappings = new HashMap<>();

    private Object plugin;
    private Game game;
    private SpongeCommandDispatcher dispatcher;
    private Authorization<CommandSource> authorization;

    public SpongeRegistry(InfluxManager<?> manager, Object plugin, Game game, SpongeCommandDispatcher dispatcher, Authorization<CommandSource> authorization) {
        super(manager);
        this.commandService = game.getCommandDispatcher();
        this.plugin = plugin;
        this.game = game;
        this.dispatcher = dispatcher;
        this.authorization = authorization;
    }

    @Override
    public boolean register(Controller controller) {
        Optional<CommandMapping> result = commandService.register(plugin, new InfluxSpongeCallable(getManager(), plugin, game, dispatcher, authorization, controller));
        if (result.isPresent() && !result.get().getAllAliases().isEmpty()) {
            mappings.put(controller.getCommand().getCommandName(), result.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean unregister(String command) {
        if (super.unregister(command)) {
            CommandMapping commandMapping = mappings.get(command);
            commandService.removeMapping(commandMapping);
            return true;
        }
        return false;
    }
}