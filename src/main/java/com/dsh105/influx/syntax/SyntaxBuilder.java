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
import com.dsh105.influx.syntax.parameter.ParameterBinding;
import com.dsh105.influx.syntax.parameter.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxBuilder {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(<|\\[)([^<>\\[\\]]+?)(>|\\])");

    private CommandBinding commandBinding;
    private String commandSyntax;
    private String[] arguments;
    private List<Parameter> parameters;

    public SyntaxBuilder(String commandSyntax) {
        this(commandSyntax, null);
    }

    public SyntaxBuilder(String commandSyntax, CommandBinding commandBinding) {
        this.commandBinding = commandBinding;
        this.commandSyntax = commandSyntax;
        this.arguments = this.commandSyntax.split("\\s+");
    }

    public CommandBinding getCommandBinding() {
        return commandBinding;
    }

    public String getCommandSyntax() {
        return commandSyntax;
    }

    public String[] getArguments() {
        return arguments;
    }

    public List<Parameter> getParameters() {
        return build();
    }

    public List<Parameter> build() {
        if (parameters == null) {
            parameters = new ArrayList<>();

            for (int i = 0; i < arguments.length; i++) {
                String argument = arguments[i];
                Parameter parameter;
                ParameterBinding binding = getCommandBinding() != null ? getCommandBinding().getBinding(i) : null;

                Matcher variableMatcher = VARIABLE_PATTERN.matcher(argument);
                if (variableMatcher.matches()) {
                    parameter = buildVariable(variableMatcher, binding, i);
                } else {
                    List<Variable> innerVariables = new ArrayList<>();
                    while (variableMatcher.find()) {
                        innerVariables.add(buildVariable(variableMatcher, binding, i));
                    }
                    parameter = new Parameter(argument, i, innerVariables);
                }

                parameters.add(i, parameter);
            }
        }
        return parameters;
    }

    private Variable buildVariable(Matcher matcher, ParameterBinding binding, int index) {
        return new Variable(matcher.group(2), index, binding == null ? "" : binding.getRegex(), matcher.group(1).equals("["), index == arguments.length - 1 && matcher.group(2).endsWith("..."));
    }
}