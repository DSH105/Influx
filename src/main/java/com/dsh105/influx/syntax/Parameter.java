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
import java.util.Collections;
import java.util.List;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Parameter {

    private String name;
    protected Range range;
    private boolean containsInnerVariables;
    private List<Variable> innerVariables;

    public Parameter(String name, int index) {
        this(name, new Range(index));
    }

    public Parameter(String name, Range range) {
        this(name, range, new ArrayList<Variable>());
    }

    public Parameter(String name, int index, List<Variable> innerVariables) {
        this(name, new Range(index), innerVariables);
    }

    public Parameter(String name, Range range, List<Variable> innerVariables) {
        this.name = name;
        this.range = range;
        this.containsInnerVariables = true;
        this.innerVariables = innerVariables;
    }

    public String getFullName() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public Range getRange() {
        return range;
    }

    public List<Variable> getInnerVariables() {
        return Collections.unmodifiableList(innerVariables);
    }

    public boolean containsInnerVariables() {
        return containsInnerVariables;
    }

    public boolean verify(String parameter) {
        return getName().equalsIgnoreCase(parameter);
    }

    public boolean isOptional() {
        return false;
    }

    public boolean isContinuous() {
        return false;
    }

    @Override
    public String toString() {
        return "Parameter{" +
                "name='" + name + "" +
                ", range=" + range +
                ", containsInnerVariables=" + containsInnerVariables +
                ", innerVariables=" + innerVariables +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parameter)) return false;

        Parameter parameter = (Parameter) o;

        if (containsInnerVariables != parameter.containsInnerVariables) return false;
        if (!innerVariables.equals(parameter.innerVariables)) return false;
        if (!name.equals(parameter.name)) return false;
        if (!range.equals(parameter.range)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + range.hashCode();
        result = 31 * result + (containsInnerVariables ? 1 : 0);
        result = 31 * result + innerVariables.hashCode();
        return result;
    }
}