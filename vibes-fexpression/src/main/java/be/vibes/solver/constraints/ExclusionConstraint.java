package be.vibes.solver.constraints;

import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ExclusionConstraint extends Constraint {
    private LiteralConstraint left;
    private LiteralConstraint right;

    public ExclusionConstraint(LiteralConstraint left, LiteralConstraint right) {
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
        return "exc(" + left.toString(withSubmodels, currentAlias) + ", "
                + right.toString(withSubmodels, currentAlias) + ")";
    }

    @Override
    public List<Constraint> getConstraintSubParts() {
        return Arrays.asList(left, right);
    }

    @Override
    public void replaceConstraintSubPart(Constraint oldSubConstraint, Constraint newSubConstraint) {
        if (left == oldSubConstraint) {
            left = (LiteralConstraint) newSubConstraint;
        } else if (right == oldSubConstraint) {
            right = (LiteralConstraint) newSubConstraint;
        }
    }

    @Override
    public Constraint clone() {
        return new ExclusionConstraint((LiteralConstraint) left.clone(), (LiteralConstraint) right.clone());
    }

    @Override
    public int hashCode(int level) {
        final int prime = 31;
        int result = prime * level + (left == null ? 0 : left.hashCode(1 + level));
        result = prime * result + (right == null ? 0 : right.hashCode(1 + level));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ExclusionConstraint other = (ExclusionConstraint) obj;
        return Objects.equals(left, other.left) && Objects.equals(right, other.right);
    }

}
