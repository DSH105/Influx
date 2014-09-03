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

package com.dsh105.influx.dispatch;

import com.dsh105.commodus.StringUtil;
import com.dsh105.influx.Controller;
import com.dsh105.influx.format.FormatSet;
import com.dsh105.influx.Manager;
import com.dsh105.influx.format.MessagePurpose;
import com.dsh105.influx.context.CommandEvent;
import com.dsh105.influx.syntax.Suggestion;
import com.dsh105.influx.syntax.SyntaxBuilder;
import com.dsh105.influx.syntax.parameter.Parameter;
import com.dsh105.influx.syntax.parameter.Variable;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;

public class Dispatcher implements CommandExecutor {

    private Manager manager;

    public Dispatcher(Manager manager) {
        this.manager = manager;
    }

    public Manager getManager() {
        return manager;
    }

    public Controller match(String... arguments) {
        return match(StringUtil.combineArray(" ", arguments).trim());
    }

    public Controller match(String input) {
        for (Controller controller : manager) {
            if (controller.matches(input)) {
                return controller;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> arguments = new ArrayList<>();
        Collections.addAll(arguments, args);
        arguments.add(0, command.getLabel());
        return dispatch(sender, arguments.toArray(new String[0]));
    }

    public <T extends CommandSender> boolean dispatch(T sender, String... arguments) {
        String input = StringUtil.combineArray(" ", arguments).trim();
        Controller controller = match(input);
        if (controller != null) {
            return dispatch(new CommandEvent<>(getManager(), controller, input, sender));
        }

        // command not found - do something else with it
        getManager().getFormatSet().send(sender, getManager().getMessage(MessagePurpose.COMMAND_NOT_FOUND), FormatSet.SEVERE);

        Suggestion suggestion = new Suggestion(getManager(), input);
        if (!suggestion.getSuggestions().isEmpty()) {
            String suggestions = StringUtil.combine(ChatColor.RESET + "{c1}, " + ChatColor.ITALIC, suggestion.getSuggestions());
            getManager().getFormatSet().send(sender, "Did you mean: " + suggestions);
        }
        return true;
    }

    public <T extends CommandSender> boolean dispatch(CommandEvent<T> event) {
        try {

            if (event.getCommand().acceptsSender(event.sender())) {
                event.respond(getManager().getMessage(MessagePurpose.NO_ACCESS), FormatSet.SEVERE);
                return true;
            }

            List<String> permissions = preparePermissions(event);
            if (permissions.size() > 0 && !testPermissions(event.sender(), permissions)) {
                String response = getManager().getMessage(event.getCommand().getVariables().size() > 0 ? MessagePurpose.NO_PERMISSION_WITH_VAR_RECOMMENDATION : MessagePurpose.NO_PERMISSION);
                event.respond(response, FormatSet.SEVERE);
                return true;
            }

            // Begin actually executing/invoking the command
            boolean result = event.getController().getCallable().call(event);

            if (!result) {
                for (String part : event.getController().getDescription().getUsage()) {
                    event.respond(part.replace("<command>", event.getCommand().getStringSyntax()));
                }
            }
        } catch (Exception e) {
            event.respond(getManager().getMessage(MessagePurpose.UNEXPECTED_ERROR), FormatSet.SEVERE);
            new CommandInvocationException("Unhandled exception executing \""  + event.getCommand().getStringSyntax() + "\" (from \"" + event.getInput() + "\") for " + getManager().getPlugin().getName(), e).printStackTrace();
        }
        return true;
    }

    private boolean testPermissions(CommandSender sender, String... permissions) {
        return testPermissions(sender, Arrays.asList(permissions));
    }

    private boolean testPermissions(CommandSender sender, Collection<String> permissions) {
        for (String permission : permissions) {
            if (!permission.isEmpty() && sender.hasPermission(permission)) {
                continue;
            }
            return false;
        }
        return true;
    }

    private List<String> preparePermissions(CommandEvent event) {
        List<String> permissions = new ArrayList<>();
        for (String permission : event.getCommand().getPermissions()) {
            SyntaxBuilder syntaxBuilder = new SyntaxBuilder(permission);

            // Permissions *should* be without spaces
            // Accounted for anyway, just in case
            String modifiedPermission = permission;
            for (Parameter parameter : syntaxBuilder.getParameters()) {
                if (parameter instanceof Variable) {
                    Variable variable = (Variable) parameter;
                    modifiedPermission = modifiedPermission.replace(variable.getFullName(), event.var(variable.getName()));
                    continue;
                }

                if (parameter.containsInnerVariables()) {
                    for (Variable variable : parameter.getInnerVariables()) {
                        modifiedPermission = modifiedPermission.replace(variable.getFullName(), event.var(variable.getName()));
                    }
                }
            }

            permissions.add(permission);
        }
        return permissions;
    }
}