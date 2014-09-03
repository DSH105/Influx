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

import com.dsh105.influx.syntax.Command;

import java.util.Set;

public interface Mapping extends Iterable<Controller> {

    Set<Controller> getMappedCommands();

    Set<Controller> getMappedCommands(CommandListener listener);


    void register(CommandListener listener, String... parentNests);

    void register(CommandBuilder builder, String... parentNests);

    void nestCommandsIn(CommandListener destination, CommandListener origin, String... parentNests);

    void nestCommandsIn(CommandListener destination, CommandListener origin, boolean onlyAnnotatedNests, String... parentNests);

    void unregister(CommandListener listener);

    void unregister(Controller controller);
}