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
import com.dsh105.influx.help.entry.SpongeExpandedHelpEntry;
import com.dsh105.influx.help.entry.SpongeHelpEntry;
import com.dsh105.influx.response.MessagePurpose;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.message.MessageBuilder;
import org.spongepowered.api.text.message.Messages;
import org.spongepowered.api.util.command.CommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpongeHelpProvider<S extends CommandSource> extends HelpProvider<SpongeHelpEntry, S, Message.Text> {

    private Paginator<SpongeHelpEntry> paginator;

    public SpongeHelpProvider(InfluxManager<S> manager, HelpProvision provision) {
        super(manager, provision);
        this.paginator = new Paginator<>(6);
        this.buildHeader();
    }

    public void buildHeader() {
        StringBuilder header = new StringBuilder();
        // FIXME: it's deprecated for a reason
        header.append(Messages.replaceCodes(getManager().getMessage(MessagePurpose.SPONGE_HELP_HEADER), '&'));
        for (int i = header.length(); i < 5; i++) {
            header.append("-");
        }
        this.setHeader(header.toString());
    }

    @Override
    public SpongeHelpEntry buildHelpEntry(Controller controller) {
        return getProvision() == HelpProvision.BUKKIT_CONDENSED ? new SpongeHelpEntry(this, controller) : new SpongeExpandedHelpEntry(this, controller);
    }

    public Paginator<SpongeHelpEntry> getPaginator() {
        return paginator;
    }

    @Override
    protected boolean add(SpongeHelpEntry entry) {
        if (super.add(entry)) {
            paginator.append(entry);
            return true;
        }
        return false;
    }

    @Override
    protected boolean remove(String group, SpongeHelpEntry entry) {
        if (super.remove(group, entry)) {
            paginator.remove(entry);
            return true;
        }
        return false;
    }

    @Override
    public <T extends S> void sendPage(T sender, int page) {
        Paginator<SpongeHelpEntry> paginator = new Paginator<>(this.paginator.getPerPage());
        for (SpongeHelpEntry entry : this.paginator.getContent()) {
            if (getManager().authorize(sender, entry.getController(), entry.getPermissions())) {
                paginator.append(entry);
            }
        }
        if (!paginator.exists(page)) {
            getManager().respond(sender, getManager().getMessage(MessagePurpose.PAGE_NOT_FOUND, "<page>", page));
            return;
        }
        String header = getHeader().replace("<topic>", getManager().getHelpTitle() + " Page " + TextColors.RED + page + TextColors.GOLD + "/" + TextColors.RED + paginator.getTotalPages());
        getManager().respondAnonymously(sender, header);
        for (SpongeHelpEntry part : paginator.getPage(page)) {
            sender.sendMessage(part.getMessage());
        }
    }

    @Override
    public <T extends S> void sendHelpFor(T sender, Controller controller) {
        String header = getHeader().replace("<topic>", getManager().getHelpTitle());
        getManager().respondAnonymously(sender, header);
        for (Message.Text part : getHelpFor(controller)) {
            sender.sendMessage(part);
        }
    }

    @Override
    public List<Message.Text> getHelpFor(Controller controller) {
        List<Message.Text> help = new ArrayList<>();
        String fullCommand = getManager().getCommandPrefix() + controller.getCommand().getAcceptedStringSyntax();
        SpongeHelpEntry helpEntry = getHelpEntry(controller);
        if (helpEntry == null) {
            help.add(Messages.of(getManager().getMessage(MessagePurpose.NO_HELP_FOUND, "<command>", fullCommand)));
        } else {
            String message = getManager().getMessage(MessagePurpose.BUKKIT_HELP_ENTRY);
            help.add(prepare(getManager(), helpEntry, message, 30));
            help.add(Messages.of(getManager().getMessage(MessagePurpose.BUKKIT_EXPANDED_HELP_DESCRIPTION_PART, "<desc>", helpEntry.getShortDescription())));
            for (String part : helpEntry.getLongDescription()) {
                help.add(Messages.of(getManager().getMessage(MessagePurpose.BUKKIT_EXPANDED_HELP_DESCRIPTION_PART, "<desc>", part)));
            }
        }
        return help;
    }

    public static Message.Text prepare(final InfluxManager<?> manager, final SpongeHelpEntry helpEntry, String message, int maxDescLength) {
        int aliases = helpEntry.getController().getCommand().getAliases().size();
        MessageBuilder.Text result = Messages.builder("");

        int next = 0;
        Matcher matcher = Pattern.compile("<(.+?)>").matcher(message);
        while (next < message.length()) {
            if (matcher.find()) {
                if (matcher.start() > next) {
                    result.append(Messages.of(manager.getResponder().format(message.substring(matcher.start(), next))));
                }
                next = matcher.end() + 1;
                switch (matcher.group(1)) {
                    case "command":
                        result.append(Messages.of(manager.getResponder().format(manager.getCommandPrefix() + helpEntry.getController().getCommand().getAcceptedStringSyntax(), false)))
                                .style(TextStyles.UNDERLINE)
                                .onHover(new HoverAction.ShowText() {
                                    @Override
                                    public String getId() {
                                        return "auto-complete";
                                    }

                                    @Override
                                    public Message getResult() {
                                        return Messages.of(manager.getResponder().format("{c2}Click to auto-complete"));
                                    }
                                })
                                .onClick(new ClickAction.SuggestCommand() {
                                    @Override
                                    public String getId() {
                                        return "entry-command";
                                    }

                                    @Override
                                    public String getResult() {
                                        return helpEntry.getController().getCommand().getAcceptedStringSyntax();
                                    }
                                });
                        break;
                    case "alias_num":
                        result.append(Messages.of(aliases + " alias" + (aliases == 1 ? "" : "es") + ""));
                        if (aliases > 1) {
                            result.onHover(new HoverAction.ShowText() {
                                @Override
                                public String getId() {
                                    return "aliases";
                                }

                                @Override
                                public Message getResult() {
                                    return Messages.of(manager.getResponder().format(StringUtil.combine("{c1}, {c2}", helpEntry.getController().getCommand().getReadableStringAliases())));
                                }
                            });
                        }
                        break;
                    case "short_desc":
                    case "long_desc":
                        String desc = matcher.group(1).equals("short_desc") ? helpEntry.getShortDescription() : StringUtil.combineArray("\n", helpEntry.getLongDescription()[0]);
                        if (maxDescLength > 0) {
                            desc = desc.length() > maxDescLength ? desc.substring(0, maxDescLength - 3) + "..." : desc;
                        }
                        result.append(Messages.of(desc));
                        break;
                    default:
                        result.append(Messages.of(manager.getResponder().format(matcher.group(0), false)));
                        break;
                }
            } else {
                // We're done
                result.append(Messages.of(message.substring(next)));
                break;
            }
        }

        return result.build();
    }
}