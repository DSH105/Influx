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

package com.dsh105.influx.dispatch;

import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.response.ResponseLevel;
import com.dsh105.influx.syntax.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandContext<S> {

    private InfluxManager<S> manager;
    private Controller controller;

    protected ConsumedArgumentSet consumedArgumentSet;
    private String input;
    private String[] arguments;
    private S sender;

    private final Map<Range, String[]> argumentSets = new HashMap<>();
    private final Map<String, ContextualVariable> consumedVariables = new HashMap<>();

    public CommandContext(InfluxManager<S> manager, Controller controller, S sender, ConsumedArgumentSet consumedArgumentSet) {
        this.manager = manager;
        this.controller = controller;
        this.consumedArgumentSet = consumedArgumentSet;
        this.input = consumedArgumentSet.getInput();
        this.arguments = input.split("\\s+");
        this.sender = sender;
    }

    public InfluxManager<S> getManager() {
        return manager;
    }

    public S sender() {
        return sender;
    }

    public Controller getController() {
        return controller;
    }

    public Command getCommand() {
        return getController().getCommand();
    }

    public String getInput() {
        return input;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String[] args() {
        return getArguments();
    }

    public int argsLength() {
        return args().length;
    }

    public void respond(String response) {
        respond(response, ResponseLevel.DEFAULT);
    }

    public void respond(String response, ResponseLevel level) {
        getManager().respond(sender(), response, level);
    }

    public void respondAnonymously(String response) {
        respondAnonymously(response, ResponseLevel.DEFAULT);
    }

    public void respondAnonymously(String response, ResponseLevel level) {
        getManager().respondAnonymously(sender(), response, level);
    }

    @Deprecated // it uses a deprecated method
    public Parameter parameter(int index) {
        return controller.getCommand().getParameter(index);
    }

    public Parameter parameter(String name) {
        return controller.getCommand().getParameter(name);
    }

    @Deprecated
    public ContextualVariable getVariable(int index) {
        Variable variable = parameter(index, Variable.class);
        if (variable == null) {
            return null;
        }

        ContextualVariable result = consumedVariables.get(variable.getName());
        if (result == null) {
            try {
                result = new ContextualVariable(variable, this, consumedArgumentSet.getConsumedArguments(variable));
            } catch (IllegalVerificationException e) {
                throw new IllegalStateException("Existing variable verification has been externally changed to an invalid syntax.", e);
            }
            consumedVariables.put(result.getFullName(), result);
        }
        return result;
    }

    public ContextualVariable getVariable(String name) {
        ContextualVariable result = consumedVariables.get(name);
        if (result == null) {
            Variable variable = parameter(name, Variable.class);
            if (variable == null) {
                return null;
            }

            try {
                result = new ContextualVariable(variable, this, consumedArgumentSet.getConsumedArguments(variable));
            } catch (IllegalVerificationException e) {
                throw new IllegalStateException("Existing variable verification has been externally changed to an invalid syntax.", e);
            }
            consumedVariables.put(result.getName(), result);
        }
        return result;
    }

    @Deprecated
    protected <P extends Parameter> P parameter(int index, Class<P> paramType) {
        Parameter parameter = parameter(index);
        if (parameter != null && paramType.isAssignableFrom(parameter.getClass())) {
            return (P) parameter;
        }
        return null;
    }

    protected <P extends Parameter> P parameter(String name, Class<P> paramType) {
        Parameter parameter = parameter(name);
        if (parameter != null && paramType.isAssignableFrom(parameter.getClass())) {
            return (P) parameter;
        }
        return null;
    }

    public String var(String name) {
        ContextualVariable variable = getVariable(name);
        return variable != null ? variable.getConsumedValue() : null;
    }

    @Deprecated
    public String var(int index) {
        ContextualVariable variable = getVariable(index);
        return variable != null ? variable.getConsumedValue() : null;
    }

    public String arg(int index) {
        if (index >= getArguments().length) {
            throw new ArrayIndexOutOfBoundsException("Argument " + index + " does not exist.");
        }
        return getArguments()[index];
    }

    public String arg(int index, int startIndex, int endIndex) {
        return arg(index, new Range(startIndex, endIndex));
    }

    public String arg(int index, Range argumentRange) {
        String[] arguments = args(argumentRange);
        if (index >= getArguments().length) {
            throw new ArrayIndexOutOfBoundsException("Argument " + index + " does not exist in range "  + argumentRange.getStartIndex() + "-" + argumentRange.getEndIndex() + ".");
        }
        return arguments[index];
    }

    public String[] args(int startIndex, int endIndex) {
        return args(new Range(startIndex, endIndex));
    }

    public String[] args(Range argumentRange) {
        if (argumentRange.getStartIndex() >= argsLength()) {
            throw new ArrayIndexOutOfBoundsException("Arguments in given range (" + argumentRange + ") do not exist.");
        }

        int endIndex = argumentRange.getEndIndex() + 1;
        String[] result = argumentSets.get(argumentRange);
        if (result == null || result.length == 0) {
            result = Arrays.copyOfRange(getArguments(), argumentRange.getStartIndex(), endIndex >= argsLength() ? argsLength() : endIndex);
            argumentSets.put(argumentRange, result);
        }
        return result;
    }
}