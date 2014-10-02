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

import com.dsh105.commodus.reflection.Reflection;
import com.dsh105.influx.InfluxBukkitManager;
import com.dsh105.influx.InfluxManager;
import org.bukkit.command.CommandSender;

public enum HelpProvision {

    CONDENSED,
    EXPANDED,
    BUKKIT_CONDENSED,
    BUKKIT;

    public HelpProvider newProvider(InfluxManager<?> manager) {
        switch (this) {
            case EXPANDED:
                return new ExpandedHelpProvider<>(manager, this);
            case BUKKIT: case BUKKIT_CONDENSED:
                if (manager instanceof InfluxBukkitManager) {
                    return new BukkitHelpProvider<>((InfluxBukkitManager) manager, this);
                }
                throw new IllegalStateException("Bukkit help provision can only be enabled for an InfluxBukkitManager");
            default:
                return new DefaultHelpProvider<>(manager, this);
        }
    }
}