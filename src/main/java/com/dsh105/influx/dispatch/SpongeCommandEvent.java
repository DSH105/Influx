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
import com.dsh105.influx.InfluxSpongeManager;
import com.dsh105.influx.response.ResponseLevel;
import com.dsh105.influx.response.SpongeResponder;
import com.dsh105.influx.syntax.ConsumedArgumentSet;
import org.spongepowered.api.Game;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.util.command.CommandSource;

public class SpongeCommandEvent<S extends CommandSource> extends CommandContext<S> {

    public SpongeCommandEvent(InfluxManager<S> manager, Controller controller, S sender, ConsumedArgumentSet consumedArgumentSet) {
        super(manager, controller, sender, consumedArgumentSet);
    }

    @Override
    public S sender() {
        return super.sender();
    }

    public PluginContainer getPluginContainer() {
        return ((InfluxSpongeManager) getManager()).getPluginContainer();
    }

    public Game getGame() {
        return ((InfluxSpongeManager) getManager()).getGame();
    }

    public void respond(Message.Text response) {
        respond(response, ResponseLevel.DEFAULT);
    }

    public void respond(Message.Text response, ResponseLevel level) {
        ((SpongeResponder) getManager()).respond(sender(), response, level);
    }

    public void respondAnonymously(Message.Text response) {
        respondAnonymously(response, ResponseLevel.DEFAULT);
    }

    public void respondAnonymously(Message.Text response, ResponseLevel level) {
        ((SpongeResponder) getManager().getResponder()).respondAnonymously(sender(), response, level);
    }
}