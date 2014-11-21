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

import com.dsh105.commodus.StringUtil;
import com.dsh105.influx.dispatch.CommandContext;

public class ContextualVariable extends Variable {

    private CommandContext<?> context;
    private String[] value;

    public ContextualVariable(Variable variable, CommandContext<?> context, String... consumedArguments) throws IllegalVerificationException {
        super(variable.getName(), variable.getRange(), variable.getRegex(), variable.isOptional(), variable.isContinuous(), variable.getDefaultValue(), variable.getArgumentsAccepted());
        this.context = context;

        if (this.isContinuous()) {
            this.range = new Range(getRange().getStartIndex(), context.getArguments().length);
        }
        this.value = consumedArguments;
    }

    public CommandContext<?> getContext() {
        return context;
    }

    public String[] getConsumedArguments() {
        return value;
    }

    public String getConsumedValue() {
        return StringUtil.combineArray(" ", getConsumedArguments());
    }
}