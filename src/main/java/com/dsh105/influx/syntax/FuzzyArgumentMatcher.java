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

import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.util.Comparators;

import java.util.*;

public class FuzzyArgumentMatcher {

    private InfluxManager<?> manager;
    private String input;
    private String[] arguments;

    private SortedMap<Controller, SortedSet<Syntax>> candidates = new TreeMap<>(new Comparators.ControllerComparator());
    private SortedMap<Integer, SortedSet<Controller>> possibleMatches = new TreeMap<>();

    public FuzzyArgumentMatcher(InfluxManager<?> manager, String input) {
        this.manager = manager;
        this.input = input;
        this.arguments = new ArgumentParser(input).getArguments();

        this.possibleMatches.put(0, new TreeSet<>(new Comparators.ControllerComparator()));
        for (Controller controller : manager.getMappedCommands()) {
            SortedSet<Syntax> set = new TreeSet<>(new Comparators.CommandComparator());
            set.add(controller.getCommand());
            set.addAll(controller.getCommand().getAliases());
            candidates.put(controller, set);
        }

        compareFuzzily();
    }

    public InfluxManager<?> getManager() {
        return manager;
    }

    public String getInput() {
        return input;
    }

    public SortedSet<Controller> getAllPossibleMatches() {
        SortedSet<Controller> possibleMatches = new TreeSet<>(new Comparators.ControllerComparator());
        for (SortedSet<Controller> set : this.possibleMatches.values()) {
            possibleMatches.addAll(set);
        }
        return Collections.unmodifiableSortedSet(possibleMatches);
    }

    public SortedSet<Controller> getHighestPossibleMatches() {
        return Collections.unmodifiableSortedSet(this.possibleMatches.get(this.possibleMatches.lastKey()));
    }

    private void compareFuzzily() {
        // Only compares parameters, not variables

        controllerIter: for (Controller controller : candidates.keySet()) {
            SortedSet<Syntax> syntaxSet = candidates.get(controller);
            if (syntaxSet != null && !syntaxSet.isEmpty()) {
                syntaxIter: for (Syntax syntax : candidates.get(controller)) {
                    int syntaxLength = syntax.getSyntax().size();
                    for (int i = 0; i < syntaxLength; i++) {
                        Parameter parameter = syntax.getParameter(i, false);
                        if (parameter == null) {
                            continue syntaxIter;
                        }

                        if (parameter instanceof Variable) {
                            // Syntax requires more
                            possibleMatch(i, controller);
                            continue controllerIter;
                        }

                        try {
                            String arg = arguments[i];
                            if (i == arguments.length - 1) {
                                if (arg.toLowerCase().startsWith(parameter.getName().toLowerCase())) {
                                    possibleMatch(i, controller);
                                    continue controllerIter;
                                }
                            } else if (parameter.verify(arg)) {
                                possibleMatch(i, controller);
                                continue controllerIter;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            possibleMatch(i, controller);
                            continue controllerIter;
                        }
                    }
                }
            }
        }
    }

    private void possibleMatch(int index, Controller candidate) {
        SortedSet<Controller> candidates = possibleMatches.get(index);
        if (candidates == null) {
            candidates = new TreeSet<>(new Comparators.ControllerComparator());
        }
        candidates.add(candidate);
        possibleMatches.put(index, candidates);
    }
}