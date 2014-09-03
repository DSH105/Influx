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

package com.dsh105.influx.format;

import com.dsh105.influx.CommandManager;
import com.dsh105.powermessage.markup.MarkupBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatSet {

    public static final ChatColor[] SEVERE = {ChatColor.DARK_RED, ChatColor.RED};
    public static final ChatColor[] WARNING = {ChatColor.GOLD, ChatColor.YELLOW};

    private CommandManager manager;

    private ChatColor[] formats;

    public FormatSet(CommandManager manager, ChatColor... formats) {
        this.manager = manager;
        this.formats = formats;
    }

    public ChatColor[] getMessageFormats() {
        return formats;
    }

    public ChatColor getMessageFormat(int index) {
        if (index > getMessageFormats().length) {
            return ChatColor.WHITE;
        }
        return getMessageFormats()[index - 1];
    }

    public void setMessageFormats(ChatColor... messageFormats) {
        if (messageFormats == null) {
            this.formats = new ChatColor[0];
            return;
        }
        this.formats = messageFormats;
    }

    public String formatMessage(String message) {
        return formatMessage(this, message);
    }

    public String formatMessage(ChatColor[] formats, String message) {
        return formatMessage(new FormatSet(manager, formats), message);
    }

    public String formatMessage(FormatSet formatSet, String message) {
        Matcher matcher = Pattern.compile("\\{c([0-9]+)\\}").matcher(message);
        while (matcher.find()) {
            message = message.replace(matcher.group(1), formatSet.getMessageFormat(Integer.parseInt(matcher.group(1)) - 1) + "");
        }
        return message;
    }

    public void send(CommandSender sender, String message, ChatColor... formats) {
        if (message.isEmpty()) {
            return;
        }

        if (!manager.getMessagePrefix().isEmpty()) {
            message = manager.getMessagePrefix() + ChatColor.RESET + message;
        }

        new MarkupBuilder().withText(manager.formatMessage(formats, message)).build().send(sender);
    }

    public void send(CommandSender sender, String message) {
        send(sender, message, manager.getMessageFormats());
    }
}