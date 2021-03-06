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

package com.dsh105.influx.dispatch;

public class CommandDispatchException extends RuntimeException {

    public CommandDispatchException() {
    }

    public CommandDispatchException(String s) {
        super(s);
    }

    public CommandDispatchException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CommandDispatchException(Throwable throwable) {
        super(throwable);
    }
}