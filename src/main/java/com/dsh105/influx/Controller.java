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

import com.dsh105.influx.dispatch.CommandInvoker;
import com.dsh105.influx.dispatch.InjectedInvoker;
import com.dsh105.influx.syntax.CommandBinding;
import com.dsh105.influx.syntax.Command;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Controller implements Comparable<Controller> {

    private CommandListener registeredListener;
    private Command command;
    private Description description;

    private CommandBinding commandBinding;
    private CommandInvoker commandInvoker;

    public Controller(CommandListener registeredListener, Command command, Description description, CommandBinding commandBinding) {
        this(registeredListener, command, description, commandBinding, null);
    }

    public Controller(CommandListener registeredListener, Command command, Description description, CommandBinding commandBinding, CommandInvoker commandInvoker) {
        this.registeredListener = registeredListener;
        this.command = command;
        this.description = description;
        this.commandBinding = commandBinding;
        this.commandInvoker = commandInvoker != null ? commandInvoker : new InjectedInvoker();
    }

    public CommandListener getRegisteredListener() {
        return registeredListener;
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

    public CommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    @Override
    public int compareTo(Controller controller) {
        return getCommand().compareTo(controller.getCommand());
    }

    @Override
    public String toString() {
        return "Controller{" +
                "registeredListener=" + registeredListener +
                ", command=" + command +
                ", description=" + description +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Controller)) return false;

        Controller that = (Controller) o;

        if (!commandInvoker.equals(that.commandInvoker)) return false;
        if (!command.equals(that.command)) return false;
        if (!commandBinding.equals(that.commandBinding)) return false;
        if (!description.equals(that.description)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = command.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + commandBinding.hashCode();
        result = 31 * result + commandInvoker.hashCode();
        return result;
    }
}