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

import com.dsh105.commodus.Paginator;
import com.dsh105.commodus.StringUtil;
import com.dsh105.influx.Controller;
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.help.entry.BukkitExpandedHelpEntry;
import com.dsh105.influx.help.entry.BukkitHelpEntry;
import com.dsh105.influx.response.MessagePurpose;
import com.dsh105.powermessage.core.PowerMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitHelpProvider<S extends CommandSender> extends HelpProvider<BukkitHelpEntry, S, PowerMessage> {

    private Paginator<BukkitHelpEntry> paginator;

    public BukkitHelpProvider(InfluxManager<S> manager, HelpProvision provision) {
        super(manager, provision);
        this.paginator = new Paginator<>(6);
        this.buildHeader();
    }

    public void buildHeader() {
        StringBuilder header = new StringBuilder();
        header.append(ChatColor.translateAlternateColorCodes('&', getManager().getMessage(MessagePurpose.BUKKIT_HELP_HEADER)));
        for (int i = header.length(); i < /*ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH*/ 5; i++) {
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
    protected boolean add(BukkitHelpEntry entry) {
        if (super.add(entry)) {
            paginator.append(entry);
            return true;
        }
        return false;
    }

    @Override
    protected boolean remove(String group, BukkitHelpEntry entry) {
        if (super.remove(group, entry)) {
            paginator.remove(entry);
            return true;
        }
        return false;
    }

    @Override
    public <T extends S> void sendPage(T sender, int page) {
        Paginator<BukkitHelpEntry> paginator = new Paginator<>(this.paginator.getPerPage());
        for (BukkitHelpEntry entry : this.paginator.getContent()) {
            if (getManager().authorize(sender, entry.getController(), entry.getPermissions())) {
                paginator.append(entry);
            }
        }
        if (!paginator.exists(page)) {
            getManager().respond(sender, getManager().getMessage(MessagePurpose.PAGE_NOT_FOUND, "<page>", page));
            return;
        }
        String header = getHeader().replace("<topic>", getManager().getHelpTitle() + " Page " + ChatColor.RED + page + ChatColor.GOLD + "/" + ChatColor.RED + paginator.getTotalPages());
        getManager().respondAnonymously(sender, header);
        for (BukkitHelpEntry part : paginator.getPage(page)) {
            part.send(sender);
        }
    }

    @Override
    public <T extends S> void sendHelpFor(T sender, Controller controller) {
        String header = getHeader().replace("<topic>", getManager().getHelpTitle());
        getManager().respondAnonymously(sender, header);
        for (PowerMessage part : getHelpFor(controller)) {
            part.send(sender);
        }
    }

    @Override
    public List<PowerMessage> getHelpFor(Controller controller) {
        List<PowerMessage> help = new ArrayList<>();
        String fullCommand = getManager().getCommandPrefix() + controller.getCommand().getAcceptedStringSyntax();
        BukkitHelpEntry helpEntry = getHelpEntry(controller);
        if (helpEntry == null) {
            help.add(new PowerMessage(getManager().getMessage(MessagePurpose.NO_HELP_FOUND, "<command>", fullCommand)));
        } else {
            String message = getManager().getMessage(MessagePurpose.BUKKIT_HELP_ENTRY);
            help.add(prepare(getManager(), helpEntry, message, 30));
            help.add(new PowerMessage(getManager().getMessage(MessagePurpose.BUKKIT_EXPANDED_HELP_DESCRIPTION_PART, "<desc>", helpEntry.getShortDescription())));
            for (String part : helpEntry.getLongDescription()) {
                help.add(new PowerMessage(getManager().getMessage(MessagePurpose.BUKKIT_EXPANDED_HELP_DESCRIPTION_PART, "<desc>", part)));
            }
        }
        return help;
    }

    public static PowerMessage prepare(InfluxManager<?> manager, BukkitHelpEntry helpEntry, String message, int maxDescLength) {
        int aliases = helpEntry.getController().getCommand().getAliases().size();
        PowerMessage result = new PowerMessage();

        int next = 0;
        Matcher matcher = Pattern.compile("<(.+?)>").matcher(message);
        while (next < message.length()) {
            if (matcher.find()) {
                if (matcher.start() > next) {
                    result.then(manager.getResponder().format(message.substring(matcher.start(), next)));
                }
                next = matcher.end() + 1;
                switch (matcher.group(1)) {
                    case "command":
                        result.then(manager.getResponder().format(manager.getCommandPrefix() + helpEntry.getController().getCommand().getAcceptedStringSyntax(), false))
                                .colour(ChatColor.UNDERLINE)
                                .tooltip(manager.getResponder().format("{c2}Click to auto-complete"))
                                .suggest(helpEntry.getController().getCommand().getAcceptedStringSyntax());
                        break;
                    case "alias_num":
                        result.then(aliases + " alias" + (aliases == 1 ? "" : "es") + "");
                        if (aliases > 1) {
                            result.tooltip(manager.getResponder().format(StringUtil.combine("{c1}, {c2}", helpEntry.getController().getCommand().getReadableStringAliases())));
                        }
                        break;
                    case "short_desc":
                    case "long_desc":
                        String desc = matcher.group(1).equals("short_desc") ? helpEntry.getShortDescription() : StringUtil.combineArray("\n", helpEntry.getLongDescription()[0]);
                        if (maxDescLength > 0) {
                            desc = desc.length() > maxDescLength ? desc.substring(0, maxDescLength - 3) + "..." : desc;
                        }
                        result.then(desc);
                        break;
                    default:
                        result.then(manager.getResponder().format(matcher.group(0), false));
                        break;
                }
            } else {
                // We're done
                result.then(message.substring(next));
                break;
            }
        }

        return result;
    }
}