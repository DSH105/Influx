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

import com.dsh105.influx.CommandListener;
import com.dsh105.influx.InvalidCommandException;
import com.dsh105.influx.annotation.Convert;
import com.dsh105.influx.annotation.Verify;
import com.dsh105.influx.converter.Converter;
import com.dsh105.influx.syntax.Command;
import com.dsh105.influx.annotation.Bind;
import com.dsh105.influx.context.CommandEvent;
import com.dsh105.influx.syntax.parameter.Parameter;
import com.dsh105.influx.syntax.parameter.ParameterBinding;
import com.dsh105.influx.syntax.parameter.ParameterException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WrappedCommandMethod extends CommandBinding {

    private CommandListener listener;
    private String methodName;

    public WrappedCommandMethod(CommandListener listener, String methodName) {
        this.listener = listener;
        this.methodName = methodName;
    }

    public CommandListener getListener() {
        return listener;
    }

    @Override
    public Method prepareCallable() {
        Method method = null;
        try {
            method = getListener().getClass().getDeclaredMethod(methodName);
        } catch (NoSuchMethodException ignored) {
            // Ignore...for now
        }

        if (method == null || !isValid(method)) {
            throw new InvalidCommandException("Provided method is invalid: " + getListener().getClass().getName() + "#" + methodName);
        }

        return method;
    }

    @Override
    public Map<Integer, ParameterBinding> prepareBindings() {
        Map<Integer, ParameterBinding> bindings = new HashMap<>();
        if (getParameters().length != 0) {
            for (int i = 1; i < getParameters().length; i++) {
                Class<?> parameter = getParameters()[i];
                String binding = null;
                Class<? extends Converter> converter = null;
                String verification = "";

                for (Annotation annotation : this.getCallableMethod().getParameterAnnotations()[i]) {
                    if (annotation.annotationType() == Bind.class) {
                        binding = ((Bind) annotation).value();
                    } else if (annotation.annotationType() == Convert.class) {
                        converter = ((Convert) annotation).value();
                    } else if (annotation.annotationType() == Verify.class) {
                        verification = ((Verify) annotation).value();
                    }
                }

                if (binding != null) {
                    bindings.put(i, new ParameterBinding(parameter, binding, verification, converter));
                }
            }
        }
        return bindings;
    }
}