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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentParser {

    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("([^\\s\"']+)|\"([^\"]*)\"|'([^']*)'");

    private String input;
    private List<String> argumentList;
    private String[] arguments;

    public ArgumentParser(String input) {
        this.input = input;
        this.argumentList = new ArrayList<>();
        Matcher splitter = ARGUMENT_PATTERN.matcher(this.input);
        while (splitter.find()) {
            for (int i = 1; i <= 3; i++) {
                String argument = splitter.group(i);
                if (argument != null) {
                    this.argumentList.add(argument);
                }
            }
        }
        this.arguments = this.argumentList.toArray(new String[0]);
    }

    public String getInput() {
        return input;
    }

    public List<String> getArgumentList() {
        return argumentList;
    }

    public String[] getArguments() {
        return arguments;
    }
}