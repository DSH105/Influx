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

package com.dsh105.influx.registration;

import com.dsh105.influx.InfluxBukkitManager;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.InfluxSpongeManager;
import com.dsh105.influx.registration.bukkit.BukkitRegistry;
import com.dsh105.influx.registration.sponge.SpongeRegistry;

public enum RegistrationStrategy {

    NONE, BUKKIT, SPONGE;

    public Registry prepare(InfluxManager<?> manager) {
        Registry registry;
        switch (this) {
            case BUKKIT:
                if (manager instanceof InfluxBukkitManager) {
                    registry = new BukkitRegistry((InfluxBukkitManager) manager);
                    break;
                }
                throw new IllegalStateException("Bukkit command registration can only be enabled for an InfluxBukkitManager");
            case SPONGE:
                if (manager instanceof InfluxSpongeManager) {
                    registry = new SpongeRegistry((InfluxSpongeManager) manager);
                    break;
                }
                throw new IllegalStateException("Sponge command registration can only be enabled for an InfluxSpongeManager");
            default:
                registry = new Registry(manager);
                break;
        }
        registry.register(manager.getMappedCommands());
        return registry;
    }
}