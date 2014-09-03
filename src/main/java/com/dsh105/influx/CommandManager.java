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
import com.dsh105.influx.registration.RegistrationStrategy;
import com.dsh105.influx.registration.Registry;
import com.google.common.base.Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class CommandManager extends CommandMapping implements Manager {

    private Plugin plugin;
    private String messagePrefix;

    private Dispatcher dispatcher;
    private Registry registry;

    private FormatSet formatSet;
    private Map<MessagePurpose, String> messages = new HashMap<>();

    public CommandManager(Plugin plugin) {
        this.plugin = plugin;
        this.formatSet = new FormatSet(this);
        this.dispatcher = new Dispatcher(this);
        this.registry = RegistrationStrategy.NONE.prepare(this);
    }

    public CommandManager(Plugin plugin, String messagePrefix) {
        this(plugin);
        this.messagePrefix = messagePrefix;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public void setMessage(MessagePurpose purpose, String value) {
        Preconditions.checkNotNull(purpose, "Purpose must not be null.");
        Preconditions.checkNotNull(value, "Message must not be null.");
        this.messages.put(purpose, value);
    }

    @Override
    public String getMessage(MessagePurpose purpose) {
        Preconditions.checkNotNull(purpose, "Purpose must not be null.");
        if (!messages.containsKey(purpose)) {
            messages.put(purpose, purpose.getDefaultValue());
        }
        return messages.get(purpose);
    }

    @Override
    public String getMessagePrefix() {
        return messagePrefix;
    }

    @Override
    public void setMessagePrefix(String messagePrefix) {
        this.messagePrefix = messagePrefix;
    }

    @Override
    public FormatSet getFormatSet() {
        return formatSet;
    }

    @Override
    public ChatColor[] getMessageFormats() {
        return formatSet.getMessageFormats();
    }

    @Override
    public ChatColor getMessageFormat(int index) {
        return formatSet.getMessageFormat(index);
    }

    @Override
    public void setMessageFormats(ChatColor... messageFormats) {
        formatSet.setMessageFormats(messageFormats);
    }

    @Override
    public String formatMessage(String message) {
        return formatMessage(formatSet, message);
    }

    @Override
    public String formatMessage(ChatColor[] formats, String message) {
        return this.formatSet.formatMessage(formats, message);
    }

    @Override
    public String formatMessage(FormatSet formatSet, String message) {
        return formatSet.formatMessage(message);
    }

}