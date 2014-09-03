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

package com.dsh105.influx;

import com.dsh105.influx.dispatch.Dispatcher;
import com.dsh105.influx.format.FormatSet;
import com.dsh105.influx.format.MessagePurpose;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

public interface Manager extends Mapping {

    Plugin getPlugin();

    Dispatcher getDispatcher();

    void setMessage(MessagePurpose purpose, String value);

    String getMessage(MessagePurpose purpose);

    String getMessagePrefix();

    void setMessagePrefix(String messagePrefix);

    FormatSet getFormatSet();

    ChatColor[] getMessageFormats();

    ChatColor getMessageFormat(int index);

    String formatMessage(String message);

    void setMessageFormats(ChatColor... messageFormats);

    String formatMessage(ChatColor[] formats, String message);

    String formatMessage(FormatSet formatSet, String message);
}