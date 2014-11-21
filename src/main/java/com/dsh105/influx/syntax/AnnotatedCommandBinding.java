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
import com.dsh105.influx.IllegalCommandException;
import com.dsh105.influx.annotation.*;
import com.dsh105.influx.dispatch.CommandContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedCommandBinding extends CommandBinding {

    private CommandListener originListener;
    private String methodName;
    private Class<?>[] methodParameters;

    public AnnotatedCommandBinding(CommandListener originListener, String methodName, Class<?>... methodParameters) throws IllegalCommandException {
        this.originListener = originListener;
        this.methodName = methodName;
        this.methodParameters = methodParameters;
        prepare();
    }

    public CommandListener getOriginListener() {
        return originListener;
    }

    @Override
    public Method prepareCallable() throws IllegalCommandException {
        Method method = null;
        try {
            method = getOriginListener().getClass().getDeclaredMethod(methodName, methodParameters);
        } catch (NoSuchMethodException ignored) {
            // Ignore...for now
        }

        if (method == null || !isValid(method)) {
            throw new IllegalCommandException("Provided method is invalid: " + getOriginListener().getClass().getName() + "#" + methodName);
        }

        return method;
    }

    @Override
    public void prepareBindings() {
        for (int i = 1; i < getParameters().length; i++) {
            Class<?> parameter = getParameters()[i];
            ParameterBinding.Builder bindingBuilder = new ParameterBinding.Builder().ofType(parameter);

            for (Annotation annotation : this.getCallableMethod().getParameterAnnotations()[i]) {
                if (annotation instanceof Bind) {
                    bindingBuilder.bind(((Bind) annotation).value());
                } else if (annotation instanceof Convert) {
                    bindingBuilder.convertUsing(((Convert) annotation).value());
                } else if (annotation instanceof Verify) {
                    bindingBuilder.verify(((Verify) annotation).value());
                } else if (annotation instanceof Accept) {
                    Accept accept = (Accept) annotation;
                    bindingBuilder.accept(accept.value());
                    if (!accept.showAs().isEmpty()) {
                        bindingBuilder.showAs(accept.showAs());
                    }
                } else if (annotation instanceof Default) {
                    bindingBuilder.withDefault(((Default) annotation).value());
                }
            }

            bind(i, bindingBuilder.build(), bindingBuilder.getBoundParameter() == null);
        }
    }

    public static boolean isValid(Method candidate) {
        return candidate.isAnnotationPresent(com.dsh105.influx.annotation.Command.class) && candidate.getReturnType().equals(boolean.class) && candidate.getParameterTypes().length >= 1 && CommandContext.class.isAssignableFrom(candidate.getParameterTypes()[0]);
    }
}