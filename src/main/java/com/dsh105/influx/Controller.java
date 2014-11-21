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
import com.dsh105.influx.syntax.Command;
import com.dsh105.influx.syntax.CommandBinding;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Controller {

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
    public String toString() {
        return "Controller{" +
                "registeredListener=" + registeredListener +
                ", command=" + command +
                ", description=" + description +
                "}";
    }
}