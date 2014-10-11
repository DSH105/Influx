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

package com.dsh105.influx.response;

public enum MessagePurpose {

    PAGE_NOT_FOUND("{c1}Page {c2}<page> {c1}not found."),
    COMMAND_NOT_FOUND("{c1}Command does not exist: {c2}<command>{c2}"),
    RESTRICTED_SENDER("{c1}Access to this command is restricted."),
    RESTRICTED_PERMISSION("{c1}You are not permitted to do that."),
    RESTRICTED_PERMISSION_WITH_VAR_RECOMMENDATION("{c1}You are not permitted to do that. Maybe a variable was invalid?"),
    UNEXPECTED_ERROR("{c1}Something unexpected happened. Please see the console for any errors and report them immediately."),
    INVALID_ARGUMENT_NUMBER("{c1}Invalid number of arguments (<args>). Must be {c2}<range>{c1}."),
    SUGGESTIONS("Did you mean: {c2}<suggestions>"),
    NO_HELP_FOUND("No help for {c2}<command>"),

    BUKKIT_HELP_HEADER("&e---- &6Help: &e--&6 <topic>&e"),
    BUKKIT_SHORT_HELP_ENTRY("{c2}<command>"),
    BUKKIT_HELP_ENTRY("{c2}<command> {c1}(<alias_num>) - <short_desc>"),
    BUKKIT_ENTRY_HELP_TITLE("{c2}<command> {c1}(<alias_num>):"),
    BUKKIT_EXPANDED_HELP_DESCRIPTION_PART("&r• <desc>");

    private String defaultValue;

    MessagePurpose(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}