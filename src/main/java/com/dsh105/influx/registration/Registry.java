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

import java.util.Collection;
import java.util.List;

public interface Registry {

    Manager getManager();

    List<String> getRegisteredCommands();

    void register(Collection<Controller> queue);

    void register(Controller... controllers);

    void register(InfluxCommand command);

    void unregister(Controller controller);

    void unregister(InfluxCommand command);

    void unregister(String command);
}