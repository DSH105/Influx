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

package com.dsh105.influx.help.entry;

import com.dsh105.commodus.paginator.Pageable;
import com.dsh105.influx.Controller;
import com.dsh105.influx.Description;
import com.dsh105.influx.help.HelpProvider;
import com.dsh105.powermessage.core.PowerMessage;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

public class BukkitHelpEntry extends HelpEntry implements Pageable {

    private PowerMessage message;

    public BukkitHelpEntry(HelpProvider helpProvider, Controller controller) {
        super(helpProvider, controller);
    }

    @Override
    public void reformat() {
        super.reformat();
        message = new PowerMessage();
        message.then(getHelpProvider().getManager().getCommandPrefix() + getCommandSyntax())
                .tooltip(format("{c2}Click to auto-complete"))
                .suggest(getCommandSyntax());
    }

    public PowerMessage getMessage() {
        if (message == null) {
            reformat();
        }
        return message;
    }

    @Override
    public String getContent() {
        return getMessage().getContent();
    }

    @Override
    public Pageable send(CommandSender sender) {
        return getMessage().send(sender);
    }
}