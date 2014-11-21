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

import com.dsh105.influx.IllegalCommandException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandBinding {

    private Class<?>[] parameters;
    private Map<Integer, ParameterBinding> bindings = new HashMap<>();
    private Map<Integer, ParameterBinding> looseBindings = new HashMap<>();
    private Method callableMethod;

    protected void prepare() throws IllegalCommandException {
        this.callableMethod = prepareCallable();
        this.parameters = callableMethod.getParameterTypes();
        this.prepareBindings();
    }

    public Method getCallableMethod() {
        return callableMethod;
    }

    public Class<?>[] getParameters() {
        return parameters;
    }

    protected void bind(int position, ParameterBinding binding, boolean loose) {
        (loose ? looseBindings : bindings).put(position, binding);
    }

    public Map<Integer, ParameterBinding> getBindings() {
        return getBindings(false);
    }

    public Map<Integer, ParameterBinding> getBindings(boolean loose) {
        return Collections.unmodifiableMap(loose ? looseBindings : bindings);
    }

    public ParameterBinding getBinding(int index) {
        return getBinding(index, false);
    }

    public ParameterBinding getBinding(int index, boolean loose) {
        return getBindings(loose).get(index);
    }

    public ParameterBinding getBinding(String parameterName) {
        return getBinding(parameterName, false);
    }

    public ParameterBinding getBinding(String parameterName, boolean loose) {
        for (ParameterBinding binding : getBindings(loose).values()) {
            if (binding.getBoundParameter().equals(parameterName)) {
                return binding;
            }
        }
        return null;
    }
    public ParameterBinding getBinding(Parameter parameter) {
        return getBinding(parameter, false);
    }

    public ParameterBinding getBinding(Parameter parameter, boolean loose) {
        if (parameter instanceof Variable) {
            return getBinding(parameter.getName() + (parameter.isContinuous() ? "..." : ""), loose);
        }
        return null;
    }

    public abstract Method prepareCallable() throws IllegalCommandException;

    public abstract void prepareBindings();

    @Override
    public String toString() {
        return "CommandBinding{" +
                "parameters=" + Arrays.toString(parameters) +
                ", bindings=" + bindings +
                ", callableMethod=" + callableMethod +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandBinding)) return false;

        CommandBinding that = (CommandBinding) o;

        if (!bindings.equals(that.bindings)) return false;
        if (!callableMethod.equals(that.callableMethod)) return false;
        if (!Arrays.equals(parameters, that.parameters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(parameters);
        result = 31 * result + bindings.hashCode();
        result = 31 * result + callableMethod.hashCode();
        return result;
    }
}