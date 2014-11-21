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

package com.dsh105.influx.util;

import java.util.HashMap;
import java.util.Map;

public class Primitive {

    private Primitive() {

    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = new HashMap<>();
    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMIIVE = new HashMap<>();

    static {
        add(boolean.class, Boolean.class);
        add(int.class, Integer.class);
        add(short.class, Short.class);
        add(float.class, Float.class);
        add(double.class, Double.class);
        add(long.class, Long.class);
        add(byte.class, Byte.class);
        add(char.class, Character.class);
        add(void.class, Void.class);
    }

    private static void add(Class<?> primitive, Class<?> wrapper) {
        PRIMITIVE_TO_WRAPPER.put(primitive, wrapper);
        WRAPPER_TO_PRIMIIVE.put(wrapper, primitive);
    }

    public static <T> Class<T> wrap(Class<T> primitive) {
        Class<T> wrapper = (Class<T>) PRIMITIVE_TO_WRAPPER.get(primitive);
        return wrapper != null ? wrapper : primitive;
    }

    public static <T> Class<T> unwrap(Class<T> wrapper) {
        Class<T> primitive = (Class<T>) WRAPPER_TO_PRIMIIVE.get(wrapper);
        return primitive != null ? primitive : wrapper;
    }
}