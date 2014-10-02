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
import com.dsh105.influx.InfluxBukkitManager;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.syntax.ConsumedArgumentSet;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class BukkitCommandEvent<S extends CommandSender> extends CommandContext<S> {

    public BukkitCommandEvent(InfluxManager<S> manager, Controller controller, S sender, ConsumedArgumentSet consumedArgumentSet) {
        super(manager, controller, sender, consumedArgumentSet);
    }

    @Override
    public S sender() {
        return super.sender();
    }

    public Plugin getPlugin() {
        return ((InfluxBukkitManager) getManager()).getPlugin();
    }
}