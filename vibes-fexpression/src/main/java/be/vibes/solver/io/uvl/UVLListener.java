package be.vibes.solver.io.uvl;

import be.vibes.fexpression.DimacsModel;
import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.*;
import be.vibes.solver.exception.SolverInitializationException;
import de.vill.exception.ParseError;
import de.vill.exception.ParseErrorList;
import de.vill.model.FeatureType;
import de.vill.model.LanguageLevel;
import de.vill.model.constraint.*;
import de.vill.model.expression.*;
import org.antlr.v4.runtime.Token;
import uvl.UVLJavaBaseListener;
import uvl.UVLJavaParser;

import java.util.*;

public class UVLListener extends UVLJavaBaseListener {
    private final FeatureModel<Feature> featureModel;
    private final Set<LanguageLevel> importedLanguageLevels = new HashSet<>(List.of(LanguageLevel.BOOLEAN_LEVEL));
    private final Stack<Feature> featureStack = new Stack<>();
    private final Stack<Group> groupStack = new Stack<>();

    private final Stack<FExpression> constraintStack = new Stack<>();

    private final Stack<Expression> expressionStack = new Stack<>();

    private final Stack<FExpression> subFexpStack = new Stack<>();

    private final List<ParseError> errorList = new LinkedList<>();
    private final SolverType solverType;

    public UVLListener(){
        super();
        this.featureModel = new FeatureModel<>();
        this.solverType = SolverType.SAT4J;
    }

    public UVLListener(SolverType type){
        super();
        this.featureModel = new FeatureModel<>();
        this.solverType = type;
    }

    @Override
    public void enterIncludes(UVLJavaParser.IncludesContext ctx) {
        errorList.add(new ParseError("Includes are not yet supported! "));
    }

    @Override
    public void exitIncludeLine(UVLJavaParser.IncludeLineContext ctx) {
        String[] levels = ctx.languageLevel().getText().split("\\.");
        if (levels.length == 1) {
            LanguageLevel majorLevel = LanguageLevel.getLevelByName(levels[0]);
            importedLanguageLevels.add(majorLevel);
        } else if (levels.length == 2) {
            LanguageLevel majorLevel = LanguageLevel.getLevelByName(levels[0]);
            List<LanguageLevel> minorLevels;
            if (levels[1].equals("*")) {
                minorLevels = LanguageLevel.valueOf(majorLevel.getValue() + 1);
            } else {
                minorLevels = new LinkedList<>();
                minorLevels.add(LanguageLevel.getLevelByName(levels[1]));
            }
            importedLanguageLevels.add(majorLevel);
            for (LanguageLevel minorLevel : minorLevels) {
                if (minorLevel.getValue() - 1 != majorLevel.getValue()) {
                    errorList.add(new ParseError("Minor language level " + minorLevel.getName() + " does not correspond to major language level " + majorLevel + " but to " + LanguageLevel.valueOf(minorLevel.getValue() - 1)));
                    //throw new ParseError("Minor language level " + minorLevel.getName() + " does not correspond to major language level " + majorLevel + " but to " + LanguageLevel.valueOf(minorLevel.getValue() - 1));
                }
                importedLanguageLevels.add(minorLevel);
            }
        } else {
            errorList.add(new ParseError("Invalid import Statement: " + ctx.languageLevel().getText()));
            //throw new ParseError("Invalid import Statement: " + ctx.LANGUAGELEVEL().getText());
        }
    }

    @Override
    public void exitNamespace(UVLJavaParser.NamespaceContext ctx) {
        featureModel.setNamespace(ctx.reference().getText().replace("\"", ""));
    }

    @Override
    public void enterImportLine(UVLJavaParser.ImportLineContext ctx) {
        errorList.add(new ParseError("Imports are not yet supported! "));
    }

    @Override
    public void enterFeatures(UVLJavaParser.FeaturesContext ctx) {
        groupStack.push(new Group(Group.GroupType.MANDATORY));
    }

    @Override
    public void exitFeatures(UVLJavaParser.FeaturesContext ctx) {
        Group group = groupStack.pop();
        Feature feature = group.getFeatures().getFirst();
        featureModel.setRootFeature(feature);
        feature.setParentGroup(null);
    }

    @Override
    public void enterOrGroup(UVLJavaParser.OrGroupContext ctx) {
        Group group = new Group(Group.GroupType.OR);
        Feature feature = featureStack.peek();
        feature.addChildren(group);
        group.setParentFeature(feature);
        groupStack.push(group);
    }

    @Override
    public void exitOrGroup(UVLJavaParser.OrGroupContext ctx) {
        groupStack.pop();
        FExpression rightConstraint = subFexpStack.pop();
        FExpression leftConstraint = subFexpStack.pop();
        FExpression constraint = leftConstraint.or(rightConstraint);
        subFexpStack.push(constraint);
    }

    @Override
    public void enterAlternativeGroup(UVLJavaParser.AlternativeGroupContext ctx) {
        Group group = new Group(Group.GroupType.ALTERNATIVE);
        Feature feature = featureStack.peek();
        feature.addChildren(group);
        group.setParentFeature(feature);
        groupStack.push(group);
    }

    @Override
    public void exitAlternativeGroup(UVLJavaParser.AlternativeGroupContext ctx) {
        groupStack.pop();
        FExpression rightConstraint = subFexpStack.pop();
        FExpression leftConstraint = subFexpStack.pop();
        FExpression c1 = leftConstraint.or(rightConstraint);
        FExpression c2 = (leftConstraint.not()).or(rightConstraint.not());
        FExpression constraint = c1.and(c2);
        subFexpStack.push(constraint);
    }

    @Override
    public void enterOptionalGroup(UVLJavaParser.OptionalGroupContext ctx) {
        Group group = new Group(Group.GroupType.OPTIONAL);
        Feature feature = featureStack.peek();
        feature.addChildren(group);
        group.setParentFeature(feature);
        groupStack.push(group);
    }

    @Override
    public void exitOptionalGroup(UVLJavaParser.OptionalGroupContext ctx) {
        groupStack.pop();
        FExpression c1 = subFexpStack.pop();
        FExpression constraint = c1.or(c1.not());
        subFexpStack.push(constraint);
    }

    @Override
    public void enterMandatoryGroup(UVLJavaParser.MandatoryGroupContext ctx) {
        Group group = new Group(Group.GroupType.MANDATORY);
        Feature feature = featureStack.peek();
        feature.addChildren(group);
        group.setParentFeature(feature);
        groupStack.push(group);
    }

    @Override
    public void exitMandatoryGroup(UVLJavaParser.MandatoryGroupContext ctx) {
        groupStack.pop();
    }

    @Override
    public void enterCardinalityGroup(UVLJavaParser.CardinalityGroupContext ctx) {
        errorList.add(new ParseError("Cardinalities are not yet supported! "));
    }

    @Override
    public void enterFeature(UVLJavaParser.FeatureContext ctx) {
        String featureReference = ctx.reference().getText().replace("\"", "");
        String[] featureReferenceParts = featureReference.split("\\.");
        String featureName;
        String featureNamespace;
        if (featureReferenceParts.length > 1) {
            featureName = featureReferenceParts[featureReferenceParts.length - 1];
            featureNamespace = featureReference.substring(0, featureReference.length() - featureName.length() - 1);
        } else {
            featureName = featureReferenceParts[0];
            featureNamespace = null;
        }

        Feature feature = new Feature(featureName);
        if (featureNamespace != null) {
            feature.setNameSpace(featureNamespace);
            feature.setSubmodelRoot(true);
            if (feature.getRelatedImport() == null) {
                errorList.add(new ParseError("Feature " + featureReference + " is imported, but there is no import named " + featureNamespace));
                //throw new ParseError("Feature " + featureReference + " is imported, but there is no import named " + featureNamespace);
            }
        }

        featureStack.push(feature);
        Group parentGroup = groupStack.peek();
        parentGroup.getFeatures().add(feature);
        feature.setParentGroup(parentGroup);
        if (featureNamespace == null) {
            featureModel.getFeatureMap().put(featureName, feature);
        } else {
            featureModel.getFeatureMap().put(featureNamespace + "." + featureName, feature);
        }

        FExpression fExpression = new FExpression(featureName);
        subFexpStack.push(fExpression);
    }

    @Override
    public void exitFeature(UVLJavaParser.FeatureContext ctx) {
        featureStack.pop();
    }

    @Override
    public void enterFeatureType(final UVLJavaParser.FeatureTypeContext ctx) {
        errorList.add(new ParseError("Feature types are not yet supported! "));
    }

    @Override
    public void exitFeatureCardinality(UVLJavaParser.FeatureCardinalityContext ctx) {
        String lowerBound;
        String upperBound;
        if (ctx.getText().contains("..")) {
            lowerBound = ctx.CARDINALITY().getText().replace("[", "").replace("]", "").split("\\.\\.")[0];
            upperBound = ctx.CARDINALITY().getText().replace("[", "").replace("]", "").split("\\.\\.")[1];
        } else {
            lowerBound = ctx.getText().replace("[", "").replace("]", "");
            upperBound = lowerBound;
        }
        if (upperBound.equals("*")) {
            errorList.add(new ParseError("Feature Cardinality must not have * as upper bound! (" + ctx.CARDINALITY().getText() + ")"));
            //throw new ParseError("Feature Cardinality must not have * as upper bound! (" + ctx.CARDINALITY().getText() + ")");
        }

        Feature feature = featureStack.peek();
        feature.setLowerBound(lowerBound);
        feature.setUpperBound(upperBound);
    }

    @Override
    public void enterAttributes(UVLJavaParser.AttributesContext ctx) {
        errorList.add(new ParseError("Attributes are not yet supported! "));
    }

    @Override
    public void enterSingleConstraintAttribute(UVLJavaParser.SingleConstraintAttributeContext ctx) {
        errorList.add(new ParseError("Attributes are not yet supported! "));
    }

    @Override
    public void exitListConstraintAttribute(UVLJavaParser.ListConstraintAttributeContext ctx) {
        errorList.add(new ParseError("Attributes are not yet supported! "));
    }


    @Override
    public void exitLiteralConstraint(UVLJavaParser.LiteralConstraintContext ctx) {
        String featureReference = ctx.reference().getText().replace("\"", "");

        LiteralConstraint constraint = new LiteralConstraint(featureReference);

        Token t = ctx.getStart();
        int line = t.getLine();
        constraint.setLineNumber(line);

        FExpression fExpression = new FExpression(featureReference);
        constraintStack.push(fExpression);
    }

    @Override
    public void enterParenthesisConstraint(UVLJavaParser.ParenthesisConstraintContext ctx) {
        errorList.add(new ParseError("ParenthesisConstraint are not yet supported."));
    }

    @Override
    public void exitNotConstraint(UVLJavaParser.NotConstraintContext ctx) {
        FExpression constraint = constraintStack.pop().not();
        constraintStack.push(constraint);
    }

    @Override
    public void exitAndConstraint(UVLJavaParser.AndConstraintContext ctx) {
        FExpression rightConstraint = constraintStack.pop();
        FExpression leftConstraint = constraintStack.pop();
        FExpression constraint = rightConstraint.and(leftConstraint);
        constraintStack.push(constraint);
    }

    @Override
    public void exitOrConstraint(UVLJavaParser.OrConstraintContext ctx) {
        super.exitOrConstraint(ctx);
        FExpression rightConstraint = constraintStack.pop();
        FExpression leftConstraint = constraintStack.pop();
        FExpression constraint = rightConstraint.or(leftConstraint);
        constraintStack.push(constraint);
    }

    @Override
    public void enterImplicationConstraint(UVLJavaParser.ImplicationConstraintContext ctx) {
        errorList.add(new ParseError("ImplicationConstraint are not yet supported."));
    }

    @Override
    public void enterEquivalenceConstraint(UVLJavaParser.EquivalenceConstraintContext ctx) {
        errorList.add(new ParseError("EquivalenceConstraint are not yet supported."));
    }

    @Override
    public void enterEqualEquation(UVLJavaParser.EqualEquationContext ctx) {
        errorList.add(new ParseError("Equations are not yet supported! "));
    }

    @Override
    public void enterLowerEquation(UVLJavaParser.LowerEquationContext ctx) {
        errorList.add(new ParseError("Equations are not yet supported! "));
    }

    @Override
    public void enterGreaterEquation(UVLJavaParser.GreaterEquationContext ctx) {
        errorList.add(new ParseError("Equations are not yet supported! "));
    }

    @Override
    public void enterLowerEqualsEquation(UVLJavaParser.LowerEqualsEquationContext ctx) {
        errorList.add(new ParseError("Equations are not yet supported! "));
    }

    @Override
    public void enterGreaterEqualsEquation(UVLJavaParser.GreaterEqualsEquationContext ctx) {
        errorList.add(new ParseError("Equations are not yet supported! "));
    }

    @Override
    public void enterNotEqualsEquation(UVLJavaParser.NotEqualsEquationContext ctx) {
        errorList.add(new ParseError("Equations are not yet supported! "));
    }

    @Override
    public void enterBracketExpression(UVLJavaParser.BracketExpressionContext ctx) {
        errorList.add(new ParseError("Equations are not yet supported! "));
    }

    @Override
    public void exitIntegerLiteralExpression(UVLJavaParser.IntegerLiteralExpressionContext ctx) {
        Expression expression = new NumberExpression(Integer.parseInt(ctx.INTEGER().getText()));
        expressionStack.push(expression);
        Token t = ctx.getStart();
        int line = t.getLine();
        expression.setLineNumber(line);
    }

    @Override
    public void exitStringLiteralExpression(UVLJavaParser.StringLiteralExpressionContext ctx) {
        Expression expression = new StringExpression(ctx.STRING().getText().replace("'", ""));
        expressionStack.push(expression);
        Token t = ctx.getStart();
        int line = t.getLine();
        expression.setLineNumber(line);
    }

    @Override
    public void exitFloatLiteralExpression(UVLJavaParser.FloatLiteralExpressionContext ctx) {
        Expression expression = new NumberExpression(Double.parseDouble(ctx.FLOAT().getText()));
        expressionStack.push(expression);
        Token t = ctx.getStart();
        int line = t.getLine();
        expression.setLineNumber(line);
    }

    @Override
    public void exitLiteralExpression(UVLJavaParser.LiteralExpressionContext ctx) {
        String reference = ctx.reference().getText().replace("\"", "");
        LiteralExpression expression = new LiteralExpression(reference);
        expressionStack.push(expression);
        Token t = ctx.getStart();
        int line = t.getLine();
        expression.setLineNumber(line);
    }

    @Override
    public void exitAddExpression(UVLJavaParser.AddExpressionContext ctx) {
        Expression right = expressionStack.pop();
        Expression left = expressionStack.pop();
        Expression expression = new AddExpression(left, right);
        expressionStack.push(expression);
        Token t = ctx.getStart();
        int line = t.getLine();
        expression.setLineNumber(line);
    }

    @Override
    public void exitSubExpression(UVLJavaParser.SubExpressionContext ctx) {
        Expression right = expressionStack.pop();
        Expression left = expressionStack.pop();
        Expression expression = new SubExpression(left, right);
        expressionStack.push(expression);
        Token t = ctx.getStart();
        int line = t.getLine();
        expression.setLineNumber(line);
    }

    @Override
    public void exitMulExpression(UVLJavaParser.MulExpressionContext ctx) {
        Expression right = expressionStack.pop();
        Expression left = expressionStack.pop();
        Expression expression = new MulExpression(left, right);
        expressionStack.push(expression);
        Token t = ctx.getStart();
        int line = t.getLine();
        expression.setLineNumber(line);
    }

    @Override
    public void exitDivExpression(UVLJavaParser.DivExpressionContext ctx) {
        Expression right = expressionStack.pop();
        Expression left = expressionStack.pop();
        Expression expression = new DivExpression(left, right);
        expressionStack.push(expression);
        Token t = ctx.getStart();
        int line = t.getLine();
        expression.setLineNumber(line);
    }

    @Override
    public void enterSumAggregateFunction(UVLJavaParser.SumAggregateFunctionContext ctx) {
        errorList.add(new ParseError("AggregateFunction are not yet supported! "));
    }

    @Override
    public void enterAvgAggregateFunction(UVLJavaParser.AvgAggregateFunctionContext ctx) {
        errorList.add(new ParseError("AggregateFunction are not yet supported! "));
    }

    @Override
    public void enterLengthAggregateFunction(UVLJavaParser.LengthAggregateFunctionContext ctx) {
        errorList.add(new ParseError("AggregateFunction are not yet supported! "));
    }

    @Override public void enterFloorAggregateFunction(UVLJavaParser.FloorAggregateFunctionContext ctx) {
        errorList.add(new ParseError("AggregateFunction are not yet supported! "));
    }

    @Override public void enterCeilAggregateFunction(UVLJavaParser.CeilAggregateFunctionContext ctx) {
        errorList.add(new ParseError("AggregateFunction are not yet supported! "));
    }

    public FExpression getConstraint() {
        if (!errorList.isEmpty()) {
            ParseErrorList parseErrorList = new ParseErrorList("Multiple Errors occurred during parsing!");
            parseErrorList.getErrorList().addAll(errorList);
            throw parseErrorList;
        }
        return constraintStack.pop();
    }

    public FeatureModel<Feature> getFeatureModel() {
        if (!errorList.isEmpty()) {
            ParseErrorList parseErrorList = new ParseErrorList("Multiple Errors occurred during parsing!");
            parseErrorList.getErrorList().addAll(errorList);
            throw parseErrorList;
        }

        SolverFacade solver = getSolverFacade(this.getFexpr());
        this.featureModel.setSolver(solver);

        return featureModel;
    }

    public FExpression getFexpr() {

        FExpression fexp = FExpression.trueValue();
        while (!subFexpStack.isEmpty()) {
            fexp.andWith(subFexpStack.pop());
        }

        while (!constraintStack.isEmpty()) {
            fexp.andWith(constraintStack.pop());
        }
        return fexp;
    }

    private SolverFacade getSolverFacade(FExpression featureDiagram) {

        DimacsModel model;
        try {
            model = DimacsModel.createFromFeatureList(featureDiagram);
        } catch (DimacsFormatException e) {
            throw new ParseError("Unable to initialise the DimacsModel.");
        }

        SolverFacade solver;

        switch (this.solverType) {
            case SAT4J -> {
                try {
                    solver = new Sat4JSolverFacade(model);
                } catch (SolverInitializationException e) {
                    throw new ParseError("Unable to initialise the SAT4J solver.");
                }
            }
            case BDD -> solver = new BDDSolverFacade(featureDiagram);
            default -> throw new UnsupportedOperationException("Only SAT4J and BDD solvers are currently supported. Default is SAT4J.");
        }
        return solver;
    }
}
