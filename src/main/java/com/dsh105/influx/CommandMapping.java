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

import com.dsh105.influx.util.Comparators;

import java.lang.reflect.Method;
import java.util.*;

public abstract class CommandMapping implements InfluxMapping {

    private final Map<CommandListener, TreeSet<Controller>> commands = new HashMap<>();

    @Override
    public SortedSet<Controller> getMappedCommands() {
        SortedSet<Controller> mapped = new TreeSet<>(new Comparators.ControllerComparator());
        for (Set<Controller> commandSet : commands.values()) {
            mapped.addAll(commandSet);
        }
        return Collections.unmodifiableSortedSet(mapped);
    }

    @Override
    public SortedSet<Controller> getMappedCommands(CommandListener listener) {
        return Collections.unmodifiableSortedSet(commands.get(listener));
    }

    @Override
    public boolean exists(Controller controller) {
        return getMappedCommands().contains(controller);
    }

    @Override
    public Set<CommandListener> getRegisteredListeners() {
        return Collections.unmodifiableSet(commands.keySet());
    }

    @Override
    public boolean exists(CommandListener listener) {
        return getRegisteredListeners().contains(listener);
    }

    @Override
    public List<Controller> register(CommandListener listener, String... parentNests) {
        return nestCommandsIn(listener, listener, true, parentNests);
    }

    @Override
    public Controller register(CommandBuilder builder, String... parentNests) {
        return nestCommandIn(builder.getOriginListener(), builder, parentNests);
    }

    @Override
    public List<Controller> nestCommandsIn(CommandListener destination, CommandListener origin, String... parentNests) {
        return nestCommandsIn(destination, origin, true, parentNests);
    }

    @Override
    public List<Controller> nestCommandsIn(CommandListener destination, CommandListener origin, boolean nestAll, String... parentNests) {
        List<Controller> registered = new ArrayList<>();
        for (Method method : origin.getClass().getDeclaredMethods()) {
            CommandBuilder builder;
            try {
                builder = new CommandBuilder().from(origin, method);
            } catch (IllegalCommandException e) {
                // Not a command, ignore it
                continue;
            }

            CommandNest nest = new CommandNest(nestAll, destination, builder, parentNests);
            builder.restrict(nest.getPermissions().toArray(new String[0]));
            Controller controller = register(builder, nest.getParents().toArray(new String[0]));
            if (controller != null) {
                registered.add(controller);
            }
        }
        return Collections.unmodifiableList(registered);
    }

    @Override
    public Controller nestCommandIn(CommandListener destination, CommandBuilder builder, String... parentNests) {
        Controller controller;
        try {
            controller = builder.build(destination, parentNests);
        } catch (IllegalCommandException e) {
            throw new IllegalArgumentException("Illegal command syntax provided.", e);
        }
        if (!getSenderType().isAssignableFrom(controller.getCommand().getAcceptedSenderType())) {
            throw new IllegalArgumentException("Manager can only have commands accepting the following generic type: " + getSenderType().getSimpleName() + " (not " + controller.getCommand().getAcceptedSenderType() + ")");
        }

        if (exists(controller)) {
            return null;
        }

        TreeSet<Controller> mappedCommands = commands.get(controller.getRegisteredListener());
        if (mappedCommands == null) {
            mappedCommands = new TreeSet<>(new Comparators.ControllerComparator());
        }


        if (mappedCommands.add(controller)) {
            commands.put(controller.getRegisteredListener(), mappedCommands);
            getRegistry().register(controller);
            return controller;
        }
        return null;
    }

    @Override
    public List<Controller> unregister(CommandListener listener) {
        List<Controller> unregistered = new ArrayList<>();
        TreeSet<Controller> mappedCommands = commands.get(listener);
        if (mappedCommands == null) {
            return unregistered;
        }

        commands.remove(listener);
        for (Controller controller : mappedCommands) {
            getRegistry().unregister(controller);
            unregistered.add(controller);
        }
        return Collections.unmodifiableList(unregistered);
    }

    @Override
    public boolean unregister(Controller controller) {
        TreeSet<Controller> mappedCommands = commands.get(controller.getRegisteredListener());
        if (mappedCommands == null) {
            return false;
        }
        if (mappedCommands.contains(controller)) {
            mappedCommands.remove(controller);
            getRegistry().unregister(controller);
            commands.put(controller.getRegisteredListener(), mappedCommands);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<Controller> iterator() {
        return getMappedCommands().iterator();
    }
}