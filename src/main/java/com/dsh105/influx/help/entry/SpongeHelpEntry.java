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
import com.dsh105.influx.help.SpongeHelpProvider;
import com.dsh105.influx.response.MessagePurpose;
import org.spongepowered.api.text.message.Message;

public class SpongeHelpEntry extends HelpEntry {

    protected Message.Text message;

    public SpongeHelpEntry(HelpProvider helpProvider, Controller controller) {
        super(helpProvider, controller);
        reformat();
    }

    @Override
    public void reformat() {
        super.reformat();
        message = SpongeHelpProvider.prepare(getHelpProvider().getManager(), this, getTemplate(), -1);
    }

    protected String getTemplate() {
        return getHelpProvider().getManager().getMessage(MessagePurpose.SPONGE_SHORT_HELP_ENTRY);
    }

    public Message.Text getMessage() {
        return message;
    }
}