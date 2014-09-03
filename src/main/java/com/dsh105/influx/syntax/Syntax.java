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

package com.dsh105.influx.syntax;

import com.dsh105.influx.syntax.parameter.Parameter;
import com.dsh105.influx.syntax.parameter.ParameterException;
import com.dsh105.influx.syntax.parameter.Variable;
import com.google.common.base.Preconditions;

import java.util.*;

public class Syntax implements Comparable<Syntax>, Iterable<Parameter> {

    protected String stringSyntax;
    protected List<Parameter> syntax;
    private CommandBinding commandBinding;
    private Map<String, Parameter> parameterNameMap;

    public Syntax(String stringSyntax) {
        this(stringSyntax, null);
    }

    public Syntax(String stringSyntax, CommandBinding commandBinding) {
        this.commandBinding = commandBinding;
        this.buildSyntax(stringSyntax);
    }

    protected void buildSyntax(String syntax) {
        this.stringSyntax = syntax;
        this.syntax = new SyntaxBuilder(syntax, this.commandBinding).build();
    }

    public String getCommandName() {
        return getStringSyntax().split("\\s")[0];
    }

    public String getStringSyntax() {
        return stringSyntax;
    }

    public List<Parameter> getSyntax() {
        return Collections.unmodifiableList(syntax);
    }

    public CommandBinding getCommandBinding() {
        return commandBinding;
    }

    public int getIndexOf(Parameter parameter) {
        int index = getSyntax().indexOf(parameter);
        if (index < 0) {
            throw new ParameterException("Parameter " + index + " does not exist.");
        }
        return index;
    }

    public List<Variable> getVariables() {
        return getParameters(Variable.class);
    }

    public Variable getFirstVariable() {
        return getVariables().get(0);
    }

    public Variable getFirstVerifiedVariable() {
        for (Variable variable : getParameters(Variable.class)) {
            if (variable.isRegexEnabled()) {
                return variable;
            }
        }
        throw new ParameterException("Command has no regex enabled variables.");
    }

    private <T extends Parameter> List<T> getParameters(Class<T> typeRestriction) {
        List<T> parameters = new ArrayList<>();
        for (Parameter parameter : getSyntax()) {
            if (typeRestriction.isAssignableFrom(parameter.getClass())) {
                parameters.add((T) parameter);
            }
        }
        if (parameters.isEmpty()) {
            throw new ParameterException("Command has no parameters of the following type: " + typeRestriction);
        }
        return Collections.unmodifiableList(parameters);
    }

    public Parameter getParameter(int index) {
        if (index >= getSyntax().size()) {
            throw new ParameterException("Parameter " + index + " does not exist.");
        }

        return getSyntax().get(index);
    }

    public Parameter getParameter(String name) {
        if (parameterNameMap == null) {
            parameterNameMap = new HashMap<>();
            for (Parameter parameter : getSyntax()) {
                parameterNameMap.put(parameter.getName(), parameter);
            }
        }

        Parameter parameter = parameterNameMap.get(name);
        if (parameter == null) {
            throw new ParameterException("Requested parameter is invalid.");
        }
        return parameter;
    }

    @Override
    public int compareTo(Syntax syntax) {
        for (int i = 0; i < getSyntax().size() && i < syntax.getSyntax().size(); i++) {
            int parameterComparison = getSyntax().get(i).compareTo(syntax.getSyntax().get(i));
            if (parameterComparison != 0) {
                return parameterComparison;
            }
        }
        return syntax.getSyntax().size() - getSyntax().size();
    }

    @Override
    public Iterator<Parameter> iterator() {
        return getSyntax().iterator();
    }

    @Override
    public String toString() {
        return stringSyntax;
    }

    public boolean matches(String input) {
        // TODO
    }
}