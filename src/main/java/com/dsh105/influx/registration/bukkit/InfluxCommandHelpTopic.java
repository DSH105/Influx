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

package com.dsh105.influx.registration.bukkit;

import com.dsh105.commodus.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.help.HelpTopic;
import java.util.List;

public class InfluxCommandHelpTopic extends HelpTopic {

    private InfluxCommand command;

    public InfluxCommandHelpTopic(InfluxCommand command) {
        this.command = command;

        if (command.getLabel().startsWith(command.getManager().getCommandPrefix())) {
            name = command.getLabel();
        } else {
            name = command.getManager().getCommandPrefix() + command.getLabel();
        }

        String description = command.getDescription();
        String usage = command.getUsage();
        List<String> aliases = command.getAliases();

        int i = description.indexOf("\n");
        if (i > 1) {
            shortText = description.substring(0, i - 1);
        } else {
            shortText = description;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GOLD);
        sb.append("Description: ");
        sb.append(ChatColor.WHITE);
        sb.append(description);

        sb.append("\n");

        sb.append(ChatColor.GOLD);
        sb.append("Usage: ");
        sb.append(ChatColor.WHITE);
        sb.append(usage.replace("<command>", name.substring(1)));

        if (command.getAliases().size() > 0) {
            sb.append("\n");
            sb.append(ChatColor.GOLD);
            sb.append("Aliases: ");
            sb.append(ChatColor.WHITE);
            sb.append(StringUtil.combine(", ", aliases));
        }

        fullText = sb.toString();
    }

    @Override
    public boolean canSee(CommandSender sender) {
        if (!command.isRegistered()) {
            return false;
        }

        if (sender instanceof ConsoleCommandSender) {
            return true;
        }

        if (amendedPermission != null) {
            return sender.hasPermission(amendedPermission);
        }
        return command.testPermissionSilent(sender);
    }
}