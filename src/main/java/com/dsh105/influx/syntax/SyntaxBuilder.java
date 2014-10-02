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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxBuilder {

    public static final Pattern VARIABLE_PATTERN = Pattern.compile("(<|\\[)([^<>\\[\\]]+?)(>|\\])");

    private CommandBinding commandBinding;
    private String commandSyntax;
    private String[] arguments;
    private List<Parameter> parameters;

    public SyntaxBuilder(String commandSyntax) throws IllegalSyntaxException {
        this(commandSyntax, null);
    }

    public SyntaxBuilder(String commandSyntax, CommandBinding commandBinding) throws IllegalSyntaxException {
        this.commandBinding = commandBinding;
        this.commandSyntax = commandSyntax;
        this.arguments = this.commandSyntax.split("\\s+");
        this.parameters = new ArrayList<>();

        List<String> variableNames = new ArrayList<>();

        for (int i = 0; i < arguments.length; i++) {
            String argument = arguments[i];
            Parameter parameter;

            Matcher variableMatcher = VARIABLE_PATTERN.matcher(argument);
            if (variableMatcher.matches()) {
                try {
                    parameter = buildVariable(variableMatcher, i);
                } catch (IllegalVerificationException e) {
                    throw new IllegalSyntaxException("Invalid variable: " + variableMatcher.group(0));
                }
                if (variableNames.contains(parameter.getName())) {
                    throw new IllegalSyntaxException("Command variables cannot have identical names. \"" + parameter.getName() + "\" found more than once in \"" + getCommandSyntax() + "\".");
                }
                variableNames.add(parameter.getName());
            } else {
                List<Variable> innerVariables = new ArrayList<>();
                while (variableMatcher.find()) {
                    try {
                        innerVariables.add(buildVariable(variableMatcher, i));
                    } catch (IllegalVerificationException e) {
                        throw new IllegalSyntaxException("Invalid variable: " + variableMatcher.group(0));
                    }
                }
                parameter = new Parameter(argument, i, innerVariables);
            }

            parameters.add(i, parameter);
        }

        for (int i = 0; i < parameters.size() - 1; i++) {
            Parameter parameter = parameters.get(i);
            String reason = null;
            if (parameter.isContinuous()) {
                // 'Continuous' variables cannot exist in the middle of the syntax
                reason = "continuous variables can only exist as the final parameter";
            }/* else if (variable.isOptional() && variable.getDefaultValue() == null) {
                    // Optional variables can only exist in the middle if they have a default value
                    reason = "optional variables can only exist as a middle parameter if a default value is present";
                }*/
            // This isn't really something to warn users of - command will only fire if a value is provided

            if (reason != null) {
                throw new IllegalSyntaxException("Invalid syntax (at pos. " + i + " -> \"" + parameter.getFullName() + "\"): " + reason + ".");
            }
        }
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
        return parameters;
    }

    private Variable buildVariable(Matcher matcher, int index) throws IllegalVerificationException {
        String name = matcher.group(2);
        ParameterBinding binding = getCommandBinding() == null ? null : getCommandBinding().getBinding(name);
        String regex = "";
        String defaultValue = null;
        int argumentsAccepted = 1;
        if (binding != null) {
            regex = binding.getRegex();
            defaultValue = binding.getDefaultValue();
            argumentsAccepted = binding.getArgumentsAccepted();
        }
        return new Variable(name, index, regex, matcher.group(1).equals("["), name.endsWith("..."), defaultValue, argumentsAccepted);
    }
}