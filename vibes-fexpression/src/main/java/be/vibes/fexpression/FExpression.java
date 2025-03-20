package be.vibes.fexpression;

import be.vibes.fexpression.configuration.Configuration;
import be.vibes.fexpression.exception.FExpressionException;
import com.bpodgursky.jbool_expressions.*;
import com.bpodgursky.jbool_expressions.rules.RuleSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.*;

/*
 * #%L
 * VIBeS: featured expressions
 * %%
 * Copyright (C) 2014 PReCISE, University of Namur
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
public class FExpression {

    private Expression<Feature<?>> expression;

    public static FExpression trueValue() {
        return new FExpression(Literal.of(true));
    }

    public static FExpression falseValue() {
        return new FExpression(Literal.of(false));
    }

    public static FExpression featureExpr(String name) {
        return new FExpression(name);
    }

    public static FExpression featureExpr(Feature<?> feat) {
        return new FExpression(feat);
    }

    private FExpression(Expression<Feature<?>> expression) {
        this.expression = expression;
    }

    public FExpression(Feature<?> feat) {
        expression = Variable.of(feat);
    }

    public FExpression(String featureName) {
        this(new Feature<>(featureName));
    }

    // Accessors 
    Expression<Feature<?>> getExpression() {
        return expression;
    }

    public Set<Feature<?>> getFeatures() {
        return ExprUtil.getVariables(expression);
    }

    public boolean isTrue() {
        return expression.equals(Literal.getTrue());
    }

    public boolean isFalse() {
        return expression.equals(Literal.getFalse());
    }

    // FExpression built methods
    public FExpression applySimplification() {
        Expression<Feature<?>> expr = RuleSet.simplify(expression);
        return new FExpression(expr);
    }

    public FExpression and(FExpression expr) {
        return new FExpression(And.of(this.expression, expr.getExpression()));
    }

    public void andWith(FExpression expr) {
        expression = And.of(expression, expr.getExpression());
    }

    public FExpression or(FExpression expr) {
        return new FExpression(Or.of(this.expression, expr.getExpression()));
    }

    public void orWith(FExpression expr) {
        expression = Or.of(expression, expr.getExpression());
    }

    public FExpression not() {
        return new FExpression(Not.of(this.expression));
    }

    public void notWith() {
        expression = Not.of(expression);
    }
    
    public FExpression copy(){
        return new FExpression(expression);
    }

    // Normal forms
    public FExpression toCnf() {
        return new FExpression(RuleSet.toCNF(expression));
    }

    public FExpression toDnf() {
        return new FExpression(RuleSet.toCNF(expression));
    }

    // Assignements 
    
    public FExpression assign(Configuration config) {
        Map<Feature<?>, Boolean> assignements = Maps.newHashMap();
        for(Feature<?> f : getFeatures()){
            assignements.put(f, config.isSelected(f));
        }
        return assign(assignements);
    }

    public FExpression assignTrue(Feature<?> f) {
        Map<Feature<?>, Boolean> assignements = Maps.newHashMap();
        assignements.put(f, Boolean.TRUE);
        return assign(assignements);
    }
    
    public FExpression assignTrue(Iterable<Feature<?>> trueFeatures) {
        Map<Feature<?>, Boolean> assignements = Maps.newHashMap();
        for (Feature<?> f : trueFeatures) {
            assignements.put(f, Boolean.TRUE);
        }
        return assign(assignements);
    }
    
    public FExpression assignFalse(Feature<?> f) {
        Map<Feature<?>, Boolean> assignements = Maps.newHashMap();
        assignements.put(f, Boolean.FALSE);
        return assign(assignements);
    }

    public FExpression assignFalse(Iterable<Feature<?>> falseFeatures) {
        Map<Feature<?>, Boolean> assignements = Maps.newHashMap();
        for (Feature<?> f : falseFeatures) {
            assignements.put(f, Boolean.FALSE);
        }
        return assign(assignements);
    }
        
    public FExpression assign(Map<Feature<?>, Boolean> assignements) {
        Expression<Feature<?>> expr = RuleSet.assign(expression, assignements);
        return new FExpression(expr);
    }

    // Method Redefinitions
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.expression);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FExpression other)) {
            return false;
        }
        return Objects.equals(this.expression, other.expression);
    }

    @Override
    public String toString() {
        try {
            return accept(new FExpressionToString()).toString();
        } catch (FExpressionException e) {
            throw new RuntimeException("Error while converting FExpression to string", e);
        }
    }

    private static class FExpressionToString implements FExpressionVisitorWithReturn<StringBuilder> {

        @Override
        public StringBuilder constant(boolean value) {
            return new StringBuilder(value ? "true" : "false");
        }

        @Override
        public StringBuilder feature(Feature<?> feature) {
            return new StringBuilder(feature.getFeatureName());
        }

        @Override
        public StringBuilder not(FExpression expr) throws FExpressionException {
            return new StringBuilder("!").append(expr.accept(this));
        }

        @Override
        public StringBuilder and(List<FExpression> operands) throws FExpressionException {
            return joinExpressions(operands, " && ");
        }

        @Override
        public StringBuilder or(List<FExpression> operands) throws FExpressionException {
            return joinExpressions(operands, " || ");
        }

        private StringBuilder joinExpressions(List<FExpression> operands, String operator) throws FExpressionException {
            if (operands.isEmpty()) {
                return new StringBuilder();
            }

            StringBuilder fexpStr = new StringBuilder("(");
            Iterator<FExpression> it = operands.iterator();
            fexpStr.append(it.next().accept(this));

            while (it.hasNext()) {
                fexpStr.append(operator).append(it.next().accept(this));
            }

            return fexpStr.append(")");
        }
    }


    //Visitor Pattern
    public <E> E accept(FExpressionVisitorWithReturn<E> visitor) throws FExpressionException {
        Expression<Feature<?>> exp = getExpression();

        switch (exp) {
            case Literal<?> literal -> {
                return visitor.constant(isTrue());
            }
            case Variable<Feature<?>> variable -> {
                return visitor.feature(variable.getValue());
            }
            case Not<Feature<?>> not -> {
                return visitor.not(new FExpression(not.getE()));
            }
            case And<?> and -> {
                List<FExpression> operands = Lists.newArrayList();
                for (Expression<Feature<?>> e : exp.getChildren()) {
                    operands.add(new FExpression(e));
                }
                return visitor.and(operands);
            }
            case Or<?> or -> {
                List<FExpression> operands = Lists.newArrayList();
                for (Expression<Feature<?>> e : exp.getChildren()) {
                    operands.add(new FExpression(e));
                }
                return visitor.or(operands);
            }
            case null, default ->
                    throw new IllegalStateException("Some FExpression elements have not been implemented in the visitor!");
        }
    }

}
