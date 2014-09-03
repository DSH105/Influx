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

package com.dsh105.influx.format;

public enum MessagePurpose {

    PAGE_NOT_FOUND("{c1}Page {c2}%s {c1}not found."),
    COMMAND_NOT_FOUND("{c1}Command does not exist: <command>"),
    NO_ACCESS("{c1}Access not permitted from here. Try logging in."),
    NO_PERMISSION("{c1}You are not permitted to do that."),
    NO_PERMISSION_WITH_VAR_RECOMMENDATION("{c1}You are not permitted to do that. Maybe a variable was invalid?"),
    UNEXPECTED_ERROR("{c1}Something unexpected happened. Please see the console for any errors and report them immediately.");

    private String defaultValue;

    MessagePurpose(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}