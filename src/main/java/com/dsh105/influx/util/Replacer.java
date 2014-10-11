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

package com.dsh105.influx.util;

public class Replacer {

    public static String makeReplacements(String message, Object... pairedReplacements) {
        for (int i = 0; i < pairedReplacements.length; i += 2) {
            if ((i + 1) >= pairedReplacements.length) {
                break;
            }
            message = message.replace(pairedReplacements[i].toString(), pairedReplacements[i + 1].toString());
        }
        return message;
    }
}