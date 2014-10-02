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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Variable extends Parameter {

    private String regex;
    private boolean optional;
    private boolean continuous;
    private String defaultValue;
    private int argumentsAccepted;

    public Variable(String name, int index, String regex, boolean optional, String defaultValue, int argumentsAccepted) throws IllegalVerificationException {
        this(name, index, regex, optional, false, defaultValue, argumentsAccepted);
    }

    public Variable(String name, Range range, String regex, boolean optional, String defaultValue, int argumentsAccepted) throws IllegalVerificationException {
        this(name, range, regex, optional, false, defaultValue, argumentsAccepted);
    }

    public Variable(String name, int index, String regex, boolean optional, boolean continuous, String defaultValue, int argumentsAccepted) throws IllegalVerificationException {
        this(name, new Range(index, index + argumentsAccepted), regex, optional, continuous, defaultValue, argumentsAccepted);
    }

    public Variable(String name, Range range, String regex, boolean optional, boolean continuous, String defaultValue, int argumentsAccepted) throws IllegalVerificationException {
        super(continuous ? name.substring(0, name.length() - 3) : name, range);
        this.regex = regex;
        this.optional = optional;
        this.continuous = continuous;
        this.defaultValue = defaultValue;
        this.argumentsAccepted = argumentsAccepted;

        try {
            Pattern.compile(this.regex);
        } catch (PatternSyntaxException e) {
            throw new IllegalVerificationException("Invalid pattern syntax for variable (\"" + name + "\"): \"" + this.regex + "\"", e);
        }
    }

    @Override
    public boolean containsInnerVariables() {
        return false;
    }

    @Override
    public String getFullName() {
        return getOpeningTag() + getName() + getClosingTag();
    }

    @Override
    public boolean verify(String parameter) {
        return parameter.split("\\s+").length == getArgumentsAccepted() && (!isRegexEnabled() || parameter.matches(getRegex()));
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean isContinuous() {
        return continuous;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public int getArgumentsAccepted() {
        return argumentsAccepted;
    }

    public boolean isRegexEnabled() {
        return !getRegex().isEmpty();
    }

    public String getRegex() {
        return regex;
    }

    public String getOpeningTag() {
        return isOptional() ? "[" : "<";
    }

    public String getClosingTag() {
        return isOptional() ? "]" : ">";
    }

    @Override
    public int compareTo(Parameter parameter) {
        if (parameter instanceof Variable) {
            int paramComparison = super.compareTo(parameter);
            if (paramComparison != 0) {
                return paramComparison;
            }
            if (isRegexEnabled() == ((Variable) parameter).isRegexEnabled()) {
                return 0;
            }
            return isRegexEnabled() ? -1 : 1;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable)) return false;
        if (!super.equals(o)) return false;

        Variable variable = (Variable) o;

        if (continuous != variable.continuous) return false;
        if (optional != variable.optional) return false;
        if (!regex.equals(variable.regex)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + regex.hashCode();
        result = 31 * result + (optional ? 1 : 0);
        result = 31 * result + (continuous ? 1 : 0);
        return result;
    }
}