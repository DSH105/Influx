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
import com.dsh105.influx.InfluxManager;
import com.dsh105.influx.conversion.ConversionException;
import com.dsh105.influx.help.Suggestion;
import com.dsh105.influx.response.MessagePurpose;
import com.dsh105.influx.response.ResponseLevel;
import com.dsh105.influx.response.ResponseUnsupportedException;
import com.dsh105.influx.syntax.*;

import java.util.*;

public class Dispatcher<S> {

    protected Map<String, Map<Controller, ConsumedArgumentSet>> consumedArgumentSets = new HashMap<>();
    protected Map<String, FuzzyArgumentMatcher> fuzzyMatchers = new HashMap<>();

    private InfluxManager<S> manager;

    public Dispatcher(InfluxManager<S> manager) {
        this.manager = manager;
    }

    public InfluxManager<S> getManager() {
        return manager;
    }

    public Controller match(String... arguments) {
        return match(StringUtil.combineArray(" ", arguments).trim());
    }

    public Controller match(String input) {
        Map<Controller, ConsumedArgumentSet> existing = consumedArgumentSets.get(input);
        if (existing != null) {
            for (Controller key : existing.keySet()) {
                if (existing.get(key).matches()) {
                    return key;
                }
            }
        } else {
            existing = new HashMap<>();
        }

        for (Controller candidate : manager) {
            ConsumedArgumentSet argumentSet = new ConsumedArgumentSet(candidate.getCommand(), input);

            existing.put(candidate, argumentSet);
            consumedArgumentSets.put(input, existing);

            if (argumentSet.matches()) {
                return candidate;
            }
        }
        return null;
    }

    public SortedSet<Controller> findFuzzyMatches(String input) {
        FuzzyArgumentMatcher fuzzyArgumentMatcher = fuzzyMatchers.get(input);
        if (fuzzyArgumentMatcher == null) {
            fuzzyArgumentMatcher = new FuzzyArgumentMatcher(getManager(), input);
            fuzzyMatchers.put(input, fuzzyArgumentMatcher);
        }
        return Collections.unmodifiableSortedSet(fuzzyArgumentMatcher.getHighestPossibleMatches());
    }

    public <T extends S> boolean dispatch(T sender, String... arguments) {
        String input = StringUtil.combineArray(" ", arguments).trim();

        Controller controller = match(input);

        if (controller != null) {
            return preDispatch(sender, controller, input);
        }

        // command not found - do something else with it
        getManager().respond(sender, getManager().getMessage(MessagePurpose.COMMAND_NOT_FOUND, "<command>", getManager().getCommandPrefix() + input), ResponseLevel.SEVERE);

        Suggestion suggestion = new Suggestion(getManager(), input, 3);
        if (!suggestion.getSuggestions().isEmpty()) {
            String suggestions = StringUtil.combine("{c1}, {c2}", suggestion.getSuggestions());
            getManager().respond(sender, getManager().getMessage(MessagePurpose.SUGGESTIONS, "<suggestions>", suggestions), ResponseLevel.SEVERE);
        }
        return true;
    }

    public <T extends S> boolean preDispatch(T sender, Controller controller, String input) {
        return dispatch(new CommandContext<>(getManager(), controller, sender, consumedArgumentSets.get(input).get(controller)));
    }

    public boolean dispatch(CommandContext<S> context) {
        try {
            if (!context.getCommand().acceptsSender(context.sender().getClass())) {
                context.respond(getManager().getMessage(MessagePurpose.RESTRICTED_SENDER), ResponseLevel.SEVERE);
                return true;
            }

            List<String> permissions = preparePermissions(context);
            if (permissions.size() > 0 && !getManager().authorize(context.sender(), context.getController(), permissions)) {
                String response = getManager().getMessage(context.getCommand().getVariables().size() > 0 ? MessagePurpose.RESTRICTED_PERMISSION_WITH_VAR_RECOMMENDATION : MessagePurpose.RESTRICTED_PERMISSION);
                context.respond(response, ResponseLevel.SEVERE);
                return true;
            }

            // Begin actually executing/invoking the command
            boolean result = context.getController().getCommandInvoker().invoke(context);

            if (!result) {
                for (String part : context.getController().getDescription().getUsage()) {
                    context.respond(part.replace("<command>", getManager().getCommandPrefix() + context.getCommand().getAcceptedStringSyntax()));
                }
            }
        } catch (ConversionException | ResponseUnsupportedException e) {
            context.respond(e.getMessage(), ResponseLevel.SEVERE);
        } catch (Exception e) {
            //context.respond(getManager().getMessage(MessagePurpose.UNEXPECTED_ERROR), ResponseLevel.SEVERE);
            throw new CommandDispatchException("Unhandled exception executing \"" + context.getCommand().getAcceptedStringSyntax() + "\" (\"" + context.getInput() + "\")", e);
        }
        return true;
    }

    protected List<String> preparePermissions(CommandContext context) {
        List<String> permissions = new ArrayList<>();
        for (String permission : context.getCommand().getPermissions()) {
            SyntaxBuilder syntaxBuilder;
            try {
                syntaxBuilder = new SyntaxBuilder(permission);
            } catch (IllegalSyntaxException e) {
                continue;
            }

            // Permissions *should* be without spaces
            // Accounted for anyway, just in case
            String modifiedPermission = permission;
            for (Parameter parameter : syntaxBuilder.getParameters()) {
                if (parameter instanceof Variable) {
                    Variable variable = (Variable) parameter;
                    modifiedPermission = modifiedPermission.replace(variable.getFullName(), context.var(variable.getName()));
                    continue;
                }

                if (parameter.containsInnerVariables()) {
                    for (Variable variable : parameter.getInnerVariables()) {
                        modifiedPermission = modifiedPermission.replace(variable.getFullName(), context.var(variable.getName()));
                    }
                }
            }

            // Ensure no variables are still defined in the permission
            // Most likely optional parameters that haven't been defined
            if (SyntaxBuilder.VARIABLE_PATTERN.matcher(modifiedPermission).find()) {
                continue;
            }

            permissions.add(permission);
        }
        return permissions;
    }
}