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

import java.util.Set;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class HelpEntry implements Comparable<HelpEntry> {

    private Controller controller;
    protected HelpProvider helpProvider;
    protected String[] longDesc;

    public HelpEntry(HelpProvider helpProvider, Controller controller) {
        this.helpProvider = helpProvider;
        this.controller = controller;
    }

    public Controller getController() {
        return controller;
    }

    public void reformat() {
        longDesc = new String[controller.getDescription().getLongDescription().length];
        for (int i = 0; i < controller.getDescription().getLongDescription().length; i++) {
            longDesc[i] = format(controller.getDescription().getLongDescription()[i]);
        }
    }

    public String[] getLongDescription() {
        if (longDesc == null) {
            reformat();
        }
        return longDesc;
    }

    public String getShortDescription() {
        return format(controller.getDescription().getShortDescription());
    }

    public String getCommandSyntax() {
        return controller.getCommand().getAcceptedStringSyntax();
    }

    public Set<String> getPermissions() {
        return controller.getCommand().getPermissions();
    }

    public HelpProvider getHelpProvider() {
        return helpProvider;
    }

    protected String format(String message) {
        return getHelpProvider().getManager().getResponder().format(message);
    }

    @Override
    public int compareTo(HelpEntry helpEntry) {
        return getCommandSyntax().compareTo(helpEntry.getCommandSyntax());
    }
}