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

import com.dsh105.influx.CommandBuilder;
import com.dsh105.influx.CommandListener;
import com.dsh105.influx.InfluxException;
import com.dsh105.influx.InvalidCommandException;
import com.dsh105.influx.annotation.Accept;
import com.dsh105.influx.annotation.Nest;
import com.dsh105.influx.annotation.Nested;
import com.dsh105.influx.annotation.Restrict;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CommandNest {

    private List<String> parents = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    public CommandNest(CommandBuilder builder, String... parents) {
        try {
            prepare(builder.getListener());
        } catch (InvalidCommandException e) {
            Nested nesting = builder.getCommandBinding().getCallableMethod().getAnnotation(Nested.class);
            if (nesting != null) {
                Collections.addAll(this.parents, nesting.nests());
            }
        }

        Collections.addAll(this.parents, parents);
    }

    private void prepare(CommandListener listener) {
        if (!listener.getClass().isAnnotationPresent(Nest.class)) {
            throw new InfluxException("Listener is not a valid parent command. @Nest annotation not present on " + listener.getClass().getSimpleName());
        }

        Collections.addAll(parents, listener.getClass().getAnnotation(Nest.class).nests());
        if (listener.getClass().isAnnotationPresent(Restrict.class)) {
            Collections.addAll(permissions, listener.getClass().getAnnotation(Restrict.class).value());
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