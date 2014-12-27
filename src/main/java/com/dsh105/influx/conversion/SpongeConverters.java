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

package com.dsh105.influx.conversion;

import com.dsh105.commodus.StringUtil;
import com.dsh105.commodus.container.SpongePosition;
import com.dsh105.commodus.sponge.SpongeUtil;
import com.dsh105.influx.dispatch.SpongeCommandEvent;
import com.dsh105.influx.syntax.ContextualVariable;
import org.spongepowered.api.entity.player.Player;

// TODO: More of these
public class SpongeConverters {

    private SpongeConverters() {
    }

    public static class PlayerConverter extends Converter<Player> {

        public PlayerConverter() {
            super(Player.class, 1);
        }

        @Override
        public Player convert(ContextualVariable variable) throws ConversionException {
            Player player = null;
            if (variable.getConsumedArguments().length == 1) {
                player = ((SpongeCommandEvent) variable.getContext()).getGame().getServer().get().getPlayer(variable.getConsumedArguments()[0]).orNull();
            }

            if (player == null) {
                throw new ConversionException("Sorry, that player could not be found!");
            }
            return player;
        }
    }

    public static class LocationConverter extends Converter<SpongePosition> {

        public LocationConverter() {
            super(SpongePosition.class, 4, 6);
        }

        @Override
        public SpongePosition convert(ContextualVariable variable) throws ConversionException {
            try {
                SpongePosition position = SpongeUtil.readLocation(0, variable.getConsumedArguments());
                if (position == null) {
                    throw new ConversionException("Invalid location coordinates provided: " + StringUtil.combineArray(0, 4, ", ", variable.getConsumedArguments()));
                }
                return position;
            } catch (IllegalStateException e) {
                throw new ConversionException("World does not exist:" + variable.getConsumedArguments()[0]);
            }
        }
    }
}