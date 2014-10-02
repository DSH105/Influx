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

package com.dsh105.influx.help;

import com.dsh105.commodus.StringUtil;
import com.dsh105.commodus.paginator.Paginator;
import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.help.entry.BukkitHelpEntry;
import com.dsh105.influx.help.entry.BukkitExpandedHelpEntry;
import com.dsh105.influx.response.MessagePurpose;
import com.dsh105.powermessage.core.PowerMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;

import java.util.ArrayList;
import java.util.List;

public class BukkitHelpProvider<S extends CommandSender> extends HelpProvider<BukkitHelpEntry, S> {

    private Paginator<BukkitHelpEntry> paginator;

    public BukkitHelpProvider(InfluxManager<S> manager, HelpProvision provision) {
        super(manager, provision);
        this.paginator = new Paginator<>(6);
        this.buildHeader();
    }

    private void buildHeader() {
        StringBuilder header = new StringBuilder();
        header.append(ChatColor.YELLOW)
                .append("------ ")
                .append(ChatColor.WHITE)
                .append("Help: ")
                .append(getManager().getHelpTitle())
                .append(" ")
                .append("{topic}")
                .append(ChatColor.YELLOW);
        for (int i = header.length(); i < ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH; i++) {
            header.append("-");
        }
        this.setHeader(header.toString());
    }

    @Override
    public BukkitHelpEntry buildHelpEntry(Controller controller) {
        return getProvision() == HelpProvision.BUKKIT_CONDENSED ? new BukkitHelpEntry(this, controller) : new BukkitExpandedHelpEntry(this, controller);
    }

    public Paginator<BukkitHelpEntry> getPaginator() {
        return paginator;
    }

    @Override
    protected void add(String group, BukkitHelpEntry entry) {
        super.add(group, entry);
        paginator.add(entry);
    }

    @Override
    public <T extends S> void sendPage(T sender, int page) {
        if (!getPaginator().exists(page)) {
            getManager().respond(sender, getManager().getMessage(MessagePurpose.PAGE_NOT_FOUND, "<page>", page));
            return;
        }
        String header = getHeader().replace("{topic}", "Page " + ChatColor.RED + page + ChatColor.GOLD + "/" + ChatColor.RED + getPaginator().getPages());
        getManager().respondAnonymously(sender, header);
        for (String part : getPaginator().getPage(page)) {
            getManager().respondAnonymously(sender, part);
        }
    }

    @Override
    public <T extends S> void sendHelpFor(T sender, Controller controller) {
        String header = getHeader().replace("{topic}", getManager().getCommandPrefix() + controller.getCommand().getReadableSyntax());
        getManager().respondAnonymously(sender, header);
        for (String part : getHelpFor(controller)) {
            try {
                PowerMessage message = PowerMessage.fromJson(part);
                if (!message.getSnippets().isEmpty()) {
                    message.send(sender);
                    continue;
                }
            } catch (Exception ignored) {
            }
            getManager().respondAnonymously(sender, part);
        }
    }

    @Override
    public List<String> getHelpFor(Controller controller) {
        List<String> help = new ArrayList<>();
        String fullCommand = getManager().getCommandPrefix() + controller.getCommand().getReadableSyntax();
        BukkitHelpEntry helpEntry = getHelpEntry(controller);
        if (helpEntry == null) {
            help.add("No help for {c2}\"" + fullCommand + "\"");
        } else {
            help.add(new PowerMessage()
                             .then("{c2}\"")
                             .then(fullCommand)
                             .then("{c1}\" ")
                             .group(3).tooltip(helpEntry.getShortDescription()).exit()
                             .then("(hover to see " + controller.getCommand().getAliases().size() + " aliases):")
                             .tooltip(StringUtil.combine("{c1}, {c2}", controller.getCommand().getReadableStringAliases())).toJson());
            for (String part : helpEntry.getLongDescription()) {
                help.add("• " + part);
            }
        }
        return help;
    }
}