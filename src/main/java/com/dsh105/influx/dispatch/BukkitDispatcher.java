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

import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BukkitDispatcher extends Dispatcher<CommandSender> implements CommandExecutor {

    public BukkitDispatcher(InfluxManager<CommandSender> manager) {
        super(manager);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        List<String> arguments = new ArrayList<>();
        Collections.addAll(arguments, args);
        arguments.add(0, command.getLabel());
        return dispatch(sender, arguments.toArray(new String[0]));
    }

    @Override
    public <T extends CommandSender> boolean preDispatch(T sender, Controller controller, String input) {
        return dispatch(new CommandEvent<>(getManager(), controller, sender, consumedArgumentSets.get(input).get(controller)));
    }
}