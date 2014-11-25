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

package com.dsh105.influx.conversion;

import com.dsh105.influx.response.MessagePurpose;
import com.dsh105.influx.syntax.ContextualVariable;

public abstract class Converter<T> {

    private Class<T> parameterType;
    private int minAcceptedArguments;
    private int maxAcceptedArguments;

    public Converter(Class<T> parameterType) {
        this(parameterType, -1, -1);
    }

    public Converter(Class<T> parameterType, int acceptedArgumentCount) {
        this(parameterType, acceptedArgumentCount, acceptedArgumentCount);
    }

    public Converter(Class<T> parameterType, int minAcceptedArguments, int maxAcceptedArguments) {
        this.parameterType = parameterType;
        this.minAcceptedArguments = minAcceptedArguments;
        this.maxAcceptedArguments = maxAcceptedArguments;
    }

    public Class<T> getParameterType() {
        return parameterType;
    }

    public int getMinAcceptedArguments() {
        return minAcceptedArguments;
    }

    public int getMaxAcceptedArguments() {
        return maxAcceptedArguments;
    }

    public T safelyConvert(ContextualVariable variable) throws ConversionException {
        if (minAcceptedArguments > 0 && maxAcceptedArguments > 0) {
            if (variable.getConsumedArguments().length < minAcceptedArguments || variable.getConsumedArguments().length > maxAcceptedArguments) {
                String range = minAcceptedArguments == maxAcceptedArguments ? minAcceptedArguments + " parameters long" : "between " + minAcceptedArguments + " and " + maxAcceptedArguments;
                throw new ConversionException(variable.getContext().getManager().getMessage(MessagePurpose.INVALID_ARGUMENT_NUMBER, "<args>", variable.getConsumedArguments().length, "<range>", range));
            }
        }
        return convert(variable);
    }

    public abstract T convert(ContextualVariable variable) throws ConversionException;
}