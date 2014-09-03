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

import com.dsh105.influx.annotation.*;
import com.dsh105.influx.context.CommandEvent;
import com.dsh105.influx.syntax.parameter.Parameter;
import com.dsh105.influx.syntax.parameter.ParameterBinding;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandBinding {

    private Class<?>[] parameters;
    private Map<Integer, ParameterBinding> bindings = new HashMap<>();
    private Method callableMethod;

    public Method getCallableMethod() {
        if (callableMethod == null) {
            prepare();
        }
        return callableMethod;
    }

    public Class<?>[] getParameters() {
        if (parameters == null) {
            getCallableMethod();
        }
        return parameters;
    }

    public Map<Integer, ParameterBinding> getBindings() {
        if (bindings == null) {
            getParameters();
        }
        return Collections.unmodifiableMap(bindings);
    }

    public ParameterBinding getBinding(int index) {
        return getBindings().get(index);
    }

    public ParameterBinding getBinding(Parameter parameter) {
        for (ParameterBinding binding : getBindings().values()) {
            if (binding.getBoundParameter().equals(parameter.getName())) {
                return binding;
            }
        }
        return null;
    }

    public static boolean isValid(Method candidate) {
        return candidate.isAnnotationPresent(com.dsh105.influx.annotation.Command.class) && candidate.getReturnType().equals(boolean.class) && candidate.getParameterTypes().length >= 1 && CommandEvent.class.isAssignableFrom(candidate.getParameterTypes()[0]);
    }

    private void prepare() {
        callableMethod = prepareCallable();
        parameters = callableMethod.getParameterTypes();
        bindings = prepareBindings();
    }

    public abstract Method prepareCallable();

    public abstract Map<Integer, ParameterBinding> prepareBindings();
}