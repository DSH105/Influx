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

package com.dsh105.influx.response;

import com.dsh105.influx.util.Affirm;
import com.dsh105.powermessage.core.PowerMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BukkitResponder extends Responder<CommandSender> {

    private ChatColor[] warningScheme = {ChatColor.GOLD, ChatColor.YELLOW};
    private ChatColor[] severeScheme = {ChatColor.DARK_RED, ChatColor.RED};
    private ChatColor[] normalScheme = {ChatColor.WHITE};

    public BukkitResponder(String responsePrefix) {
        super(responsePrefix);
    }

    @Override
    public void handleResponse(CommandSender sender, String message, ResponseLevel level) {
        new PowerMessage(message).send(sender);
    }

    @Override
    public String getFormat(int index, ResponseLevel level) {
        return getResponseColor(index, getScheme(level)) + "";
    }

    public ChatColor getResponseColor(int index) {
        return getResponseColor(index, getNormalScheme());
    }

    public ChatColor getResponseColor(int index, ChatColor... scheme) {
        Affirm.notNull(scheme, "Format scheme must not be null.");
        if (index > scheme.length) {
            return ChatColor.WHITE;
        }
        return scheme[index - 1];
    }

    public ChatColor[] getWarningScheme() {
        return warningScheme;
    }

    public void setWarningScheme(ChatColor[] warningScheme) {
        this.warningScheme = warningScheme;
    }

    public ChatColor[] getSevereScheme() {
        return severeScheme;
    }

    public void setSevereScheme(ChatColor[] severeScheme) {
        this.severeScheme = severeScheme;
    }

    public ChatColor[] getNormalScheme() {
        return normalScheme;
    }

    public ChatColor[] getScheme(ResponseLevel level) {
        switch (level) {
            case WARNING:
                return getWarningScheme();
            case SEVERE:
                return getSevereScheme();
            default:
                return getNormalScheme();
        }
    }

    public void setMessageFormats(ChatColor... messageFormats) {
        if (messageFormats == null) {
            this.normalScheme = new ChatColor[]{ChatColor.WHITE};
            return;
        }
        this.normalScheme = messageFormats;
    }
}