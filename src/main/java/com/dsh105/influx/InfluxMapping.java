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

import com.dsh105.influx.registration.Registry;

import java.util.List;
import java.util.Set;

public interface InfluxMapping extends Iterable<Controller> {

    Registry getRegistry();

    Class<?> getSenderType();

    Set<Controller> getMappedCommands();

    Set<Controller> getMappedCommands(CommandListener listener);

    boolean exists(Controller controller);

    Set<CommandListener> getRegisteredListeners();

    boolean exists(CommandListener listener);

    List<Controller> register(CommandListener listener, String... parentNests);

    Controller register(CommandBuilder builder, String... parentNests);

    List<Controller> nestCommandsIn(CommandListener destination, CommandListener origin, String... parentNests);

    List<Controller> nestCommandsIn(CommandListener destination, CommandListener origin, boolean onlyAnnotatedNests, String... parentNests);

    Controller nestCommandIn(CommandListener destination, CommandBuilder builder, String... parentNests);

    List<Controller> unregister(CommandListener listener);

    boolean unregister(Controller controller);

    // WARNING: Expensive process
    void updateCommandNesting(Controller controller, String... parents);
}