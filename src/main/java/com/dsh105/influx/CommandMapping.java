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

import com.dsh105.influx.annotation.Nest;
import com.dsh105.influx.annotation.Restrict;
import com.dsh105.influx.registration.RegistrationStrategy;
import com.dsh105.influx.registration.Registry;
import com.dsh105.influx.syntax.Command;

import java.lang.reflect.Method;
import java.util.*;

public abstract class CommandMapping implements Mapping {

    private final Map<CommandListener, TreeSet<Controller>> commands = new HashMap<>();

    public abstract Registry getRegistry();

    @Override
    public Set<Controller> getMappedCommands() {
        Set<Controller> mapped = new HashSet<>();
        for (Set<Controller> commandSet : commands.values()) {
            mapped.addAll(commandSet);
        }
        return Collections.unmodifiableSet(mapped);
    }

    @Override
    public Set<Controller> getMappedCommands(CommandListener listener) {
        return Collections.unmodifiableSet(commands.get(listener));
    }

    @Override
    public void register(CommandListener listener, String... parentNests) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            CommandBuilder builder;
            try {
                builder = new CommandBuilder().from(listener, method.getName());
            } catch (InvalidCommandException e) {
                // Not a command, ignore it
                continue;
            }

            if (builder.isNested) {
                // Skip it here - dealt with later
                continue;
            }

            register(builder, parentNests);
        }

        nestCommandsIn(listener, listener, true, parentNests);
    }

    @Override
    public void register(CommandBuilder builder, String... parentNests) {
        Controller controller = builder.build();
        controller.getCommand().nestUnder(parentNests);
        TreeSet<Controller> mappedCommands = commands.get(controller.getCommand().getListener());
        if (mappedCommands == null) {
            mappedCommands = new TreeSet<>();
        }

        mappedCommands.add(controller);
        commands.put(builder.getListener(), mappedCommands);
        getRegistry().register(controller);
    }

    @Override
    public void nestCommandsIn(CommandListener destination, CommandListener origin, String... parentNests) {
        nestCommandsIn(destination, origin, true, parentNests);
    }

    @Override
    public void nestCommandsIn(CommandListener destination, CommandListener origin, boolean onlyAnnotatedNests, String... parentNests) {

    }

    @Override
    public void unregister(CommandListener listener) {
        TreeSet<Controller> mappedCommands = commands.get(listener);
        if (mappedCommands == null) {
            return;
        }

        commands.remove(listener);
        for (Controller controller : mappedCommands) {
            getRegistry().unregister(controller);
        }
    }

    @Override
    public void unregister(Controller controller) {
        TreeSet<Controller> mappedCommands = commands.get(controller.getCommand().getListener());
        if (mappedCommands == null) {
            return;
        }
        mappedCommands.remove(controller);
        getRegistry().unregister(controller);
        commands.put(controller.getCommand().getListener(), mappedCommands);
    }

    @Override
    public Iterator<Controller> iterator() {
        return getMappedCommands().iterator();
    }
}