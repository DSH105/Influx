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

package com.dsh105.influx.help;

import com.dsh105.commodus.StringUtil;
import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.syntax.Command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Suggestion {

    private InfluxManager<?> manager;
    private String input;
    private ArrayList<String> suggestions;

    public Suggestion(InfluxManager<?> manager, String input) {
        this.manager = manager;
        this.input = input;
        this.suggestions = new ArrayList<>();

        for (Controller controller : getManager().getMappedCommands()) {
            if (!this.suggestions.contains(controller.getCommand().getReadableSyntax())) {
                Command command = controller.getCommand();
                String[] parts = command.getStringSyntax().split("\\s+");
                String[] parts2 = command.getReadableSyntax().split("\\s+");

                int maxIndex = command.getIndexOf(command.getFirstVariable(), false);
                if (maxIndex < 0) maxIndex = parts.length;
                int maxIndex2 = command.getIndexOf(command.getFirstVariable(), false);
                if (maxIndex2 < 0) maxIndex2 = parts2.length;

                String suggestion = StringUtil.combineArray(0, maxIndex, " ", parts);
                String suggestion2 = StringUtil.combineArray(0, maxIndex2, " ", parts);
                if (getInput().startsWith(suggestion) || getInput().startsWith(suggestion2)) {
                    this.suggestions.add(command.getReadableSyntax());
                }
            }
        }
    }

    public InfluxManager<?> getManager() {
        return manager;
    }

    public String getInput() {
        return input;
    }

    public List<String> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }
}