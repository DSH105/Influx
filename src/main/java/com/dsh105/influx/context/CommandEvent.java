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

package com.dsh105.influx.context;

import com.dsh105.influx.Controller;
import com.dsh105.influx.Manager;
import com.dsh105.influx.syntax.parameter.EventVariable;
import com.dsh105.influx.syntax.Range;
import com.dsh105.influx.syntax.parameter.Variable;
import com.dsh105.powermessage.markup.MarkupBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandEvent<T extends CommandSender> extends CommandContext {

    private final Map<Range, String[]> argumentSets = new HashMap<>();
    private final Map<String, EventVariable> eventVariableMap = new HashMap<>();

    private String input;
    private T sender;

    private String[] arguments;

    public CommandEvent(Manager manager, Controller controller, String input, T sender) {
        super(manager, controller);
        this.input = input;
        this.sender = sender;
    }

    public String getInput() {
        return input;
    }

    public String[] getArguments() {
        if (arguments == null) {
            arguments = input.split("\\s+");
        }
        return arguments;
    }

    public String var(String name) {
        return variable(name).getCombinedValue();
    }

    public String var(int index) {
        return variable(index).getCombinedValue();
    }

    @Override
    public EventVariable variable(int index) {
        Variable variable = super.variable(index);
        EventVariable result = eventVariableMap.get(variable.getName());
        if (result == null) {
            result = new EventVariable(variable, this);
            eventVariableMap.put(result.getName(), result);
        }
        return result;
    }

    @Override
    public EventVariable variable(String name) {
        EventVariable result = eventVariableMap.get(name);
        if (result == null) {
            result = new EventVariable(super.variable(name), this);
            eventVariableMap.put(result.getName(), result);
        }
        return result;
    }

    public String arg(int index) {
        if (index >= getArguments().length) {
            throw new ArgumentException("Argument " + index + " does not exist.");
        }
        return getArguments()[index];
    }

    public String arg(int index, int startIndex, int endIndex) {
        return arg(index, new Range(startIndex, endIndex));
    }

    public String arg(int index, Range argumentRange) {
        String[] arguments = args(argumentRange);
        if (index >= getArguments().length) {
            throw new ArgumentException("Argument " + index + " does not exist in range "  + argumentRange.getStartIndex() + "-" + argumentRange.getEndIndex() + ".");
        }
        return arguments[index];
    }

    public String[] args(int startIndex, int endIndex) {
        return args(new Range(startIndex, endIndex));
    }

    public String[] args(Range argumentRange) {
        if (argumentRange.getStartIndex() >= getArguments().length) {
            throw new ArgumentException("Argument in range " + argumentRange.getStartIndex() + "-" + argumentRange.getEndIndex() + " does not exist.");
        }

        String[] result = argumentSets.get(argumentRange);
        if (result == null || result.length == 0) {
            result = Arrays.copyOfRange(getArguments(), argumentRange.getStartIndex(), argumentRange.getEndIndex());
            argumentSets.put(argumentRange, result);
        }
        return result;
    }

    public T sender() {
        return sender;
    }

    public void respond(String response, ChatColor... formats) {
        getManager().getFormatSet().send(sender(), response, formats);
    }

    public void respond(String response) {
        getManager().getFormatSet().send(sender(), response);
    }
}