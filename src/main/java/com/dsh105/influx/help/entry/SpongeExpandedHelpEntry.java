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

import com.dsh105.influx.Controller;
import com.dsh105.influx.help.HelpProvider;
import com.dsh105.influx.response.MessagePurpose;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.message.MessageBuilder;
import org.spongepowered.api.text.message.Messages;

public class SpongeExpandedHelpEntry extends SpongeHelpEntry {

    public SpongeExpandedHelpEntry(HelpProvider helpProvider, Controller controller) {
        super(helpProvider, controller);
    }

    @Override
    public void reformat() {
        super.reformat();
        if (getLongDescription().length != 0) {
            message = message.builder().onHover(new HoverAction.ShowText() {
                @Override
                public String getId() {
                    return "long_description";
                }

                @Override
                public Message getResult() {
                    MessageBuilder.Text builder = Messages.builder("");
                    for (int i = 0; i < getLongDescription().length; i++) {
                        builder.append(Messages.of(getLongDescription()[i] + (i == getLongDescription().length - 1 ? "\n" : "")));
                    }
                    return builder.build();
                }
            }).build();
        }
    }

    @Override
    protected String getTemplate() {
        return getHelpProvider().getManager().getMessage(MessagePurpose.BUKKIT_HELP_ENTRY);
    }
}