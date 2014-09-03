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

package com.dsh105.influx.converter;

import com.dsh105.commodus.GeneralUtil;
import com.dsh105.commodus.IdentUtil;
import com.dsh105.commodus.StringUtil;
import com.dsh105.influx.syntax.parameter.EventVariable;
import com.dsh105.influx.syntax.parameter.ParameterException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

// TODO: More of these
public class Converters {

    private Converters() {
    }

    public static class PlayerConverter extends Converter<Player> {

        public PlayerConverter() {
            super(Player.class, 1);
        }

        @Override
        public Player convert(EventVariable variable) throws ParameterException {
            Player player = null;
            if (variable.getValue().length == 1) {
                player = IdentUtil.getPlayerOf(variable.getValue()[0]);
            }

            if (player == null) {
                throw new ParameterException("Sorry, that player could not be found!");
            }
            return player;
        }
    }

    public static class LocationConverter extends Converter<Location> {

        public LocationConverter() {
            super(Location.class, 4, 6);
        }

        @Override
        public Location convert(EventVariable variable) throws ParameterException {
            World world = Bukkit.getWorld(variable.getValue()[0]);

            if (world == null) {
                throw new IllegalArgumentException("World does not exist!");
            }

            double[] coords = new double[5];
            for (int i = 1; i < 6 ; i++) {
                try {
                    coords[i - 1] = GeneralUtil.toDouble(variable.getValue()[i]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
                    if (i <= 3) {
                        throw new IllegalStateException("Invalid location coordinates provided: " + StringUtil.combineArray(0, 4, ", ", variable.getValue()));
                    }
                }
            }

            return new Location(world, coords[0], coords[1], coords[2], (float) coords[3], (float) coords[4]);
        }
    }
}