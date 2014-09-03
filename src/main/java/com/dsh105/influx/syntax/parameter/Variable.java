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

package com.dsh105.influx.syntax.parameter;

import com.dsh105.influx.context.CommandEvent;
import com.dsh105.influx.syntax.Range;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Variable extends Parameter {

    private String regex;
    private boolean optional;
    private boolean continuous;

    public Variable(String name, int index, String regex, boolean optional) {
        this(name, index, regex, optional, false);
    }

    public Variable(String name, Range range, String regex, boolean optional) {
        this(name, range, regex, optional, false);
    }

    public Variable(String name, int index, String regex, boolean optional, boolean continuous) {
        this(name, new Range(index), regex, optional, continuous);
    }

    public Variable(String name, Range range, String regex, boolean optional, boolean continuous) {
        super(name, range);
        this.regex = regex;
        this.optional = optional;
        this.continuous = continuous;

        try {
            Pattern.compile(this.regex);
        } catch (PatternSyntaxException e) {
            throw new VariableException("Invalid pattern syntax for variable (\"" + name + "\"): \"" + this.regex + "\"", e);
        }
    }

    @Override
    public boolean containsInnerVariables() {
        return false;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public boolean isRegexEnabled() {
        return !getRegex().isEmpty();
    }

    public String getRegex() {
        return regex;
    }

    public boolean verify(String parameter) {
        return !isRegexEnabled() || getRegex().matches(parameter);
    }

    public String getOpeningTag() {
        return isOptional() ? "[" : "<";
    }

    public String getClosingTag() {
        return isOptional() ? "]" : ">";
    }

    public String getFullName() {
        return getOpeningTag() + getName() + getClosingTag();
    }

    public EventVariable prepare(CommandEvent event) {
        return new EventVariable(this, event);
    }

    @Override
    public int compareTo(Parameter parameter) {
        if (parameter instanceof Variable) {
            if (isRegexEnabled() == ((Variable) parameter).isRegexEnabled()) {
                return 0;
            }
            return isRegexEnabled() ? 1 : -1;
        }
        return 1;
    }
}