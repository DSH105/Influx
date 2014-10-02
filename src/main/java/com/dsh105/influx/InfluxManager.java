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
import com.dsh105.influx.registration.Registry;
import com.dsh105.influx.response.MessagePurpose;
import com.dsh105.influx.help.HelpProvider;
import com.dsh105.influx.help.HelpProvision;
import com.dsh105.influx.registration.RegistrationStrategy;
import com.dsh105.influx.response.Responder;
import com.dsh105.influx.response.ResponseLevel;

import java.util.Collection;

public interface InfluxManager<S> extends InfluxMapping {

    @Override
    Class<?> getSenderType();

    @Override
    Registry getRegistry();



    String getCommandPrefix();

    void setRegistrationStrategy(RegistrationStrategy strategy);

    void setRegistrationStrategy(Registry registry);

    HelpProvider<?, S> getHelp();

    void setHelpProvision(HelpProvision provision);

    void setHelpProvision(HelpProvider<?, S> provider);

    Responder<S> getResponder();

    <T extends S> void setResponseHandler(Responder<T> responder);

    Dispatcher<S> getDispatcher();

    Authorization<S> getAuthorization();

    <T extends S> void setAuthorization(Authorization<T> authorization);

    void setMessage(MessagePurpose purpose, String value);

    String getMessage(MessagePurpose purpose, Object... pairedReplacements);

    String getHelpTitle();

    void setHelpTitle(String helpTitle);

    <T extends S> void respond(T sender, String response);

    <T extends S> void respond(T sender, String response, ResponseLevel level);

    <T extends S> void respondAnonymously(T sender, String response);

    <T extends S> void respondAnonymously(T sender, String response, ResponseLevel level);

    <T extends S> boolean authorize(T sender, Controller toExecute);

    <T extends S> boolean authorize(T sender, Controller toExecute, String permission);

    <T extends S> boolean authorize(T sender, Controller toExecute, Collection<String> permissions);
}