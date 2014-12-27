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
import org.spongepowered.api.util.command.CommandSource;

public class SpongeDispatcher extends Dispatcher<CommandSource> {

    public SpongeDispatcher(InfluxManager<CommandSource> manager) {
        super(manager);
    }

    @Override
    public <T extends CommandSource> boolean preDispatch(T sender, Controller controller, String input) {
        return dispatch(new SpongeCommandEvent<>(getManager(), controller, sender, consumedArgumentSets.get(input).get(controller)));
    }
}