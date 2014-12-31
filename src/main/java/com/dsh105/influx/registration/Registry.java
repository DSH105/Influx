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
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.util.Affirm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Registry {

    private final ArrayList<String> registeredCommands = new ArrayList<>();
    private InfluxManager<?> manager;

    public Registry(InfluxManager<?> manager) {
        this.manager = manager;
    }

    public InfluxManager<?> getManager() {
        return manager;
    }

    public List<String> getRegisteredCommands() {
        return Collections.unmodifiableList(registeredCommands);
    }

    public void register(Collection<Controller> queue) {
        for (Controller controller : queue) {
            if (controller != null) {
                register(controller);
            }
        }
    }

    public boolean register(Controller controller) {
        Affirm.notNull(controller, "Controller must not be null.");
        return !registeredCommands.contains(controller.getCommand().getCommandName()) && registeredCommands.add(controller.getCommand().getCommandName());
    }

    public boolean unregister(Controller controller) {
        return unregister(controller.getCommand().getCommandName());
    }

    public boolean unregister(String command) {
        return registeredCommands.remove(command);
    }

    public void unregisterAll() {
        ArrayList<String> copy = new ArrayList<>(getRegisteredCommands());
        for (String command : copy) {
            unregister(command);
        }
    }
}