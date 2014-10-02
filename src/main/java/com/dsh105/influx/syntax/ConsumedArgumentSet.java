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

    private List<Syntax> candidates = new ArrayList<>();
    private boolean matches;
    private Map<String, String[]> consumedArguments = new HashMap<>();

    public ConsumedArgumentSet(Syntax candidate, String input) {
        this.candidate = candidate;
        this.input = input;

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
        String[] inputArgs = input.split("\\s+");
        int syntaxLength = syntax.getSyntax().size();
        int nextParameter = 0;

        for (int i = 0; i < syntaxLength; i++) {
            String consumedArguments = null;
            Parameter parameter = syntax.getParameter(i, false);
            if (parameter == null) {
                return false;
            }

            if (i == syntaxLength - 1 && nextParameter < inputArgs.length - 1) {
                // Final syntax parameter, but the input has more arguments
                if (!parameter.isContinuous()) {
                    return false;
                }
            }

            try {
                String arguments = inputArgs[nextParameter];
                if (parameter instanceof Variable) {
                    arguments = StringUtil.combineArray(nextParameter, nextParameter + ((Variable) parameter).getArgumentsAccepted(), " ", inputArgs);
                }

                if (!parameter.verify(arguments)) {
                    return false;
                }
                consumedArguments = arguments;
            } catch (ArrayIndexOutOfBoundsException e) {
                if (!parameter.isOptional()) {
                    return false;
                }

                if (parameter instanceof Variable) {
                    Variable variable = (Variable) parameter;
                    // Is this optional parameter in the middle?
                    // If so, does it have a default value?
                    if (i < syntaxLength - 1) {
                        if (variable.getDefaultValue() == null) {
                            // Optional parameters must have a default value unless they are the last parameter
                            return false;
                        }
                        nextParameter -= variable.getArgumentsAccepted();
                        consumedArguments = variable.getDefaultValue();
                    }
                }

            }

            int consumed = 1;
            if (parameter instanceof Variable) {
                consumed = ((Variable) parameter).getArgumentsAccepted();
            }
            nextParameter += consumed;

            if (consumedArguments == null) {
                return false;
            }
            consume(parameter, consumedArguments.split("\\s+"));
        }

        return true;
    }
}