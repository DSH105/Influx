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
import com.dsh105.influx.help.HelpProvider;
import com.dsh105.influx.response.MessagePurpose;

public class BukkitExpandedHelpEntry extends BukkitHelpEntry {

    public BukkitExpandedHelpEntry(HelpProvider helpProvider, Controller controller) {
        super(helpProvider, controller);
    }

    @Override
    public void reformat() {
        super.reformat();
        if (getLongDescription().length != 0) {
            getMessage().tooltip(getLongDescription());
        }
    }

    @Override
    protected String getTemplate() {
        return getHelpProvider().getManager().getMessage(MessagePurpose.BUKKIT_HELP_ENTRY);
    }
}