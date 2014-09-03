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

package com.dsh105.influx.syntax.parameter;

import com.dsh105.commodus.StringUtil;
import com.dsh105.influx.context.CommandEvent;
import com.dsh105.influx.syntax.Range;

public class EventVariable extends Variable {

    private CommandEvent event;
    private String[] value;

    public EventVariable(Variable variable, CommandEvent event) {
        super(variable.getName(), variable.getRange(), variable.getRegex(), variable.isOptional());
        this.event = event;

        if (this.isContinuous()) {
            this.range = new Range(getRange().getStartIndex(), event.getArguments().length);
        }
    }

    public CommandEvent getEvent() {
        return event;
    }

    public String[] getValue() {
        if (this.value == null) {
            this.value = event.args(getRange());
        }
        return value;
    }

    public String getCombinedValue() {
        return StringUtil.combineArray(" ", getValue());
    }
}