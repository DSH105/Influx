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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandCallable {

    private Map<Class<?>, Converter<?>> converters = new HashMap<>();

    public CommandCallable(Converter... converters) {
        addConverters(converters);
    }

    public Map<Class<?>, Converter<?>> getConverters() {
        return Collections.unmodifiableMap(converters);
    }

    public void addConverters(Converter... converters) {
        for (Converter converter : converters) {
            this.converters.put(converter.getParameterType(), converter);
        }
    }

    public abstract boolean call(CommandEvent event) throws CommandInvocationException;
}