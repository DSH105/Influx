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

import com.dsh105.influx.dispatch.CommandCallable;
import com.dsh105.influx.dispatch.PreparedCallable;
import com.dsh105.influx.syntax.CommandBinding;
import com.dsh105.influx.syntax.WrappedCommandMethod;
import com.dsh105.influx.syntax.Command;

public class Controller implements Comparable<Controller> {

    private Command command;
    private Description description;

    private CommandBinding commandBinding;
    private CommandCallable callable;

    public Controller(Command command, Description description, CommandBinding commandBinding) {
        this(command, description, commandBinding, null);
    }

    public Controller(Command command, Description description, CommandBinding commandBinding, CommandCallable callable) {
        this.command = command;
        this.description = description;
        this.commandBinding = commandBinding;
        this.callable = callable != null ? callable : new PreparedCallable();
    }

    public Command getCommand() {
        return command;
    }

    public Description getDescription() {
        return description;
    }

    public CommandBinding getCommandBinding() {
        return commandBinding;
    }

    public CommandCallable getCallable() {
        return callable;
    }

    public boolean matches(String input) {
        return getCommand().matches(input);
    }

    @Override
    public int compareTo(Controller controller) {
        return getCommand().compareTo(controller.getCommand());
    }
}