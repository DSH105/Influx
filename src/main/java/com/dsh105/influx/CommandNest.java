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

package com.dsh105.influx;

import com.dsh105.influx.annotation.Authorize;
import com.dsh105.influx.annotation.Nest;
import com.dsh105.influx.annotation.Nested;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandNest {

    private List<String> parents = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    public CommandNest(CommandListener destination, CommandBuilder builder, String... parents) {
        Nested nesting = builder.getCommandBinding().getCallableMethod().getAnnotation(Nested.class);
        if (nesting != null) {
            Collections.addAll(this.parents, nesting.value());
        }

        try {
            prepare(destination);
        } catch (IllegalCommandException ignored) {
            // Not a problem, nests can be provided in more than one way!
        }

        Collections.addAll(this.parents, parents);
    }

    protected void prepare(CommandListener listener) throws IllegalCommandException {
        if (!listener.getClass().isAnnotationPresent(Nest.class)) {
            throw new IllegalCommandException("Listener is not a valid parent command. @Nest annotation not present on " + listener.getClass().getSimpleName());
        }

        Collections.addAll(parents, listener.getClass().getAnnotation(Nest.class).nests());
        if (listener.getClass().isAnnotationPresent(Authorize.class)) {
            Collections.addAll(permissions, listener.getClass().getAnnotation(Authorize.class).value());
        }
    }

    public List<String> getParents() {
        return Collections.unmodifiableList(parents);
    }

    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public void addParents(String... parents) {
        Collections.addAll(this.parents, parents);
    }

    public void addPermissions(String... permissions) {
        Collections.addAll(this.permissions, permissions);
    }
}