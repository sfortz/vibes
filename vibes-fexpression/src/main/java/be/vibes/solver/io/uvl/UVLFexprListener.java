package be.vibes.solver.io.uvl;

import uvl.UVLJavaParser;
import be.vibes.fexpression.FExpression;
import de.vill.main.UVLListener;

import java.util.Stack;

public class UVLFexprListener extends UVLListener {

    private final Stack<FExpression> subFexpStack = new Stack<>();
    private final Stack<FExpression> constraintStack = new Stack<>();

    @Override
    public void exitOrGroup(UVLJavaParser.OrGroupContext ctx) {
        super.exitOrGroup(ctx);
        FExpression rightConstraint = subFexpStack.pop();
        FExpression leftConstraint = subFexpStack.pop();
        FExpression constraint = leftConstraint.or(rightConstraint);
        subFexpStack.push(constraint);
    }

    @Override
    public void exitAlternativeGroup(UVLJavaParser.AlternativeGroupContext ctx) {
        super.exitAlternativeGroup(ctx);
        FExpression rightConstraint = subFexpStack.pop();
        FExpression leftConstraint = subFexpStack.pop();
        FExpression c1 = leftConstraint.or(rightConstraint);
        FExpression c2 = (leftConstraint.not()).or(rightConstraint.not());
        FExpression constraint = c1.and(c2);
        subFexpStack.push(constraint);
    }

    @Override
    public void exitOptionalGroup(UVLJavaParser.OptionalGroupContext ctx) {
        super.exitOptionalGroup(ctx);
        FExpression c1 = subFexpStack.pop();
        FExpression constraint = c1.or(c1.not());
        subFexpStack.push(constraint);
    }

    @Override
    public void enterFeature(UVLJavaParser.FeatureContext ctx) {
        super.enterFeature(ctx);
        String featureReference = ctx.reference().getText().replace("\"", "");
        String[] featureReferenceParts = featureReference.split("\\.");
        String featureName;
        if (featureReferenceParts.length > 1) {
            featureName = featureReferenceParts[featureReferenceParts.length - 1];
        } else {
            featureName = featureReferenceParts[0];
        }

        FExpression fExpression = new FExpression(featureName);
        subFexpStack.push(fExpression);
    }

    @Override
    public void exitLiteralConstraint(UVLJavaParser.LiteralConstraintContext ctx) {
        super.exitLiteralConstraint(ctx);
        String featureReference = ctx.reference().getText().replace("\"", "");
        FExpression fExpression = new FExpression(featureReference);
        constraintStack.push(fExpression);
    }

    @Override
    public void exitNotConstraint(UVLJavaParser.NotConstraintContext ctx) {
        super.exitNotConstraint(ctx);
        FExpression constraint = constraintStack.pop().not();
        constraintStack.push(constraint);
    }

    @Override
    public void exitAndConstraint(UVLJavaParser.AndConstraintContext ctx) {
        super.exitAndConstraint(ctx);
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
}