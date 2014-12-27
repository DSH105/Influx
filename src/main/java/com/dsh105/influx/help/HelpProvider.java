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
import com.dsh105.commodus.reflection.Reflection;
import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.help.entry.HelpEntry;
import com.dsh105.influx.util.Affirm;
import com.dsh105.influx.util.Comparators;

import java.lang.reflect.Array;
import java.util.*;

public abstract class HelpProvider<E extends HelpEntry, S, H> {

    public static String DEFAULT = "default";

    private InfluxManager<S> manager;
    private HelpProvision provision;
    private String header;

    private HashMap<String, SortedSet<E>> groupToEntriesMap = new HashMap<>();
    private String defaultEntryListing;

    private Class<H> helpType = (Class<H>) Reflection.getTypeArguments(HelpProvider.class, getClass()).get(2);

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

    public SortedSet<E> getEntries(String group) {
        SortedSet<E> entries = groupToEntriesMap.get(group);
        if (entries == null) {
            entries = new TreeSet<>();
        }
        return Collections.unmodifiableSortedSet(entries);
    }

    public SortedSet<E> getEntries() {
        SortedSet<E> entries = new TreeSet<>();
        for (SortedSet<E> group : groupToEntriesMap.values()) {
            entries.addAll(group);
        }
        return Collections.unmodifiableSortedSet(entries);
    }

    public abstract E buildHelpEntry(Controller controller);

    public boolean add(Controller controller) {
        return this.add(buildHelpEntry(controller));
    }

    protected boolean add(E entry) {
        Affirm.notNull(entry, "Help entry must not be null.");

        String group = entry.getController().getDescription().getHelpGroup();
        SortedSet<E> groupEntries = groupToEntriesMap.get(group);
        if (groupEntries == null) {
            groupEntries = new TreeSet<>();
        }
        if (groupEntries.add(entry)) {
            groupToEntriesMap.put(group, groupEntries);
            if (group.equals(DEFAULT)) {
                this.defaultEntryListing = null;
            }
            return true;
        }
        return false;
    }

    public boolean remove(Controller controller) {
        for (Map.Entry<String, SortedSet<E>> entry : groupToEntriesMap.entrySet()) {
            for (E helpEntry : entry.getValue()) {
                if (helpEntry.getController().equals(controller)) {
                    return remove(entry.getKey(), helpEntry);
                }
            }
        }
        return false;
    }

    protected boolean remove(String group, E entry) {
        return groupToEntriesMap.get(group).remove(entry);
    }

    public E getHelpEntry(Controller controller) {
        for (Map.Entry<String, SortedSet<E>> entry : groupToEntriesMap.entrySet()) {
            for (E helpEntry : entry.getValue()) {
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
            for (E entry : getEntries(DEFAULT)) {
                if (sender != null && restrictByPermission() && !manager.authorize(sender, entry.getController(), entry.getPermissions())) {
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
        manager.respondAnonymously(sender, "Commands: {c2}" + getDefaultEntryListing(sender));
        manager.respondAnonymously(sender, "Valid command groups: {c2}" + StringUtil.combine("{c1}, {c2}", groupToEntriesMap.keySet()));
    }

    public abstract <T extends S> void sendHelpFor(T sender, Controller controller);/* {
        for (String part : getStringHelpFor(controller)) {
            manager.respondAnonymously(sender, part);
        }
    }*/

    protected <T extends S> void sendStringHelp(T sender, Controller controller) {
        for (String part : getStringHelpFor(controller)) {
            manager.respondAnonymously(sender, part);
        }
    }

    public SortedMap<Controller, String[]> getStringHelpFor(String input) {
        SortedMap<Controller, String[]> help = new TreeMap<>(new Comparators.ControllerComparator());
        SortedSet<Controller> matches = manager.getDispatcher().findFuzzyMatches(input);

        for (Controller controller : matches) {
            List<String> controllerHelp = getStringHelpFor(controller);
            if (!controllerHelp.isEmpty()) {
                help.put(controller, controllerHelp.toArray(new String[0]));
            }
        }
        return help;
    }

    public SortedMap<Controller, H[]> getHelpFor(String input) {
        SortedMap<Controller, H[]> help = new TreeMap<>(new Comparators.ControllerComparator());
        SortedSet<Controller> matches = manager.getDispatcher().findFuzzyMatches(input);

        for (Controller controller : matches) {
            List<H> controllerHelp = getHelpFor(controller);
            if (!controllerHelp.isEmpty()) {
                help.put(controller, controllerHelp.toArray((H[]) Array.newInstance(helpType, controllerHelp.size())));
            }
        }
        return help;
    }

    public abstract List<H> getHelpFor(Controller controller);

    public List<String> getStringHelpFor(Controller controller) {
        List<String> help = new ArrayList<>();
        E helpEntry = getHelpEntry(controller);
        if (helpEntry != null) {
            help.add("Aliases (" + controller.getCommand().getAliases().size() + "): " + StringUtil.combine("{c1}, {c2}", controller.getCommand().getReadableStringAliases()));
            Collections.addAll(help, helpEntry.getLongDescription());
        }
        return help;
    }
}