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

package com.dsh105.influx.response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Responder<S> {

    private String responsePrefix;

    protected Responder(String responsePrefix) {
        this.responsePrefix = responsePrefix;
    }

    public String getResponsePrefix() {
        return responsePrefix;
    }

    public void setResponsePrefix(String responsePrefix) {
        if (responsePrefix == null) {
            responsePrefix = "";
        }
        this.responsePrefix = responsePrefix;
    }

    public String format(String message) {
        return format(message, ResponseLevel.DEFAULT);
    }

    public String format(String message, boolean includeFirstColour) {
        return format(message, ResponseLevel.DEFAULT, includeFirstColour);
    }

    public String format(String message, ResponseLevel level) {
        return format(message, level, true);
    }

    public String format(String message, ResponseLevel level, boolean includeFirstColour) {
        message = (includeFirstColour ? "{c1}" : "") + message;
        Matcher matcher = Pattern.compile("\\{c([0-9]+)\\}").matcher(message);
        while (matcher.find()) {
            message = message.replace(matcher.group(0), getFormat(Integer.parseInt(matcher.group(1)), level));
        }
        return message;
    }

    public abstract String getFormat(int index, ResponseLevel level);

    public <T extends S> void respond(T sender, String message) {
        respond(sender, message, ResponseLevel.DEFAULT);
    }

    public <T extends S> void respond(T sender, String message, ResponseLevel level) {
        String response = message;
        if (!getResponsePrefix().isEmpty()) {
            response = getResponsePrefix() + " " + format(message);
        }
        respondAnonymously(sender, response, level);
    }

    public <T extends S> void respondAnonymously(T sender, String message) {
        respondAnonymously(sender, message, ResponseLevel.DEFAULT);
    }

    public <T extends S> void respondAnonymously(T sender, String message, ResponseLevel level) {
        handleResponse(sender, format(message), level);
    }

    public abstract void handleResponse(S sender, String message, ResponseLevel level);
}