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

import com.dsh105.influx.CommandListener;
import com.dsh105.influx.annotation.Priority;
import com.google.common.base.Preconditions;

import java.util.*;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Command extends Syntax {

    private final String originalStringSyntax;
    private final Set<Syntax> originalAliases;

    private CommandListener originListener;
    private Class<?> acceptedSender;
    private Priority.Type priority;
    private Set<String> permissions;
    private Set<Syntax> aliases;

    public Command(CommandListener originListener, Class<?> acceptedSender, String stringSyntax, CommandBinding commandBinding, Set<String> permissions, Priority.Type priority, Collection<String> aliases) throws IllegalSyntaxException {
        super(stringSyntax, commandBinding);
        this.originalStringSyntax = getStringSyntax();
        this.originListener = originListener;
        this.acceptedSender = acceptedSender;
        this.permissions = permissions;
        this.priority = priority;
        this.aliases = new HashSet<>();
        this.originalAliases = new HashSet<>();

        this.addAliases(aliases);
    }

    public CommandListener getOriginListener() {
        return originListener;
    }

    public Priority.Type getPriority() {
        return priority;
    }

    public Class<?> getAcceptedSenderType() {
        return acceptedSender;
    }

    public boolean acceptsSender(Class<?> senderType) {
        return getAcceptedSenderType().isAssignableFrom(senderType);
    }

    public Set<Syntax> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    public void addAliases(Collection<String> aliases) {
        this.addAliases(aliases.toArray(new String[0]));
    }

    public void addAliases(String... aliases) {
        for (String alias : aliases) {
            Syntax syntax;
            try {
                syntax = new Syntax(alias, getCommandBinding());
            } catch (IllegalSyntaxException e) {
                throw new IllegalArgumentException("Illegal syntax: " + alias, e);
            }
            this.aliases.add(syntax);
            this.originalAliases.add(syntax);
        }
    }

    public List<String> getStringAliases() {
        List<String> stringAliases = new ArrayList<>();
        for (Syntax syntax : getAliases()) {
            stringAliases.add(syntax.getStringSyntax());
        }
        return Collections.unmodifiableList(stringAliases);
    }

    public List<String> getReadableStringAliases() {
        List<String> stringAliases = new ArrayList<>();
        for (Syntax syntax : getAliases()) {
            stringAliases.add(syntax.getReadableSyntax());
        }
        return Collections.unmodifiableList(stringAliases);
    }

    public Set<String> getCommandNames() {
        Set<String> commands = new HashSet<>();
        commands.add(getCommandName().split("\\s+")[0]);
        commands.addAll(getAliasNames());
        return Collections.unmodifiableSet(commands);
    }

    public Set<String> getAliasNames() {
        Set<String> aliases = new HashSet<>();
        for (String alias : getStringAliases()) {
            aliases.add(alias.split("\\s+")[0]);
        }
        return Collections.unmodifiableSet(aliases);
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void addPermissions(String... permissions) {
        Preconditions.checkNotNull(permissions, "Parent permissions must not be null.");
        Collections.addAll(this.permissions, permissions);
    }

    public void nestUnder(String... parents) {
        if (parents == null || parents.length <= 0) {
            // Reset everything
            try {
                this.buildSyntax(this.originalStringSyntax);
            } catch (IllegalSyntaxException e) {
                throw new IllegalArgumentException("Illegal syntax: " + this.originalStringSyntax, e);
            }
            return;
        }

        Set<Syntax> aliases = new HashSet<>();
        for (int i = 0; i < parents.length; i++) {
            String parent = parents[i];

            for (Syntax syntax : this.originalAliases) {
                aliases.add(prepareSyntaxNesting(parent, syntax.getStringSyntax()));
            }
            if (i > 0) {
                aliases.add(prepareSyntaxNesting(parent, this.originalStringSyntax));
            }
        }
        this.aliases = aliases;
        // Use the first one as the primary prefix
        this.startIndex = parents[0].split("\\s+").length;
        String nestedSyntax = prepareNesting(parents[0], this.originalStringSyntax);
        try {
            this.buildSyntax(nestedSyntax);
        } catch (IllegalSyntaxException e) {
            throw new IllegalArgumentException("Illegal syntax: " + nestedSyntax, e);
        }
    }

    private String prepareNesting(String parent, String syntax) {
        return (parent + " " + syntax).replaceAll("\\s+", " ").trim();
    }

    private Syntax prepareSyntaxNesting(String parent, String syntax) {
        String combinedSyntax = prepareNesting(parent, syntax);
        try {
            return new Syntax(combinedSyntax, getCommandBinding(), parent.split("\\s+").length);
        } catch (IllegalSyntaxException e) {
            throw new IllegalArgumentException("Illegal syntax: " + combinedSyntax, e);
        }
    }

    @Override
    public int compareTo(Syntax syntax) {
        if (syntax instanceof Command) {
            int priorityDiff = getPriority().ordinal() - ((Command) syntax).getPriority().ordinal();
            if (priorityDiff != 0) {
                return priorityDiff;
            }
        }

        return super.compareTo(syntax);
    }

    @Override
    public String toString() {
        return "Command{" +
                "originalStringSyntax='" + originalStringSyntax + "'" +
                ", originalAliases=" + originalAliases +
                ", listener=" + originListener +
                ", acceptedSender=" + acceptedSender +
                ", priority=" + priority +
                ", permissions=" + permissions +
                ", aliases=" + aliases +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Command)) return false;
        if (!super.equals(o)) return false;

        Command command = (Command) o;

        if (!acceptedSender.equals(command.acceptedSender)) return false;
        if (!aliases.equals(command.aliases)) return false;
        if (!originalAliases.equals(command.originalAliases)) return false;
        if (!originalStringSyntax.equals(command.originalStringSyntax)) return false;
        if (!permissions.equals(command.permissions)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + originalStringSyntax.hashCode();
        result = 31 * result + originalAliases.hashCode();
        result = 31 * result + acceptedSender.hashCode();
        result = 31 * result + permissions.hashCode();
        result = 31 * result + aliases.hashCode();
        return result;
    }
}