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

import com.dsh105.influx.conversion.ConversionException;
import com.dsh105.influx.conversion.Converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandInvoker {

    private Map<Class<?>, Converter<?>> converters = new HashMap<>();

    public CommandInvoker(Converter... converters) {
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

    public abstract boolean invoke(CommandContext context) throws CommandInvocationException, ConversionException;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommandInvoker)) return false;

        CommandInvoker that = (CommandInvoker) o;

        if (!converters.equals(that.converters)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return converters.hashCode();
    }
}