package be.vibes.selection.dissimilar;

/*-
 * #%L
 * VIBeS: test case selection
 * %%
 * Copyright (C) 2014 - 2018 University of Namur
 * Copyright 2025 Sophie Fortz
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

import be.vibes.fexpression.FExpression;
import be.vibes.solver.SolverFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.*;
import be.vibes.selection.exception.DissimilarityComputationException;
import be.vibes.solver.ConstraintIdentifier;
import be.vibes.solver.exception.SolverFatalErrorException;
import be.vibes.solver.exception.ConstraintNotFoundException;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.exception.SolverInitializationException;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.execution.FeaturedTransitionSystemExecutor;
import be.vibes.ts.TestCase;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;

public class FtsTestCaseDissimilarityComputor extends TestCaseDissimilarityComputor {

    private static final Logger LOG = LoggerFactory
            .getLogger(FtsTestCaseDissimilarityComputor.class);

    private final TestCaseDissimilarityComputor testCaseDissimilarity;
    private SolverFacade fm;
    private final BinaryOperator<Double> combineOperator;
    private FeaturedTransitionSystem fts;

    public FtsTestCaseDissimilarityComputor(SolverFacade solver, FeaturedTransitionSystem fts) {
        this(solver, new JaccardDissimilarityComputor(), fts);
    }

    public FtsTestCaseDissimilarityComputor(SolverFacade solver, TestCaseDissimilarityComputor computor, FeaturedTransitionSystem fts) {
        this(solver, computor, (Double x, Double y) -> x * y, fts);
    }

    public FtsTestCaseDissimilarityComputor(SolverFacade solver, TestCaseDissimilarityComputor computor, BinaryOperator<Double> combineOperator, FeaturedTransitionSystem fts) {
        this.fm = solver;
        this.testCaseDissimilarity = computor;
        this.combineOperator = combineOperator;
        this.fts = fts;
    }

    public FtsTestCaseDissimilarityComputor(SolverFacade solver, SetBasedDissimilarityComputor<? extends Set> computor, FeaturedTransitionSystem fts) {
        this(solver, TestCaseDissimilarityComputor.toTestCaseDissimilarityComputor(computor), fts);
    }

    public FtsTestCaseDissimilarityComputor(SolverFacade solver, SequenceBasedDissimilarityComputor<? extends List> computor, FeaturedTransitionSystem fts) {
        this(solver, TestCaseDissimilarityComputor.toTestCaseDissimilarityComputor(computor), fts);
    }

    public void setFm(SolverFacade fm) {
        this.fm = fm;
    }

    public void setFts(FeaturedTransitionSystem fts) {
        this.fts = fts;
    }
    
    @Override
    public double dissimilarity(TestCase o1, TestCase o2) throws DissimilarityComputationException {
        double dissimilarity = testCaseDissimilarity.dissimilarity(o1, o2);
        checkState(dissimilarity >= 0, "Dissimilarity computed using %s has to be >=0 but was %s!", testCaseDissimilarity.getClass().getName(), dissimilarity);
        LOG.trace("Test case dissimilarity is {}", dissimilarity);
        // Compute similarity
        double dissimilarity2 = productsDissimilarity(o1, o2);
        checkState(dissimilarity2 >= 0, "Products dissimilarity has to be >=0 but was %s!", dissimilarity2);
        LOG.trace("Products dissimilarity is {}", dissimilarity2);
        dissimilarity = combineOperator.apply(dissimilarity, dissimilarity2);
        LOG.trace("Combined dissimilarity is {}", dissimilarity);
        return dissimilarity;
    }

    private double productsDissimilarity(TestCase o1, TestCase o2) throws DissimilarityComputationException {
        try {
            FExpression intersect;
            FExpression o1Expr = getProductConstraint(o1);
            FExpression o2Expr = getProductConstraint(o2);
            if (o1Expr == null || o1Expr.isTrue()) {
                intersect = o2Expr == null ? FExpression.trueValue() : o2Expr;
            } else if (o2Expr == null) {
                intersect = o1Expr;
            } else {
                intersect = o1Expr.and(o2Expr).applySimplification();
            }

            FExpression union;
            if (o1Expr == null || o1Expr.isTrue()) {
                union = o2Expr == null ? FExpression.trueValue() : o2Expr;
            } else if (o2Expr == null) {
                union = o1Expr;
            } else {
                union = o1Expr.or(o2Expr).applySimplification();
            }

            ConstraintIdentifier id = fm.addConstraint(intersect);
            double intersectionCount = fm.getNumberOfSolutions();
            fm.removeConstraint(id);

            id = fm.addConstraint(union);
            double unionCount = fm.getNumberOfSolutions();
            fm.removeConstraint(id);

            checkState(unionCount >= intersectionCount, "Union (%s) has to be higher or equal to intersection (%s) !", unionCount, intersectionCount);

            return 1 - (intersectionCount / unionCount);
        } catch (SolverInitializationException e) {
            LOG.error("Error while coputing product sets for dissimilarity!", e);
            throw new DissimilarityComputationException("Solver error to compute products", e);
        } catch (SolverFatalErrorException | ConstraintNotFoundException e) {
            LOG.error("Error while coputing product sets for dissimilarity!", e);
            throw new DissimilarityComputationException("Solver error to compute products", e);
        } catch (ConstraintSolvingException e) {
            LOG.error("Error while coputing product sets for dissimilarity!", e);
            throw new DissimilarityComputationException("Solver error to compute products", e);
        }
    }

    private FExpression getProductConstraint(TestCase tc) {
        FeaturedTransitionSystemExecutor exec = new FeaturedTransitionSystemExecutor(fts, fm);
        return exec.getFexpression(tc);
    }
}
