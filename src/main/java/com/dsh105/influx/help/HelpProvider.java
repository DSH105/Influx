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
import com.dsh105.influx.help.entry.HelpEntry;
import com.dsh105.powermessage.core.PowerMessage;

import java.util.*;

public abstract class HelpProvider<H extends HelpEntry, S> {

    public static String DEFAULT = "default";

    private InfluxManager<S> manager;
    private HelpProvision provision;
    private String header;

    private HashMap<String, List<H>> groupToEntriesMap = new HashMap<>();
    private String defaultEntryListing;

    private boolean restrictByPermission = false;

    protected HelpProvider(InfluxManager<S> manager, HelpProvision provision) {
        this.manager = manager;
        this.provision = provision;

        for (Controller controller : manager) {
            this.add(controller);
        }
    }

    public InfluxManager<S> getManager() {
        return manager;
    }

    public HelpProvision getProvision() {
        return provision;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public boolean restrictByPermission() {
        return restrictByPermission;
    }

    public void setRestrictByPermission(boolean restrictByPermission) {
        this.restrictByPermission = restrictByPermission;
    }

    public List<H> getEntries(String group) {
        List<H> entries = groupToEntriesMap.get(group);
        if (entries == null) {
            entries = new ArrayList<>();
        }
        return Collections.unmodifiableList(entries);
    }

    public List<H> getEntries() {
        List<H> entries = new ArrayList<>();
        for (List<H> group : groupToEntriesMap.values()) {
            entries.addAll(group);
        }
        return Collections.unmodifiableList(entries);
    }

    public abstract H buildHelpEntry(Controller controller);

    public void add(Controller controller) {
        this.add(DEFAULT, controller);
    }

    public void add(String group, Controller controller) {
        if (!manager.exists(controller)) {
            throw new IllegalArgumentException("Command must be mapped in the InfluxManager before it can be added to a HelpProvider.");
        }
        add(group, buildHelpEntry(controller));

    }

    protected void add(String group, H entry) {
        if (entry != null) {
            List<H> groupEntries = groupToEntriesMap.get(group);
            if (groupEntries == null) {
                groupEntries = new ArrayList<>();
            }
            groupEntries.add(entry);
            groupToEntriesMap.put(group, groupEntries);
            if (group.equals(DEFAULT)) {
                this.defaultEntryListing = null;
            }
        }
    }

    public void remove(Controller controller) {
        mapIter: {
            for (Map.Entry<String, List<H>> entry : groupToEntriesMap.entrySet()) {
                Iterator<H> iterator = entry.getValue().iterator();
                while (iterator.hasNext()) {
                    H helpEntry = iterator.next();
                    if (helpEntry.getController().equals(controller)) {
                        iterator.remove();
                        break mapIter;
                    }
                }
            }
        }
    }

    public H getHelpEntry(Controller controller) {
        for (Map.Entry<String, List<H>> entry : groupToEntriesMap.entrySet()) {
            for (H helpEntry : entry.getValue()) {
                if (helpEntry.getController().equals(controller)) {
                    return helpEntry;
                }
            }
        }
        return null;
    }

    public String getDefaultEntryListing() {
        return getDefaultEntryListing(null);
    }

    public <T extends S> String getDefaultEntryListing(T sender) {
        if (this.defaultEntryListing == null || this.defaultEntryListing.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (H entry : getEntries(DEFAULT)) {
                if (sender != null && restrictByPermission() && !getManager().authorize(sender, entry.getController(), entry.getPermissions())) {
                    continue;
                }

                if (builder.length() > 0) {
                    builder.append("{c1}, {c2}");
                }
                builder.append(entry.getCommandSyntax());
            }
            this.defaultEntryListing = builder.toString();
        }
        return this.defaultEntryListing;
    }


    public <T extends S> void sendPage(T sender) {
        this.sendPage(sender, 1);
    }

    public <T extends S> void sendPage(T sender, int page) {
        getManager().respondAnonymously(sender, "Commands: {c2}" + getDefaultEntryListing(sender));
        getManager().respondAnonymously(sender, "Valid command groups: {c2}" + StringUtil.combine("{c1}, {c2}", groupToEntriesMap.keySet()));
    }

    public <T extends S> void sendHelpFor(T sender, Controller controller) {
        for (String part : getHelpFor(controller)) {
            getManager().respondAnonymously(sender, part);
        }
    }

    public SortedMap<Controller, String[]> getHelpFor(String input) {
        SortedMap<Controller, String[]> help = new TreeMap<>();
        SortedSet<Controller> matches = getManager().getDispatcher().findFuzzyMatches(input);

        for (Controller controller : matches) {
            List<String> controllerHelp = getHelpFor(controller);
            if (!controllerHelp.isEmpty()) {
                help.put(controller, controllerHelp.toArray(new String[0]));
            }
        }
        return help;
    }

    public List<String> getHelpFor(Controller controller) {
        List<String> help = new ArrayList<>();
        String fullCommand = getManager().getCommandPrefix() + controller.getCommand().getReadableSyntax();
        H helpEntry = getHelpEntry(controller);
        if (helpEntry != null) {
            help.add("Aliases (" + controller.getCommand().getAliases().size() + "): " + StringUtil.combine("{c1}, {c2}", controller.getCommand().getReadableStringAliases()));
            Collections.addAll(help, helpEntry.getLongDescription());
        }
        return help;
    }
}