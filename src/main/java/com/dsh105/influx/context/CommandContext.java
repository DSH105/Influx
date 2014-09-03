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
import com.dsh105.influx.syntax.Command;
import com.dsh105.influx.syntax.parameter.Parameter;
import com.dsh105.influx.syntax.parameter.ParameterException;
import com.dsh105.influx.syntax.parameter.Variable;
import org.bukkit.plugin.Plugin;

public class CommandContext {

    private Manager manager;
    private Controller controller;

    public CommandContext(Manager manager, Controller controller) {
        this.manager = manager;
        this.controller = controller;
    }

    public Manager getManager() {
        return manager;
    }

    public Plugin getPlugin() {
        return getManager().getPlugin();
    }

    public Controller getController() {
        return controller;
    }

    public Command getCommand() {
        return getController().getCommand();
    }

    public Parameter param(int index) {
        return controller.getCommand().getParameter(index);
    }

    public Parameter param(String name) {
        return controller.getCommand().getParameter(name);
    }

    public Variable variable(int index) {
        return param(index, Variable.class);
    }

    public Variable variable(String name) {
        return param(name, Variable.class);
    }

    protected <T extends Parameter> T param(int index, Class<T> paramType) {
        Parameter parameter = param(index);
        if (parameter == null || !(paramType.isAssignableFrom(parameter.getClass()))) {
            throw new ParameterException("Requested parameter is not of the following type: " + paramType);
        }
        return (T) parameter;
    }

    protected <T extends Parameter> T param(String name, Class<T> paramType) {
        Parameter parameter = param(name);
        if (parameter == null || !(paramType.isAssignableFrom(parameter.getClass()))) {
            throw new ParameterException("Requested parameter is not of the following type: " + paramType);
        }
        return (T) parameter;
    }
}