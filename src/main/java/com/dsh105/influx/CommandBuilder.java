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

import com.dsh105.influx.annotation.Authorize;
import com.dsh105.influx.annotation.Nested;
import com.dsh105.influx.annotation.Priority;
import com.dsh105.influx.dispatch.CommandInvoker;
import com.dsh105.influx.dispatch.InjectedInvoker;
import com.dsh105.influx.syntax.AnnotatedCommandBinding;
import com.dsh105.influx.syntax.Command;
import com.dsh105.influx.syntax.CommandBinding;
import com.dsh105.influx.syntax.IllegalSyntaxException;
import com.dsh105.influx.util.GeneralUtil;
import com.google.common.base.Preconditions;

import java.lang.reflect.Method;
import java.util.*;

public class CommandBuilder {

    private CommandListener originListener;
    private String syntax;
    private String shortDescription;
    private String[] longDescription = new String[0];
    private String[] usage = new String[0];
    private Priority.Type priority = Priority.Type.NORMAL;
    private CommandInvoker commandInvoker = new InjectedInvoker();
    private CommandBinding commandBinding;
    private Set<String> aliases = new HashSet<>();
    private Set<String> permissions = new HashSet<>();

    protected boolean nested;

    public CommandBuilder from(com.dsh105.influx.annotation.Command commandAnnotation) {
        this.syntax(commandAnnotation.syntax());
        this.withAliases(commandAnnotation.aliases());
        this.describeAs(commandAnnotation.desc(), commandAnnotation.help());
        this.usage(commandAnnotation.usage());
        return this;
    }

    public CommandBuilder from(CommandListener listener, Method method) throws IllegalCommandException {
        return from(listener, method.getName(), method.getParameterTypes());
    }

    public CommandBuilder from(CommandListener listener, String methodName, Class<?>... parameterTypes) throws IllegalCommandException {
        Preconditions.checkNotNull(listener, "Listener must not be null.");
        Preconditions.checkNotNull(methodName, "Method name must not be null.");

        this.originListener = listener;
        this.commandBinding = new AnnotatedCommandBinding(this.originListener, methodName, parameterTypes);
        Method method = commandBinding.getCallableMethod();

        this.nested = method.isAnnotationPresent(Nested.class);

        if (!method.isAnnotationPresent(com.dsh105.influx.annotation.Command.class)) {
            throw new IllegalCommandException("@Command annotation not present on " + listener.getClass().getSimpleName() + "#" + methodName + "");
        }

        from(method.getAnnotation(com.dsh105.influx.annotation.Command.class));
        if (method.isAnnotationPresent(Authorize.class)) {
            restrict(method.getAnnotation(Authorize.class).value());
        }
        if (method.isAnnotationPresent(Priority.class)) {
            prioritise(method.getAnnotation(Priority.class).value());
        }

        return this;
    }

    public CommandBuilder syntax(String syntax) {
        Preconditions.checkNotNull(syntax, "Syntax must not be null.");
        this.syntax = syntax.trim();
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

    public CommandBuilder prioritise(Priority.Type priority) {
        Preconditions.checkNotNull(priority, "Priority must not be null.");
        this.priority = priority;
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

    public CommandBuilder callUsing(CommandInvoker commandInvoker) {
        Preconditions.checkNotNull(commandInvoker, "Command invoker must not be null.");
        this.commandInvoker = commandInvoker;
        return this;
    }

    public CommandListener getOriginListener() {
        return originListener;
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

    public Priority.Type getPriority() {
        return priority;
    }

    public Set<String> getAliases() {
        return Collections.unmodifiableSet(aliases);
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public CommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    public CommandBinding getCommandBinding() {
        return commandBinding;
    }

    public Controller build() throws IllegalCommandException {
        return build(getOriginListener());
    }

    public Controller build(CommandListener destinationListener) throws IllegalCommandException {
        Preconditions.checkNotNull(originListener, "Command has not been bound to a originListener.");
        Preconditions.checkNotNull(syntax, "Valid syntax has not been provided.");
        Preconditions.checkNotNull(shortDescription, "Description has not been provided");
        Preconditions.checkNotNull(commandBinding, "Command has not been bound to a method or originListener.");
        Description description = new Description(shortDescription, longDescription, usage);
        Command command = null;
        try {
            command = new Command(originListener, GeneralUtil.getSenderTypeFor(commandBinding.getCallableMethod()), syntax, commandBinding, permissions, priority, aliases);
        } catch (IllegalSyntaxException e) {
            throw new IllegalCommandException("Invalid command syntax provided.", e);
        }
        return new Controller(destinationListener, command, description, commandBinding, commandInvoker);
    }

    public Controller build(String... parents) throws IllegalCommandException {
        return build(getOriginListener(), parents);
    }

    public Controller build(CommandListener destinationListener, String... parents) throws IllegalCommandException {
        Controller controller = build(destinationListener);
        if (parents != null) {
            controller.getCommand().nestUnder(parents);
        }
        return controller;
    }
}