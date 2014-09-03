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
import com.dsh105.influx.Controller;
import com.dsh105.influx.Manager;
import com.dsh105.influx.context.CommandEvent;
import com.dsh105.influx.syntax.Command;
import com.dsh105.influx.syntax.parameter.ParameterException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Suggestion {

    private Manager manager;
    private String input;
    private ArrayList<String> suggestions = new ArrayList<>();

    public Suggestion(Manager manager, String input) {
        this.manager = manager;
        this.input = input;
    }

    public Manager getManager() {
        return manager;
    }

    public String getInput() {
        return input;
    }

    public List<String> getSuggestions() {
        if (suggestions == null) {
            suggestions = new ArrayList<>();

            for (Controller controller : getManager().getMappedCommands()) {
                Command command = controller.getCommand();
                String syntax = command.getStringSyntax();
                String[] parts = syntax.split("\\s+");

                int maxIndex;
                try {
                    maxIndex = command.getIndexOf(command.getFirstVariable());
                } catch (ParameterException e) {
                    maxIndex = parts.length;
                }

                String suggestion = StringUtil.combineArray(0, maxIndex, " ", parts);

                if (!suggestions.contains(syntax)) {
                    if (getInput().startsWith(suggestion)) {
                        suggestions.add(syntax);
                    }
                }
            }
        }
        return Collections.unmodifiableList(suggestions);
    }
}