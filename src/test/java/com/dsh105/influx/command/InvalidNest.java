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

package com.dsh105.influx.command;

import com.dsh105.influx.CommandListener;
import com.dsh105.influx.annotation.Bind;
import com.dsh105.influx.annotation.Command;
import com.dsh105.influx.dispatch.CommandContext;

public class InvalidNest implements CommandListener {

    @Command(
            syntax = "influx void",
            desc = "A command with an invalid return type"
    )
    public void aVoid(CommandContext context) {

    }

    @Command(
            syntax = "influx context",
            desc = "A command without a context parameter type"
    )
    public boolean missingContext() {
        return true;
    }

    @Command(
            syntax = "influx <var> [var]",
            desc = "A command with an invalid return type"
    )
    public boolean duplicateVariables(CommandContext context) {
        return true;
    }

    @Command(
            syntax = "influx [continuously...] invalid",
            desc = "A command with an invalid return type"
    )
    public boolean invalidContinuousParameter(CommandContext context) {
        return true;
    }
}