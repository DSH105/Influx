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

import com.dsh105.influx.util.Affirm;

import java.util.*;

/**
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class Syntax implements Comparable<Syntax>, Iterable<Parameter> {

    protected String stringSyntax;
    protected List<Parameter> syntax;
    private CommandBinding commandBinding;
    private Map<String, Parameter> parameterNameMap;
    private Map<String, Variable> variableNameMap;
    protected int startIndex = 0;

    public Syntax(String stringSyntax) throws IllegalSyntaxException {
        this(stringSyntax, 0);
    }

    public Syntax(String stringSyntax, CommandBinding commandBinding) throws IllegalSyntaxException {
        this(stringSyntax, commandBinding, 0);
    }

    public Syntax(String stringSyntax, int startIndex) throws IllegalSyntaxException {
        this(stringSyntax, null, startIndex);
    }

    public Syntax(String stringSyntax, CommandBinding commandBinding, int startIndex) throws IllegalSyntaxException {
        this.commandBinding = commandBinding;
        this.startIndex = startIndex;
        this.parameterNameMap = new HashMap<>();
        this.variableNameMap = new HashMap<>();
        this.buildSyntax(stringSyntax);
    }

    protected void buildSyntax(String stringSyntax) throws IllegalSyntaxException {
        Affirm.notNull(stringSyntax, "Syntax must not be null.");
        this.stringSyntax = stringSyntax;
        this.syntax = new SyntaxBuilder(getStringSyntax(), getCommandBinding()).getParameters();
        parameterNameMap.clear();
        variableNameMap.clear();
        for (Parameter parameter : getSyntax()) {
            if (parameter instanceof Variable) {
                variableNameMap.put(parameter.getName(), (Variable) parameter);
            } else {
                parameterNameMap.put(parameter.getFullName(), parameter);
            }
            if (parameter.containsInnerVariables()) {
                for (Variable innerVariable : parameter.getInnerVariables()) {
                    variableNameMap.put(innerVariable.getName(), innerVariable);
                }
            }
        }
    }

    public String getCommandName() {
        return getStringSyntax().split("\\s+")[0];
    }

    public String getStringSyntax() {
        return stringSyntax;
    }

    public String getReadableSyntax() {
        if (getVariables().isEmpty()) {
            return stringSyntax;
        }

        String syntax = "";
        for (Parameter parameter : getSyntax()) {
            ParameterBinding parameterBinding = getCommandBinding().getBinding(parameter);
            if (parameterBinding != null && !parameterBinding.getAlternateName().isEmpty()) {
                syntax += parameterBinding.getAlternateName() + " ";
                continue;
            }
            syntax += parameter.getFullName() + " ";
        }
        return syntax.trim();
    }

    public List<Parameter> getSyntax() {
        return Collections.unmodifiableList(syntax);
    }

    public CommandBinding getCommandBinding() {
        return commandBinding;
    }

    public int getIndexOf(Parameter parameter) {
        return getIndexOf(parameter, true);
    }

    public int getIndexOf(Parameter parameter, boolean allowIndexFixing) {
        int index = getSyntax().indexOf(parameter) - (allowIndexFixing ? startIndex : 0);
        if (index < 0) {
            return -1;
        }
        return index;
    }

    public Parameter getParentOf(Variable variable) {
        for (Parameter parameter : getSyntax()) {
            if (parameter.containsInnerVariables()) {
                for (Variable candidate : parameter.getInnerVariables()) {
                    if (variable.equals(candidate)) {
                        return parameter;
                    }
                }
            }
        }
        return null;
    }

    public List<Variable> getVariables() {
        return getParameters(Variable.class);
    }

    public Variable getFirstVariable() {
        if (getVariables().isEmpty()) {
            return null;
        }
        return getVariables().get(0);
    }

    public Variable getFirstVerifiableVariable() {
        for (Variable variable : getParameters(Variable.class)) {
            if (variable.isRegexEnabled()) {
                return variable;
            }
        }
        // No regex-enabled variables
        return null;
    }

    private <T extends Parameter> List<T> getParameters(Class<T> typeRestriction) {
        List<T> parameters = new ArrayList<>();
        for (Parameter parameter : getSyntax()) {
            if (typeRestriction.isAssignableFrom(parameter.getClass())) {
                parameters.add((T) parameter);
            }
        }
        // If it's empty, this syntax contains no parameters of the given type
        return Collections.unmodifiableList(parameters);
    }

    @Deprecated // this method isn't safe to use with params changing positions all over the place (not good for aliases!)
    public Parameter getParameter(int index) {
        return getParameter(index, true);
    }

    // Positions aren't necessarily guaranteed to be the same
    @Deprecated // this method isn't safe to use with params changing positions all over the place (not good for aliases!)
    public Parameter getParameter(int index, boolean allowIndexFixing) {
        int searchIndex = index + (allowIndexFixing ? startIndex : 0);
        if (searchIndex >= getSyntax().size()) {
            Parameter parameter = getSyntax().get(getSyntax().size() - 1);
            if (!parameter.isContinuous()) {
                // It doesn't exist
                return null;
            }
            return parameter;
        }

        return getSyntax().get(searchIndex);
    }

    public Parameter getParameter(String name) {
        Parameter parameter = variableNameMap.containsKey(name) ? variableNameMap.get(name) : parameterNameMap.get(name);
        if (parameter == null) {
            return null;
        }
        return parameter;
    }

    public Parameter getFinalParameter() {
        return getParameter(getSyntax().size() - 1, false);
    }

    @Override
    public int compareTo(Syntax syntax) {
        for (int i = 0; i < getSyntax().size() && i < syntax.getSyntax().size(); i++) {
            Parameter parameter = getSyntax().get(i);
            Parameter parameter2 = syntax.getSyntax().get(i);
            int parameterComparison = parameter.compareTo(parameter2);
            if (parameterComparison != 0) {
                return parameterComparison;
            }
        }
        int sizeDiff = syntax.getSyntax().size() - getSyntax().size();
        int varPos = getFirstVariable() != null ? getFirstVariable().getRange().getStartIndex() : 0;
        int varPos2 = syntax.getFirstVariable() != null ? syntax.getFirstVariable().getRange().getStartIndex() : 0;

        int varDiff = varPos - varPos2;
        return sizeDiff != 0 ? sizeDiff : (varDiff != 0 ? varDiff : (equals(syntax) ? 0 : 1));
    }

    @Override
    public Iterator<Parameter> iterator() {
        return getSyntax().iterator();
    }

    @Override
    public String toString() {
        return "Syntax{" +
                "stringSyntax='" + stringSyntax + "'" +
                ", syntax=" + syntax +
                ", commandBinding=" + commandBinding +
                ", parameterNameMap=" + parameterNameMap +
                ", variableNameMap=" + variableNameMap +
                ", startIndex=" + startIndex +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Syntax)) return false;

        Syntax that = (Syntax) o;

        if (startIndex != that.startIndex) return false;
        if (commandBinding != null ? !commandBinding.equals(that.commandBinding) : that.commandBinding != null) {
            return false;
        }
        if (!parameterNameMap.equals(that.parameterNameMap)) return false;
        if (!stringSyntax.equals(that.stringSyntax)) return false;
        if (!syntax.equals(that.syntax)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = stringSyntax.hashCode();
        result = 31 * result + syntax.hashCode();
        result = 31 * result + (commandBinding != null ? commandBinding.hashCode() : 0);
        result = 31 * result + parameterNameMap.hashCode();
        result = 31 * result + startIndex;
        return result;
    }
}