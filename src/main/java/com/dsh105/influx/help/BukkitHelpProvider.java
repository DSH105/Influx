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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BukkitHelpProvider<S extends CommandSender> extends HelpProvider<BukkitHelpEntry, S> {

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
            paginator.add(entry);
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
        Paginator<BukkitHelpEntry> paginator = new Paginator<>(getPaginator().getPerPage());
        for (BukkitHelpEntry entry : getPaginator().getRaw()) {
            if (getManager().authorize(sender, entry.getController(), entry.getPermissions())) {
                paginator.add(entry);
            }
        }
        if (!paginator.exists(page)) {
            getManager().respond(sender, getManager().getMessage(MessagePurpose.PAGE_NOT_FOUND, "<page>", page));
            return;
        }
        String header = getHeader().replace("<topic>", getManager().getHelpTitle() + " Page " + ChatColor.RED + page + ChatColor.GOLD + "/" + ChatColor.RED + paginator.getPages());
        getManager().respondAnonymously(sender, header);
        for (String part : paginator.getPage(page)) {
            getManager().respondAnonymously(sender, part);
        }
    }

    @Override
    public <T extends S> void sendHelpFor(T sender, Controller controller) {
        String header = getHeader().replace("<topic>", getManager().getHelpTitle());
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
        String fullCommand = getManager().getCommandPrefix() + controller.getCommand().getAcceptedStringSyntax();
        BukkitHelpEntry helpEntry = getHelpEntry(controller);
        if (helpEntry == null) {
            help.add(getManager().getMessage(MessagePurpose.NO_HELP_FOUND, "<command>", fullCommand));
        } else {
            String message = getManager().getMessage(MessagePurpose.BUKKIT_HELP_ENTRY);
            help.add(prepare(getManager(), helpEntry, message, 30).toJson());
            help.add(getManager().getMessage(MessagePurpose.BUKKIT_EXPANDED_HELP_DESCRIPTION_PART, "<desc>", helpEntry.getShortDescription()));
            for (String part : helpEntry.getLongDescription()) {
                help.add(getManager().getMessage(MessagePurpose.BUKKIT_EXPANDED_HELP_DESCRIPTION_PART, "<desc>", part));
            }
        }
        return help;
    }

    public static PowerMessage prepare(InfluxManager<?> manager, BukkitHelpEntry helpEntry, String message, int maxDescLength) {
        int aliases = helpEntry.getController().getCommand().getAliases().size();
        PowerMessage powerMessage = new PowerMessage();
        int index = 0;
        Matcher matcher = Pattern.compile("<(.+?)>").matcher(message);
        while (matcher.find()) {
            if (matcher.start() != 0) {
                powerMessage.then(manager.getResponder().format(message.substring(index, matcher.start())));
            }

            switch (matcher.group(1)) {
                case "command":
                    powerMessage.then(manager.getResponder().format(manager.getCommandPrefix() + helpEntry.getController().getCommand().getAcceptedStringSyntax(), false))
                            .colour(ChatColor.UNDERLINE)
                            .tooltip(manager.getResponder().format("{c2}Click to auto-complete"))
                            .suggest(helpEntry.getController().getCommand().getAcceptedStringSyntax());
                    break;
                case "alias_num":
                    powerMessage.then(aliases + " alias" + (aliases == 1 ? "" : "es") + "");
                    if (aliases > 1) {
                        powerMessage.tooltip(manager.getResponder().format(StringUtil.combine("{c1}, {c2}", helpEntry.getController().getCommand().getReadableStringAliases())));
                    }
                    break;
                case "short_desc":
                case "long_desc":
                    String desc = matcher.group(1).equals("short_desc") ? helpEntry.getShortDescription() : StringUtil.combineArray("\n", helpEntry.getLongDescription()[0]);
                    if (maxDescLength > 0) {
                        desc = desc.length() > maxDescLength ? desc.substring(0, maxDescLength - 3) + "..." : desc;
                    }
                    powerMessage.then(desc);
                    break;
                default:
                    powerMessage.then(manager.getResponder().format(matcher.group(0), false));
                    break;
            }
            index = matcher.end();
        }
        if (index != message.length()) {
            powerMessage.then(manager.getResponder().format(message.substring(index, message.length())));
        }
        return powerMessage;
    }
}