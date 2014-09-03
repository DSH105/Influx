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

import com.dsh105.influx.annotation.Accept;
import com.dsh105.influx.annotation.Convert;
import com.dsh105.influx.annotation.Nested;
import com.dsh105.influx.annotation.Restrict;
import com.dsh105.influx.dispatch.CommandCallable;
import com.dsh105.influx.dispatch.PreparedCallable;
import com.dsh105.influx.syntax.Command;
import com.dsh105.influx.syntax.CommandBinding;
import com.dsh105.influx.syntax.Syntax;
import com.dsh105.influx.syntax.WrappedCommandMethod;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandBuilder {

    private CommandListener listener;
    private String syntax;
    private String shortDescription;
    private String[] longDescription = new String[0];
    private String[] usage = new String[0];
    private Accept.Behaviour acceptance = Accept.Behaviour.ALL;
    private CommandCallable commandCallable = new PreparedCallable();
    private CommandBinding commandBinding;
    private List<String> aliases = new ArrayList<>();
    private List<String> permissions = new ArrayList<>();

    protected boolean isNested;

    public CommandBuilder from(com.dsh105.influx.annotation.Command commandAnnotation) {
        this.syntax(commandAnnotation.syntax());
        this.withAliases(commandAnnotation.aliases());
        this.describeAs(commandAnnotation.desc(), commandAnnotation.help());
        this.usage(commandAnnotation.usage());
        return this;
    }

    public CommandBuilder from(CommandListener listener, String methodName) {
        Preconditions.checkNotNull(listener, "Listener must not be null.");
        Preconditions.checkNotNull(methodName, "Method name must not be null.");
        this.listener = listener;
        this.commandBinding = new WrappedCommandMethod(listener, methodName);

        Method method = commandBinding.getCallableMethod();

        if (method.isAnnotationPresent(com.dsh105.influx.annotation.Command.class)) {
            throw new InvalidCommandException("@Command annotation not present on " + listener.getClass().getSimpleName() + "#" + methodName + "");
        }

        from(method.getAnnotation(com.dsh105.influx.annotation.Command.class));
        isNested = method.isAnnotationPresent(Nested.class);
        if (method.isAnnotationPresent(Accept.class)) {
            accept(method.getAnnotation(Accept.class).value());
        }
        if (method.isAnnotationPresent(Restrict.class)) {
            restrict(method.getAnnotation(Restrict.class).value());
        }
        return this;
    }

    public CommandBuilder describeAs(String shortDescription, String... longDescription) {
        Preconditions.checkNotNull(shortDescription, "Short description must not be null.");
        this.shortDescription = shortDescription;
        this.longDescription = longDescription != null ? longDescription : new String[0];
        return this;
    }

    public CommandBuilder usage(String... usage) {
        this.usage = usage != null ? usage : new String[0];
        return this;
    }

    public CommandBuilder accept(Accept.Behaviour acceptance) {
        Preconditions.checkNotNull(acceptance, "Command acceptance must not be null.");
        this.acceptance = acceptance;
        return this;
    }

    public CommandBuilder syntax(String syntax) {
        Preconditions.checkNotNull(syntax, "Syntax must not be null.");
        this.syntax = syntax.trim();
        return this;
    }

    public CommandBuilder restrict(String... permissions) {
        if (permissions != null) {
            Collections.addAll(this.permissions, permissions);
        }
        return this;
    }

    public CommandBuilder withAliases(String... aliases) {
        if (aliases != null) {
            Collections.addAll(this.aliases, aliases);
        }
        return this;
    }

    public CommandBuilder callUsing(CommandCallable commandCallable) {
        Preconditions.checkNotNull(commandCallable, "Command callable name must not be null.");
        this.commandCallable = commandCallable;
        return this;
    }

    public CommandListener getListener() {
        return listener;
    }

    public String getSyntax() {
        return syntax;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String[] getLongDescription() {
        return longDescription;
    }

    public String[] getUsage() {
        return usage;
    }

    public Accept.Behaviour getAcceptance() {
        return acceptance;
    }

    public CommandCallable getCommandCallable() {
        return commandCallable;
    }

    public CommandBinding getCommandBinding() {
        return commandBinding;
    }

    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }

    public List<String> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public Controller build() {
        Preconditions.checkNotNull(listener, "Command has not been bound to a listener.");
        Preconditions.checkNotNull(syntax, "Valid syntax has not been provided.");
        Preconditions.checkNotNull(shortDescription, "Description has not been provided");
        Preconditions.checkNotNull(commandBinding, "Command has not been bound to a method or listener.");
        Description description = new Description(shortDescription, longDescription, usage);
        Command command = new Command(listener, getSenderType(commandBinding.getCallableMethod()), syntax, commandBinding, permissions, acceptance, aliases);
        return new Controller(command, description, commandBinding, commandCallable);
    }

    public Controller build(String... parents) {
        Preconditions.checkNotNull(parents, "Parent must not be null.");
        Controller controller = build();
        controller.getCommand().nestUnder(parents);
        return controller;
    }

    private Class<? extends CommandSender> getSenderType(Method method) {
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        for (Type genericType : genericParameterTypes) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                Type[] paramArgTypes = parameterizedType.getActualTypeArguments();
                for (Type paramArgType : paramArgTypes) {
                    if (paramArgType != null) {
                        try {
                            return (Class<? extends CommandSender>) paramArgType;
                        } catch (ClassCastException e) {
                            // Unlikely...
                            throw new InvalidCommandException("Command parameter type is invalid - must extend CommandSender.", e);
                        }
                    }
                }
            }
        }
        return CommandSender.class;
    }
}