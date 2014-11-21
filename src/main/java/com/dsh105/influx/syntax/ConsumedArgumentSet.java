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

import com.dsh105.commodus.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumedArgumentSet {

    private Syntax candidate;
    private String input;
    private String[] arguments;

    private List<Syntax> candidates = new ArrayList<>();
    private boolean matches;
    private Map<String, String[]> consumedArguments = new HashMap<>();

    public ConsumedArgumentSet(Syntax candidate, String input) {
        this.candidate = candidate;
        this.input = input;
        this.arguments = new ArgumentParser(this.input).getArguments();

        this.candidates.add(candidate);
        if (candidate instanceof Command) {
            this.candidates.addAll(((Command) candidate).getAliases());
        }
        
        this.matches = compare();
    }
    
    public String[] getConsumedArguments(Parameter parameter) {
        return consumedArguments.get(parameter.getFullName());
    }

    public Syntax getCandidate() {
        return candidate;
    }

    public String getInput() {
        return input;
    }

    public String[] getArguments() {
        return arguments;
    }

    public boolean matches() {
        return matches;
    }

    private void consume(Parameter parameter, String... consumedArguments) {
        consume(parameter.getFullName(), consumedArguments);
    }

    private void consume(String parameterName, String... consumedArguments) {
        this.consumedArguments.put(parameterName, consumedArguments);
    }

    private boolean compare() {
        for (Syntax syntax : candidates) {
            if (compare(syntax)) {
                return true;
            }
        }

        return false;
    }

    private boolean compare(Syntax syntax) {
        int syntaxLength = syntax.getSyntax().size();
        int nextParameter = 0;

        for (int i = 0; i < syntaxLength; i++) {
            String consumedArguments = null;
            Parameter parameter = syntax.getParameter(i, false);
            if (parameter == null) {
                return false;
            }

            if (i == syntaxLength - 1 && nextParameter < arguments.length - 1) {
                // Final syntax parameter, but the input has more arguments
                if (!parameter.isContinuous()) {
                    return false;
                }
            }

            int consumed = parameter instanceof Variable ? ((Variable) parameter).getArgumentsAccepted() : 1;

            try {
                String arguments = this.arguments[nextParameter];
                if (parameter instanceof Variable) {
                    arguments = StringUtil.combineArray(nextParameter, nextParameter + consumed, " ", this.arguments);
                }

                if (!parameter.verify(arguments)) {
                    // May be a variable parameter that isn't specified
                    throw new VerificationException();
                }
                consumedArguments = arguments;
            } catch (ArrayIndexOutOfBoundsException | VerificationException e) {
                if (!parameter.isOptional()) {
                    return false;
                }

                if (parameter instanceof Variable) {
                    Variable variable = (Variable) parameter;
                    // Optional parameters must have a default value unless they are the last parameter
                    if (variable.getDefaultValue() == null) {
                        // Is this optional parameter in the middle?
                        // If so, does it have a default value?
                        if (i < syntaxLength - 1) {
                            return false;
                        }
                        consumedArguments = "";
                    } else {
                        consumedArguments = variable.getDefaultValue();
                    }
                    consumed -= variable.getArgumentsAccepted();
                }

            }

            if (consumedArguments == null) {
                return false;
            }

            nextParameter += consumed;
            consume(parameter, new ArgumentParser(consumedArguments).getArguments());
        }

        return true;
    }
}