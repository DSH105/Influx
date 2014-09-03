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

package com.dsh105.influx.syntax.parameter;

import com.dsh105.influx.annotation.Bind;
import com.dsh105.influx.converter.Converter;

public class ParameterBinding {

    private Class<?> bindingType;
    private String boundParameter;
    private String regex;
    private Class<? extends Converter> converter;

    public ParameterBinding(Class<?> bindingType, String boundParameter, String regex, Class<? extends Converter> converter) {
        this.bindingType = bindingType;
        this.boundParameter = boundParameter;
        this.regex = regex;
        this.converter = converter;
    }

    public Class<?> getBindingType() {
        return bindingType;
    }

    public String getBoundParameter() {
        return boundParameter;
    }

    public String getRegex() {
        return regex;
    }

    public Class<? extends Converter> getConverter() {
        return converter;
    }
}