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

public class Description {

    private String shortDescription;
    private String[] longDescription;
    private String[] usage;

    public Description(String shortDescription, String[] longDescription, String[] usage) {
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.usage = usage;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String[] getLongDescription() {
        return longDescription;
    }

    public String[] getUsage() {
        return usage;
    }

    public String getShortUsage() {
        if (getUsage().length <= 0) {
            return "";
        }

        return getUsage()[0];
    }
}