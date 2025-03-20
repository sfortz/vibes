package be.vibes.solver.constraints;

import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.ImplicationConstraint;
import de.vill.model.constraint.LiteralConstraint;

public class RequirementConstraint extends ImplicationConstraint {
    private LiteralConstraint left;
    private LiteralConstraint right;

    public RequirementConstraint(LiteralConstraint left, LiteralConstraint right) {
        super(left, right);
        this.left = left;
        this.right = right;
    }

    public LiteralConstraint getLeft() {
        return left;
    }

    public LiteralConstraint getRight() {
        return right;
    }

    @Override
    public String toString(boolean withSubmodels, String currentAlias) {
        return "req(" + left.toString(withSubmodels, currentAlias) + ", "
                + right.toString(withSubmodels, currentAlias) + ")";
    }

    @Override
    public void replaceConstraintSubPart(Constraint oldSubConstraint, Constraint newSubConstraint) {
        super.replaceConstraintSubPart(oldSubConstraint,newSubConstraint);
        if (left == oldSubConstraint) {
            left = (LiteralConstraint) newSubConstraint;
        } else if (right == oldSubConstraint) {
            right = (LiteralConstraint) newSubConstraint;
        }
        super.replaceConstraintSubPart(oldSubConstraint,newSubConstraint);
    }

    @Override
    public Constraint clone() {
        super.clone();
        return new RequirementConstraint((LiteralConstraint) left.clone(), (LiteralConstraint) right.clone());
    }

}