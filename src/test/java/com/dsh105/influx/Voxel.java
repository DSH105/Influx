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

package com.dsh105.influx;

import com.dsh105.commodus.GeneralUtil;
import com.dsh105.commodus.StringUtil;
import com.dsh105.influx.conversion.ConversionException;
import com.dsh105.influx.syntax.ContextualVariable;

public class Voxel {

    private int x;
    private int y;
    private int z;

    public Voxel(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    @Override
    public String toString() {
        return "Voxel{x=" + x + ", y=" + y + ", z=" + z + "}";
    }

    public static class Converter extends com.dsh105.influx.conversion.Converter<Voxel> {

        public Converter() {
            super(Voxel.class, 3);
        }

        @Override
        public Voxel convert(ContextualVariable variable) throws ConversionException {
            int[] coordinates = new int[3];
            for (int i = 0; i < 3; i++) {
                try {
                    coordinates[i] = GeneralUtil.toInteger(variable.getConsumedArguments()[i]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    throw new ConversionException("Invalid voxel coordinates provided: " + StringUtil.combineArray(0, 4, ", ", variable.getConsumedArguments()));
                }
            }
            return new Voxel(coordinates[0], coordinates[1], coordinates[2]);
        }
    }
}