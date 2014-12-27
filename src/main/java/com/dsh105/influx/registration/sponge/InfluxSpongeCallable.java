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
import com.dsh105.influx.InfluxSpongeManager;
import com.google.common.base.Optional;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandSource;

import java.util.ArrayList;
import java.util.List;

public class InfluxSpongeCallable implements CommandCallable {

    private final InfluxSpongeManager manager;
    private final Controller controller;

    public InfluxSpongeCallable(InfluxSpongeManager manager, Controller controller) {
        this.manager = manager;
        this.controller = controller;
    }

    public InfluxSpongeManager getManager() {
        return manager;
    }

    public Controller getController() {
        return controller;
    }

    @Override
    public boolean call(CommandSource source, String arguments, List<String> parents) throws CommandException {
        // TODO: include the parents perhaps?
        return manager.getDispatcher().dispatch(source, arguments);
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return manager.authorize(source, controller);
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