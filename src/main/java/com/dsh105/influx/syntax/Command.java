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

package com.dsh105.influx.syntax;

import com.dsh105.influx.Controller;
import com.dsh105.influx.CommandListener;
import com.dsh105.influx.annotation.Accept;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;

import java.util.*;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Command extends Syntax {

    private CommandListener listener;
    private CommandNest nest;
    private Class<? extends CommandSender> acceptedSender;
    private Accept.Behaviour acceptance;
    private List<String> permissions;
    private List<Syntax> aliases;

    public Command(CommandListener listener, Class<? extends CommandSender> acceptedSender, String stringSyntax, CommandBinding commandBinding, List<String> permissions, Accept.Behaviour acceptance, Collection<String> aliases) {
        super(stringSyntax, commandBinding);
        this.listener = listener;
        this.acceptedSender = acceptedSender;
        this.permissions = permissions;
        this.acceptance = acceptance;
        this.aliases = new ArrayList<>();
        for (String alias : aliases) {
            this.aliases.add(new Syntax(alias, getCommandBinding()));
        }
    }

    public Command(CommandListener listener, Class<? extends CommandSender> acceptedSender, String stringSyntax, CommandBinding commandBinding, List<String> permissions, Accept.Behaviour acceptance, String... aliases) {
        this(listener, acceptedSender, stringSyntax, commandBinding, permissions, acceptance, Arrays.asList(aliases));
    }

    public CommandListener getListener() {
        return listener;
    }

    public CommandNest getNest() {
        return nest;
    }

    public Class<? extends CommandSender> getAcceptedSenderType() {
        return acceptedSender;
    }

    public boolean acceptsSender(CommandSender sender) {
        return acceptsSender(sender.getClass());
    }

    public boolean acceptsSender(Class<? extends CommandSender> senderType) {
        return getAcceptedSenderType().isAssignableFrom(senderType);
    }

    public List<Syntax> getAliases() {
        return Collections.unmodifiableList(aliases);
    }

    public List<String> getStringAliases() {
        List<String> stringAliases = new ArrayList<>();
        for (Syntax syntax : getAliases()) {
            stringAliases.add(syntax.toString());
        }
        return Collections.unmodifiableList(stringAliases);
    }

    public Set<String> getCommandNames() {
        Set<String> commands = new HashSet<>();
        commands.add(getCommandName().split("\\s")[0]);
        commands.addAll(getAliasNames());
        return Collections.unmodifiableSet(commands);
    }

    public Set<String> getAliasNames() {
        Set<String> aliases = new HashSet<>();
        for (String alias : getStringAliases()) {
            aliases.add(alias.split("\\s")[0]);
        }
        return Collections.unmodifiableSet(aliases);
    }

    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public Accept.Behaviour getAcceptance() {
        return acceptance;
    }

    public void nestUnder(String... parents) {
        Preconditions.checkNotNull(parents, "Parent commands must not be null.");

        for (String parent : parents) {
            this.buildSyntax((parent + " " + stringSyntax).replaceAll("\\s+", " ").trim());
            List<Syntax> aliases = new ArrayList<>();
            aliases.addAll(getAliases());
            for (Syntax syntax : getAliases()) {
                aliases.add(new Syntax((parent + " " + syntax.getStringSyntax()).replaceAll("\\s+", " ").trim(), getCommandBinding()));
            }
            this.aliases = aliases;
        }
    }

    public void nestUnderPermissions(String... permissions) {
        Preconditions.checkNotNull(permissions, "Parent permissions must not be null.");
        Collections.addAll(this.permissions, permissions);
    }

    @Override
    public int compareTo(Syntax syntax) {
        if (syntax instanceof Command) {
            if (getAcceptance() != ((Command) syntax).getAcceptance()) {
                return getAcceptance() == Accept.Behaviour.ALL ? 1 : -1;
            }
        }

        return super.compareTo(syntax);
    }
}