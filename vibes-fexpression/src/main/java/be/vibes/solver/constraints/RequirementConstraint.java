package be.vibes.solver.constraints;

/*
 * Copyright 2025 Sophie Fortz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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