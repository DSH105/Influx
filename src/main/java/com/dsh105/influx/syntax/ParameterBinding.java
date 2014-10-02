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

import com.dsh105.influx.conversion.Converter;
import com.google.common.base.Preconditions;

public class ParameterBinding {

    private Class<?> bindingType;
    private String boundParameter;
    private String alternateName;
    private String regex;
    private Class<? extends Converter> converter;
    private int argumentsAccepted;
    private String defaultValue;

    ParameterBinding(Class<?> bindingType, String boundParameter, String alternateName, String regex, Class<? extends Converter> converter, int argumentsAccepted, String defaultValue) {
        this.bindingType = bindingType;
        this.boundParameter = boundParameter;
        this.alternateName = alternateName != null ? alternateName : "";
        this.regex = regex;
        this.converter = converter;
        this.argumentsAccepted = argumentsAccepted;
        this.defaultValue = defaultValue;
    }

    public Class<?> getBindingType() {
        return bindingType;
    }

    public String getBoundParameter() {
        return boundParameter;
    }

    public String getAlternateName() {
        return alternateName;
    }

    public String getRegex() {
        return regex;
    }

    public Class<? extends Converter> getConverter() {
        return converter;
    }

    public int getArgumentsAccepted() {
        return argumentsAccepted;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public static class Builder {

        private Class<?> bindingType;
        private String boundParameter;
        private String alternateName;
        private String regex = "";
        private Class<? extends Converter> converter;
        private int argumentsAccepted = 1;
        private String defaultValue;

        public Builder() {
        }

        public Builder ofType(Class<?> bindingType) {
            this.bindingType = bindingType;
            return this;
        }

        public Builder bind(String boundParameter) {
            this.boundParameter = boundParameter;
            return this;
        }

        public Builder showAs(String alternateName) {
            this.alternateName = alternateName;
            return this;
        }

        public Builder verify(String regex) {
            this.regex = regex;
            return this;
        }

        public Builder convertUsing(Class<? extends Converter> converter) {
            this.converter = converter;
            return this;
        }

        public Builder accept(int argumentsAccepted) {
            this.argumentsAccepted = argumentsAccepted;
            return this;
        }

        public Builder withDefault(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Class<?> getBindingType() {
            return bindingType;
        }

        public String getBoundParameter() {
            return boundParameter;
        }

        public String getAlternateName() {
            return alternateName;
        }

        public String getRegex() {
            return regex;
        }

        public Class<? extends Converter> getConverter() {
            return converter;
        }

        public int getArgumentsAccepted() {
            return argumentsAccepted;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public ParameterBinding build() {
            Preconditions.checkNotNull(bindingType, "Binding type must not be null.");
            Preconditions.checkNotNull(boundParameter, "Bound parameter must not be null.");
            return new ParameterBinding(bindingType, boundParameter, alternateName, regex, converter, argumentsAccepted, defaultValue);
        }
    }
}