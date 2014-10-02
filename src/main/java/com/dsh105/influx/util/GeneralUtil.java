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

import com.dsh105.commodus.reflection.Reflection;
import com.dsh105.influx.BukkitCommandManager;
import com.dsh105.influx.CommandManager;
import com.dsh105.influx.InfluxBukkitManager;
import com.dsh105.influx.InfluxManager;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class GeneralUtil {

    public static Class<?> getSenderTypeFor(Method method) {
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (Type genericType : genericParameterTypes) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] paramArgTypes = parameterizedType.getActualTypeArguments();
                for (Type paramArgType : paramArgTypes) {
                    if (paramArgType != null) {
                        return (Class<?>) paramArgType;
                    }
                }
            }
        }
        return Object.class;
    }

    public static <T> Class<T> getSenderTypeFor(InfluxManager<T> manager) {
        Class<?> influxManagerClass = manager.getClass();
        while (influxManagerClass != null && influxManagerClass != CommandManager.class) {
            influxManagerClass = influxManagerClass.getSuperclass();
        }
        System.out.println(influxManagerClass.getCanonicalName());

        if (influxManagerClass == null) {
            try {
                return (Class<T>) (manager instanceof InfluxBukkitManager ? InfluxBukkitManager.DEFAULT_CLASS_ACCEPTANCE : Object.class);
            } catch (ClassCastException e) {
                return null; // :(
            }
        }

        Type paramType = ((ParameterizedType) influxManagerClass.getGenericSuperclass()).getActualTypeArguments()[0];
        System.out.println(paramType);
        System.out.println(influxManagerClass.getGenericSuperclass());
        return (Class<T>) paramType;
        /*Type genericInterface = null;
        for (Type generic : manager.getClass().getGenericInterfaces()) {
            System.out.println("------ " + generic.toString());
            if (generic.toString().equals(InfluxManager.class.getCanonicalName())) {
                genericInterface = generic;
            }
        }
        if (genericInterface == null) {
            return Object.class;
        }

        Class<?> paramClass = (Class<?>) genericInterface;

        System.out.println("------ " + paramClass.getCanonicalName());

        Type paramEventType = paramClass.getGenericSuperclass();
        System.out.println("------ " + paramEventType);
        Class<?> event = (Class<?>) ((ParameterizedType) paramEventType).getActualTypeArguments()[0];
        System.out.println("------ " + event.getCanonicalName());*/

        /*System.out.println(manager.getClass().getCanonicalName());
        Class genericClass = manager.getClass();
        classSearch: {
            do {
                for (Class<?> c : genericClass.getInterfaces()) {
                    if (c.equals(InfluxManager.class)) {
                        break classSearch;
                    }
                }
                genericClass = genericClass.getSuperclass();
            } while (genericClass != InfluxManager.class);
        }
        System.out.println(genericClass.getCanonicalName());

        /*Type[] genericTypes = genericClass.getTypeParameters();
        for (Type genericType : genericTypes) {
            System.out.println("Generic type: " + genericType);
            System.out.println(genericType.toString());
            if (genericType instanceof Class) {
                System.out.println("yay");
            }
            System.out.println("Generic type class: " + Reflection.getClass(genericType.toString()));
        }*/

        //Type t = (Type)genericClass;
        /*for (Type genericInterfaceType : genericClass) {
            System.out.println(genericInterfaceType);
        }*/
        /*System.out.println(":O");
        System.out.println(genericClass.getGenericSuperclass());
        System.out.println("HEYYYYYY " + ((ParameterizedType) BukkitCommandManager.class.getGenericSuperclass()).getActualTypeArguments()[0]);
        Type[] genericTypes = ((ParameterizedType) genericClass.getGenericInterfaces()[0]).getActualTypeArguments();
        for (Type genericType : genericTypes) {
            System.out.println("Generic type: " + genericType.toString());
            if (genericType instanceof Class) {
                System.out.println("yay");
            }
        }
        if (genericClass.getGenericSuperclass() instanceof ParameterizedType) {
        }

        ParameterizedType paramType = (ParameterizedType) genericClass.getGenericInterfaces()[0];
        //ParameterizedType paramType = (ParameterizedType) superClass.getGenericSuperclass();
        Class<?> typeArgument = (Class<?>) paramType.getActualTypeArguments()[0];
        System.out.println(typeArgument.getCanonicalName() + " <-------------------");
        return typeArgument != null ? typeArgument : Object.class;*/
    }
}