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

package com.dsh105.influx.help.entry;

import com.dsh105.influx.Controller;
import com.dsh105.influx.Description;
import com.dsh105.influx.help.HelpProvider;

import java.util.List;
import java.util.Set;

public class ExpandedHelpEntry extends HelpEntry {

    public ExpandedHelpEntry(HelpProvider helpProvider, Controller controller) {
        super(helpProvider, controller);
    }

    @Override
    public String toString() {
        return "• {c2}" + getCommandSyntax() + " {c1}(" + getController().getCommand().getAliases().size() + " aliases)" + " - " + getShortDescription();
    }
}