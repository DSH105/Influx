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

package com.dsh105.influx.util;

import com.dsh105.influx.Controller;
import com.dsh105.influx.syntax.Command;
import com.dsh105.influx.syntax.Parameter;
import com.dsh105.influx.syntax.Syntax;
import com.dsh105.influx.syntax.Variable;

import java.util.Comparator;

/**
 * Note: these are used purely for sorting commands into a list when searching for the appropriate commands to execute.
 * They should otherwise NOT be used to compare commands.
 */
public class Comparators {

    public static class ControllerComparator implements Comparator<Controller> {

        @Override
        public int compare(Controller controller1, Controller controller2) {
            int comparison = new CommandComparator().compare(controller1.getCommand(), controller2.getCommand());
            return comparison != 0 ? comparison : (controller1.equals(controller2) ? 0 : -1);
        }
    }

    public static class SyntaxComparator implements Comparator<Syntax> {

        @Override
        public int compare(Syntax syntax1, Syntax syntax2) {
            for (int i = 0; i < syntax1.getSyntax().size() && i < syntax2.getSyntax().size(); i++) {
                Parameter parameter = syntax1.getSyntax().get(i);
                Parameter parameter2 = syntax2.getSyntax().get(i);

                if (parameter instanceof Variable && parameter2 instanceof Variable) {
                    boolean last1 = parameter.equals(syntax1.getFinalParameter());
                    boolean last2 = parameter2.equals(syntax2.getFinalParameter());
                    if (last1 != last2) {
                        return last1 ? 1 : -1;
                    }
                }

                int parameterComparison = new VariableComparator().compare(parameter, parameter2);
                if (parameterComparison != 0) {
                    return parameterComparison;
                }
            }
            int sizeDiff = syntax1.getSyntax().size() - syntax2.getSyntax().size();
            if (syntax1.getFirstVariable() != null && syntax2.getFirstVariable() != null) {
                return syntax2.getFirstVariable().getRange().getStartIndex() - syntax1.getFirstVariable().getRange().getStartIndex();
            }

            return sizeDiff != 0 ? sizeDiff : syntax1.getStringSyntax().compareTo(syntax2.getStringSyntax());
        }
    }

    public static class CommandComparator extends SyntaxComparator {

        @Override
        public int compare(Syntax syntax1, Syntax syntax2) {
            if (syntax1 instanceof Command && syntax2 instanceof Command) {
                int priorityDiff = ((Command) syntax2).getPriority().ordinal() - ((Command) syntax1).getPriority().ordinal();
                if (priorityDiff != 0) {
                    return priorityDiff;
                }
            }

            return super.compare(syntax1, syntax2);
        }
    }

    public static class ParameterComparator implements Comparator<Parameter> {

        @Override
        public int compare(Parameter parameter1, Parameter parameter2) {
            boolean isVar1 = parameter1 instanceof Variable;
            boolean isVar2 = parameter2 instanceof Variable;
            if (isVar1 != isVar2) {
                int result = new VariableComparator().compare(parameter1, parameter2);
                ;
                return isVar1 ? result : result * -1;
            }

            if (parameter1.getFullName().equals(parameter2.getFullName())) {
                return 0;
            }

            if (parameter1.containsInnerVariables() != parameter2.containsInnerVariables()) {
                return parameter1.containsInnerVariables() ? 1 : -1;
            }

            return parameter1.getInnerVariables().size() - parameter2.getInnerVariables().size();
        }
    }

    public static class VariableComparator extends ParameterComparator {

        @Override
        public int compare(Parameter parameter1, Parameter parameter2) {
            boolean isVar1 = parameter1 instanceof Variable;
            boolean isVar2 = parameter2 instanceof Variable;
            if (!isVar1 && !isVar2) {
                return super.compare(parameter1, parameter2);
            }
            if (isVar1 != isVar2) {
                return isVar1 ? 1 : -1;
            }

            if (parameter1.isContinuous() != parameter2.isContinuous()) {
                return parameter1.isContinuous() ? 1 : -1;
            }

            if (parameter1.isOptional() != parameter2.isOptional()) {
                return parameter1.isOptional() ? 1 : -1;
            }

            if (((Variable) parameter1).isRegexEnabled() == ((Variable) parameter2).isRegexEnabled()) {
                return 0;
            }
            return ((Variable) parameter1).isRegexEnabled() ? -1 : 1;
        }
    }
}