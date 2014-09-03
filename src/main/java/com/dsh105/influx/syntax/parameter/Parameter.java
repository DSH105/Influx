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

import com.dsh105.influx.syntax.Range;

import java.util.Collections;
import java.util.List;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Parameter implements Comparable<Parameter> {

    private String name;
    protected Range range;
    private boolean containsInnerVariables;

    private List<Variable> innerVariables;

    public Parameter(String name, int index) {
        this(name, new Range(index));
    }

    public Parameter(String name, Range range) {
        this.name = name;
        this.range = range;
    }

    public Parameter(String name, int index, List<Variable> innerVariables) {
        this(name, new Range(index), innerVariables);
    }

    public Parameter(String name, Range range, List<Variable> innerVariables) {
        this(name, range);
        this.containsInnerVariables = true;
        this.innerVariables = innerVariables;
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

    @Override
    public int compareTo(Parameter parameter) {
        if (parameter instanceof Variable) {
            return -(parameter.compareTo(this));
        }
        return 0;
    }
}