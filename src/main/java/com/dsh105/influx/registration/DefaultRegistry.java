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

package com.dsh105.influx.registration;

import com.dsh105.influx.Controller;
import com.dsh105.influx.Manager;
import com.dsh105.influx.registration.bukkit.InfluxCommand;
import org.bukkit.command.CommandMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultRegistry implements Registry {

    private Manager manager;

    public DefaultRegistry(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void unregister(String command) {

    }

    @Override
    public void unregister(Controller controller) {
        unregister(controller.getCommand().getCommandName());
    }

    @Override
    public void unregister(InfluxCommand command) {

    }

    @Override
    public void register(InfluxCommand command) {

    }

    @Override
    public void register(Collection<Controller> queue) {
        register(queue.toArray(new Controller[0]));
    }

    @Override
    public void register(Controller... controllers) {

    }

    @Override
    public List<String> getRegisteredCommands() {
        return new ArrayList<>();
    }

    @Override
    public Manager getManager() {
        return manager;
    }
}