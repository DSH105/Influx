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

import com.dsh105.influx.help.HelpProvider;
import com.google.common.base.Preconditions;

import java.util.Arrays;

public class Description {

    private String shortDescription;
    private String[] longDescription;
    private String[] usage;
    private String helpGroup = HelpProvider.DEFAULT;

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

    public String getHelpGroup() {
        return helpGroup;
    }

    public void setHelpGroup(String helpGroup) {
        this.helpGroup = helpGroup != null ? helpGroup : HelpProvider.DEFAULT;
    }

    @Override
    public String toString() {
        return "Description{" +
                "shortDescription='" + shortDescription + "'" +
                ", longDescription=" + Arrays.toString(longDescription) +
                ", usage=" + Arrays.toString(usage) +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Description)) return false;

        Description that = (Description) o;

        if (!Arrays.equals(longDescription, that.longDescription)) return false;
        if (!shortDescription.equals(that.shortDescription)) return false;
        if (!Arrays.equals(usage, that.usage)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = shortDescription.hashCode();
        result = 31 * result + Arrays.hashCode(longDescription);
        result = 31 * result + Arrays.hashCode(usage);
        return result;
    }
}