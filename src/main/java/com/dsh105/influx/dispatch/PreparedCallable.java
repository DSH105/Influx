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
import com.dsh105.influx.context.CommandEvent;
import com.dsh105.influx.converter.Converter;
import com.dsh105.influx.syntax.parameter.EventVariable;
import com.dsh105.influx.syntax.parameter.ParameterBinding;

import java.lang.reflect.InvocationTargetException;

public class PreparedCallable extends CommandCallable {

    public PreparedCallable(Converter... converters) {
        super(converters);
    }

    @Override
    public boolean call(CommandEvent event) throws CommandInvocationException {
        Controller controller = event.getController();
        Object[] methodParameters = controller.getCommandBinding().getParameters();
        Object[] parameters = new Class[methodParameters.length];

        if (methodParameters.length != 0) {
            for (int i = 1; i < methodParameters.length; i++) {
                for (Class<?> converterType : getConverters().keySet()) {
                    ParameterBinding parameterBinding = controller.getCommandBinding().getBinding(i);
                    if (converterType.isAssignableFrom(parameterBinding.getBindingType())) {
                        EventVariable variable = event.variable(parameterBinding.getBoundParameter());
                        Converter converter;
                        try {
                            converter = parameterBinding.getConverter().newInstance();
                        } catch (InstantiationException | IllegalAccessException ignored) {
                            converter = getConverters().get(converterType);
                        }
                        parameters[i] = variable == null || converter == null ? null : getConverters().get(converterType).safelyConvert(variable);
                        break;
                    }
                }
            }
        }

        try {
            return (boolean) controller.getCommandBinding().getCallableMethod().invoke(controller.getCommand().getListener(), event, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CommandInvocationException(e);
        }
    }
}