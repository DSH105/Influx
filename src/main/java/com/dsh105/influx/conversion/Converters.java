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

import com.dsh105.commodus.GeneralUtil;
import com.dsh105.influx.syntax.ContextualVariable;

import java.util.Arrays;
import java.util.List;

public class Converters {

    private Converters() {
    }

    public static class NumberConverter extends Converter<Number> {

        public NumberConverter() {
            super(Number.class);
        }

        @Override
        public Number convert(ContextualVariable variable) throws ConversionException {
            try {
                return GeneralUtil.toInteger(variable.getConsumedValue());
            } catch (NumberFormatException e) {
                throw new ConversionException("\"" + variable.getConsumedValue() + "\" is not a number.");
            }
        }
    }

    public static class ByteConverter extends Converter<Byte> {

        public ByteConverter() {
            super(Byte.class);
        }

        @Override
        public Byte convert(ContextualVariable variable) throws ConversionException {
            try {
                return Byte.parseByte(variable.getConsumedValue());
            } catch (NumberFormatException e) {
                throw new ConversionException("\"" + variable.getConsumedValue() + "\" is not a number.");
            }
        }
    }

    public static class CharacterConverter extends Converter<Character> {

        public CharacterConverter() {
            super(Character.class);
        }

        @Override
        public Character convert(ContextualVariable variable) throws ConversionException {
            return variable.getConsumedValue().charAt(0);
        }
    }

    public static class BooleanConverter extends Converter<Boolean> {

        private List<String> trueFlags = Arrays.asList("true", "yes", "on");
        private List<String> falseFlags = Arrays.asList("false", "no", "off");

        public BooleanConverter() {
            super(Boolean.class);
        }

        @Override
        public Boolean convert(ContextualVariable variable) throws ConversionException {
            if (trueFlags.contains(variable.getConsumedValue().toLowerCase())) {
                return true;
            } else if (falseFlags.contains(variable.getConsumedValue().toLowerCase())) {
                return false;
            }
            throw new ConversionException("\"" + variable.getConsumedValue() + "\" is not a valid flag.");
        }
    }
}