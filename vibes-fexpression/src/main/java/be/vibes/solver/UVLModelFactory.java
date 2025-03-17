package be.vibes.solver;

import be.vibes.solver.io.uvl.UVLListener;

import de.vill.exception.ParseError;
import de.vill.exception.ParseErrorList;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ExpressionConstraint;
import de.vill.model.expression.Expression;
import de.vill.util.Constants;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import uvl.UVLJavaLexer;
import uvl.UVLJavaParser;

import java.util.*;

public class UVLModelFactory {

    private final SolverType solverType;
    private final List<ParseError> errorList = new LinkedList<>();

    public UVLModelFactory(SolverType type) {
        switch (type) {
            case SAT4J -> this.solverType = SolverType.SAT4J;
            case BDD -> this.solverType = SolverType.BDD;
            default -> throw new UnsupportedOperationException("Only SAT4J and BDD solvers are currently supported. Default is SAT4J.");
        }
    }

    public UVLModelFactory() {
        this(SolverType.SAT4J);
    }

    /**
     * This method parses the givel text and returns a {@link FeatureModel} if everything is fine or throws a {@link ParseError} if something went wrong.
     *
     * @param text       A String that describes a feature model in UVL notation.
     * @return A {@link FeatureModel} based on the uvl text
     * @throws ParseError If there is an error during parsing or the construction of the feature model
     */
    public FeatureModel parse(String text) throws ParseError {
        FeatureModel featureModel = parseFeatureModel(text);
        validateTypeLevelConstraints(featureModel);
        return featureModel;
    }

    private FeatureModel parseFeatureModel(String text) {
        //remove leading and trailing spaces (to be more robust)
        text = text.trim();
        UVLJavaLexer UVLJavaLexer = new UVLJavaLexer(CharStreams.fromString(text));
        CommonTokenStream tokens = new CommonTokenStream(UVLJavaLexer);
        UVLJavaParser UVLJavaParser = new UVLJavaParser(tokens);
        UVLJavaParser.removeErrorListener(ConsoleErrorListener.INSTANCE);
        UVLJavaLexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

        UVLJavaLexer.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errorList.add(new ParseError(line, charPositionInLine, "failed to parse at line " + line + ":" + charPositionInLine + " due to: " + msg, e));
                //throw new ParseError(line, charPositionInLine,"failed to parse at line " + line + ":" + charPositionInLine + " due to: " + msg, e);
            }
        });
        UVLJavaParser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                errorList.add(new ParseError(line, charPositionInLine, "failed to parse at line " + line + ":" + charPositionInLine + " due to " + msg, e));
                //throw new ParseError(line, charPositionInLine,"failed to parse at line " + line + ":" + charPositionInLine + " due to " + msg, e);
            }
        });


        UVLListener uvlListener = new UVLListener(this.solverType);
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(uvlListener, UVLJavaParser.featureModel());
        FeatureModel featureModel = null;

        try {
            featureModel = uvlListener.getFeatureModel();
        } catch (ParseErrorList e) {
            errorList.addAll(e.getErrorList());
        }

        if (!errorList.isEmpty()) {
            ParseErrorList parseErrorList = new ParseErrorList("Multiple Errors occurred during parsing!");
            parseErrorList.getErrorList().addAll(errorList);
            throw parseErrorList;
        }

        return featureModel;
    }

    private void validateTypeLevelConstraints(final FeatureModel featureModel) {
        final List<Constraint> constraints = featureModel.getOwnConstraints();
        for (final Constraint constraint: constraints) {
            if (!validateTypeLevelConstraint(constraint)) {
                throw new ParseError("Invalid Constraint in line - " + constraint.getLineNumber());
            }
        }
    }

    private boolean validateTypeLevelConstraint(final Constraint constraint) {
        boolean result = true;
        if (constraint instanceof ExpressionConstraint) {
            String leftReturnType = ((ExpressionConstraint) constraint).getLeft().getReturnType();
            String rightReturnType = ((ExpressionConstraint) constraint).getRight().getReturnType();

            if (!(leftReturnType.equalsIgnoreCase(Constants.TRUE) || rightReturnType.equalsIgnoreCase(Constants.TRUE))) {
                // if not attribute constraint
                result = result && ((ExpressionConstraint) constraint).getLeft().getReturnType().equalsIgnoreCase(((ExpressionConstraint) constraint).getRight().getReturnType());
            }
            if (!result) {
                return false;
            }
            for (final Expression expr: ((ExpressionConstraint) constraint).getExpressionSubParts()) {
                result = result && validateTypeLevelExpression(expr);
            }
        }

        for (final Constraint subCons: constraint.getConstraintSubParts()) {
            result = result && validateTypeLevelConstraint(subCons);
        }

        return result;
    }

    private boolean validateTypeLevelExpression(final Expression expression) {
        final String initial = expression.getReturnType();
        boolean result = true;

        for (final Expression expr: expression.getExpressionSubParts()) {
            result = result && validateTypeLevelExpression(expr) && initial.equalsIgnoreCase(expr.getReturnType());
        }

        return result;
    }
}
