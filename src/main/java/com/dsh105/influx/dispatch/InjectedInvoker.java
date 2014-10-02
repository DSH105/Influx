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

package com.dsh105.influx.dispatch;

import com.dsh105.influx.Controller;
import com.dsh105.influx.conversion.ConversionException;
import com.dsh105.influx.conversion.Converter;
import com.dsh105.influx.syntax.ContextualVariable;
import com.dsh105.influx.syntax.ParameterBinding;
import com.google.common.primitives.Primitives;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static com.dsh105.influx.conversion.Converters.*;

public class InjectedInvoker extends CommandInvoker {

    private static final Converter[] PRIMITIVE_CONVERTERS = {new BooleanConverter(), new ByteConverter(), new CharacterConverter(), new NumberConverter()};

    public InjectedInvoker(Converter... converters) {
        super(converters);
    }

    @Override
    public boolean invoke(CommandContext context) throws CommandInvocationException, ConversionException {
        Controller controller = context.getController();
        Class<?>[] methodParameters = controller.getCommandBinding().getParameters();
        Object[] parameters = new Object[methodParameters.length];
        Arrays.fill(parameters, null);

        if (!methodParameters[0].isAssignableFrom(context.getClass())) {
            // e.g. someMethod(CommandEvent) -> where `context` is not an instance of CommandEvent
            throw new CommandInvocationException("Method does not accept provided context (accepts " + methodParameters[0].getClass().getCanonicalName() + ", but provided " + context.getClass().getSimpleName() + ")");
        }

        parameters[0] = context;

        if (methodParameters.length != 0) {

            parameterIteration: {
                for (int i = 1; i < methodParameters.length; i++) {
                    Class<?> methodParameter = methodParameters[i];

                    ParameterBinding parameterBinding = controller.getCommandBinding().getBinding(i);
                    ContextualVariable variable = context.getVariable(parameterBinding.getBoundParameter());
                    if (variable == null) {
                        throw new CommandInvocationException("Parameter binding requested an invalid variable: " + parameterBinding.getBoundParameter());
                    }

                    if (methodParameter.isAssignableFrom(variable.getConsumedValue().getClass())) {
                        parameters[i] = variable.getConsumedValue();
                        break;
                    }

                    for (Converter<?> converter : PRIMITIVE_CONVERTERS) {
                        if (converter.getParameterType().isAssignableFrom(Primitives.wrap(methodParameter))) {
                            try {
                                parameters[i] = converter.safelyConvert(variable);
                                break parameterIteration;
                            } catch (ConversionException ignored) {
                                // Failed to convert
                            }
                        }
                    }

                    Converter converter = null;
                    if (parameterBinding.getConverter() != null) {
                        try {
                            converter = parameterBinding.getConverter().newInstance();
                        } catch (InstantiationException | IllegalAccessException e) {
                            for (Class<?> converterType : getConverters().keySet()) {
                                if (converterType.isAssignableFrom(parameterBinding.getBindingType())) {
                                    converter = getConverters().get(converterType);
                                }
                            }
                        }
                    }
                    if (converter == null && methodParameter.isPrimitive()) {
                        throw new CommandInvocationException("Failed to convert variable (\"" + variable.getName() + "\", consumed \"" + variable.getConsumedValue() + "\") for primitive method parameter type: " + methodParameter);
                    }
                    parameters[i] = converter == null ? null : converter.safelyConvert(variable);
                }
            }

        }

        try {
            return (boolean) controller.getCommandBinding().getCallableMethod().invoke(controller.getCommand().getOriginListener(), parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CommandInvocationException(e);
        }
    }
}