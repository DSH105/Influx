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

import com.dsh105.influx.dispatch.Authorization;
import com.dsh105.influx.dispatch.Dispatcher;
import com.dsh105.influx.response.MessagePurpose;
import com.dsh105.influx.help.HelpProvider;
import com.dsh105.influx.help.HelpProvision;
import com.dsh105.influx.registration.RegistrationStrategy;
import com.dsh105.influx.registration.Registry;
import com.dsh105.influx.response.DefaultResponder;
import com.dsh105.influx.response.Responder;
import com.dsh105.influx.response.ResponseLevel;
import com.dsh105.influx.util.Replacer;
import com.google.common.base.Preconditions;
import org.bukkit.material.Sandstone;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager<S> extends CommandMapping implements InfluxManager<S> {

    private String helpTitle;
    private String commandPrefix;

    protected Dispatcher<S> dispatcher;
    private Registry registry;
    private HelpProvider<?, S> help;
    private Responder<S> responder;
    private Authorization<S> authorization;

    //protected Class<T> senderType = GeneralUtil.getSenderTypeFor(this); - TODO: fixit
    protected Class<?> senderType = Object.class;

    private Map<MessagePurpose, String> messages = new HashMap<>();

    public CommandManager(RegistrationStrategy registrationStrategy, String commandPrefix) {
        this.commandPrefix = commandPrefix;
        this.dispatcher = new Dispatcher<>(this);
        this.responder = new DefaultResponder<>("");
        this.setRegistrationStrategy(registrationStrategy);
        this.setHelpProvision(HelpProvision.CONDENSED);
        this.setAuthorization(new Authorization<S>() {
            @Override
            public boolean authorize(S sender, Controller toExecute, String permission) {
                return true;
            }
        });
    }

    public CommandManager(RegistrationStrategy registrationStrategy, String commandPrefix, String helpTitle) {
        this(registrationStrategy, commandPrefix);
        this.helpTitle = helpTitle;
    }

    @Override
    public Class<?> getSenderType() {
        return senderType;
    }

    @Override
    public Registry getRegistry() {
        return registry;
    }

    @Override
    public String getCommandPrefix() {
        return commandPrefix;
    }

    @Override
    public void setRegistrationStrategy(RegistrationStrategy strategy) {
        Preconditions.checkNotNull(strategy, "Registration strategy must not be null.");
        this.setRegistrationStrategy(strategy.prepare(this));
    }

    @Override
    public HelpProvider<?, S> getHelp() {
        return help;
    }

    @Override
    public void setRegistrationStrategy(Registry registry) {
        Preconditions.checkNotNull(registry, "Registry must not be null.");
        if (this.registry != null) {
            this.registry.unregisterAll();
        }
        this.registry = registry;
        this.registry.register(getMappedCommands());
    }

    @Override
    public void setHelpProvision(HelpProvision provision) {
        Preconditions.checkNotNull(provision, "Help provision strategy must not be null.");
        this.help = provision.newProvider(this);
    }

    @Override
    public void setHelpProvision(HelpProvider<?, S> provider) {
        Preconditions.checkNotNull(provider, "Help provider must not be null.");
        this.help = provider;
    }

    @Override
    public Responder<S> getResponder() {
        return responder;
    }

    @Override
    public <T extends S> void setResponseHandler(Responder<T> responder) {
        Preconditions.checkNotNull(responder, "Response handler must not be null.");
        this.responder = (Responder<S>) responder;
    }

    @Override
    public Dispatcher<S> getDispatcher() {
        return dispatcher;
    }

    @Override
    public Authorization<S> getAuthorization() {
        return authorization;
    }

    @Override
    public <T extends S> void setAuthorization(Authorization<T> authorization) {
        Preconditions.checkNotNull(authorization, "Authorization must not be null.");
        this.authorization = (Authorization<S>) authorization;
    }

    @Override
    public void setMessage(MessagePurpose purpose, String value) {
        Preconditions.checkNotNull(purpose, "Purpose must not be null.");
        Preconditions.checkNotNull(value, "Message must not be null.");
        this.messages.put(purpose, value);
    }

    @Override
    public String getMessage(MessagePurpose purpose, Object... pairedReplacements) {
        Preconditions.checkNotNull(purpose, "Purpose must not be null.");
        if (!messages.containsKey(purpose)) {
            messages.put(purpose, purpose.getDefaultValue());
        }
        return Replacer.makeReplacements(messages.get(purpose), pairedReplacements);
    }

    @Override
    public String getHelpTitle() {
        return helpTitle;
    }

    @Override
    public void setHelpTitle(String helpTitle) {
        Preconditions.checkNotNull(helpTitle, "Help title must not be null.");
        this.helpTitle = helpTitle;
    }

    @Override
    public <T extends S> void respond(T sender, String response) {
        this.responder.respond(sender, response);
    }

    @Override
    public <T extends S> void respond(T sender, String response, ResponseLevel level) {
        this.responder.respond(sender, response, level);
    }

    @Override
    public <T extends S> void respondAnonymously(T sender, String response) {
        this.responder.respondAnonymously(sender, response);
    }

    @Override
    public <T extends S> void respondAnonymously(T sender, String response, ResponseLevel level) {
        this.responder.respondAnonymously(sender, response, level);
    }

    @Override
    public <T extends S> boolean authorize(T sender, Controller toExecute) {
        return authorize(sender, toExecute, toExecute.getCommand().getPermissions());
    }

    @Override
    public <T extends S> boolean authorize(T sender, Controller toExecute, String permission) {
        return getAuthorization().authorize(sender, toExecute, permission);
    }

    @Override
    public <T extends S> boolean authorize(T sender, Controller toExecute, Collection<String> permissions) {
        for (String permission : permissions) {
            if (!getAuthorization().authorize(sender, toExecute, permission)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Controller nestCommandIn(CommandListener destination, CommandBuilder builder, String... parentNests) {
        Controller controller = super.nestCommandIn(destination, builder, parentNests);
        if (controller != null) {
            getHelp().add(controller);
        }
        return controller;
    }

    @Override
    public List<Controller> unregister(CommandListener listener) {
        List<Controller> unregistered = super.unregister(listener);
        for (Controller controller : unregistered) {
            getHelp().remove(controller);
        }
        return unregistered;
    }

    @Override
    public boolean unregister(Controller controller) {
        if (super.unregister(controller)) {
            getHelp().remove(controller);
            return true;
        }
        return false;
    }
}