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

import com.google.common.base.Preconditions;

public class Range {

    private int startIndex;
    private int endIndex;

    public Range(int index) {
        this(index, index);
    }

    public Range(int startIndex, int endIndex) {
        Preconditions.checkArgument(startIndex >= 0, "Invalid index for range: " + startIndex);
        Preconditions.checkArgument(endIndex >= 0, "Invalid index for range: " + endIndex);
        Preconditions.checkArgument(startIndex <= endIndex, String.format("endIndex (%s) must be greater than startIndex (%s)", endIndex, startIndex));
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getSize() {
        return getEndIndex() - getStartIndex();
    }

    public boolean isBefore(Range range) {
        return getStartIndex() < range.getStartIndex();
    }

    public boolean isAfter(Range range) {
        return getStartIndex() > range.getStartIndex();
    }

    @Override
    public String toString() {
        return "Range{" +
                "startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                "}";
    }

    @Override
    public int hashCode() {
        int result = startIndex;
        result = 31 * result + endIndex;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        return endIndex == range.endIndex && startIndex == range.startIndex;

    }
}