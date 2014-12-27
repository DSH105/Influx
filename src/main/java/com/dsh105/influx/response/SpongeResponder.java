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
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.message.Message;
import org.spongepowered.api.text.message.MessageBuilder;
import org.spongepowered.api.text.message.Messages;
import org.spongepowered.api.util.command.CommandSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpongeResponder extends Responder<CommandSource> {

    private TextColor[] warningScheme = {TextColors.GOLD, TextColors.YELLOW};
    private TextColor[] severeScheme = {TextColors.DARK_RED, TextColors.RED};
    private TextColor[] normalScheme = {TextColors.WHITE};

    public SpongeResponder(String responsePrefix) {
        super(responsePrefix);
    }

    public void respond(CommandSource sender, Message.Text message) {
        respond(sender, message, ResponseLevel.DEFAULT);
    }

    public void respond(CommandSource sender, Message.Text message, ResponseLevel level) {
        Message.Text response = message;
        if (!getResponsePrefix().isEmpty()) {
            response = Messages.builder(getResponsePrefix() + " ").append(format(message)).build();
        }
        respondAnonymously(sender, response, level);
    }

    public void respondAnonymously(CommandSource sender, Message.Text message) {
        respondAnonymously(sender, message, ResponseLevel.DEFAULT);
    }

    public void respondAnonymously(CommandSource sender, Message.Text message, ResponseLevel level) {
        handleResponse(sender, format(message), level);
    }

    @Override
    public void handleResponse(CommandSource sender, String message, ResponseLevel level) {
        sender.sendMessage(message);
    }

    public void handleResponse(CommandSource sender, Message.Text message, ResponseLevel level) {
        sender.sendMessage(message);
    }

    public Message.Text format(Message.Text message) {
        return format(message, ResponseLevel.DEFAULT);
    }

    public Message.Text format(Message.Text message, boolean includeFirstColour) {
        return format(message, ResponseLevel.DEFAULT, includeFirstColour);
    }

    public Message.Text format(Message.Text message, ResponseLevel level) {
        return format(message, level, true);
    }

    public Message.Text format(Message.Text message, ResponseLevel level, boolean includeFirstColour) {
        MessageBuilder.Text result = Messages.builder("" + (includeFirstColour ? getResponseColor(1, getScheme(level)) : ""));

        String content = (includeFirstColour ? "{c1}" : "") + message.getContent();
        Matcher matcher = Pattern.compile("\\{c([0-9]+)\\}").matcher(content);
        int next = 0;
        while (next < content.length()) {
            if (matcher.find()) {
                if (matcher.start() > next) {
                    result.append(Messages.of(content.substring(matcher.start(), next)));
                    if (message.getStyle() != null) {
                        result.style(message.getStyle());
                    }
                }
                result.color(getResponseColor(Integer.parseInt(matcher.group(1)), getScheme(level)));
                next = matcher.end() + 1;
            } else {
                // We're finished
                result.append(Messages.of(content.substring(next)));
                break;
            }
        }
        if (message.getClickAction().isPresent()) {
            result.onClick(message.getClickAction().get());
        }
        if (message.getHoverAction().isPresent()) {
            result.onHover(message.getHoverAction().get());
        }
        if (message.getShiftClickAction().isPresent()) {
            result.onShiftClick(message.getShiftClickAction().get());
        }
        return result.build();
    }

    @Override
    public String getFormat(int index, ResponseLevel level) {
        return getResponseColor(index, getScheme(level)) + "";
    }

    public TextColor getResponseColor(int index) {
        return getResponseColor(index, normalScheme);
    }

    public TextColor getResponseColor(int index, TextColor... scheme) {
        Affirm.notNull(scheme, "Format scheme must not be null.");
        if (index > scheme.length) {
            return TextColors.WHITE;
        }
        return scheme[index - 1];
    }

    public TextColor[] getWarningScheme() {
        return warningScheme;
    }

    public void setWarningScheme(TextColor[] warningScheme) {
        this.warningScheme = warningScheme;
    }

    public TextColor[] getSevereScheme() {
        return severeScheme;
    }

    public void setSevereScheme(TextColor[] severeScheme) {
        this.severeScheme = severeScheme;
    }

    public TextColor[] getNormalScheme() {
        return normalScheme;
    }

    public TextColor[] getScheme(ResponseLevel level) {
        switch (level) {
            case WARNING:
                return warningScheme;
            case SEVERE:
                return severeScheme;
            default:
                return normalScheme;
        }
    }

    public void setMessageFormats(TextColor... messageFormats) {
        if (messageFormats == null) {
            this.normalScheme = new TextColor[]{TextColors.WHITE};
            return;
        }
        this.normalScheme = messageFormats;
    }
}