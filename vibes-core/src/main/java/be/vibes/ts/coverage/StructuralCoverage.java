package be.vibes.ts.coverage;

/*-
 * #%L
 * VIBeS: core
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

import be.vibes.solver.FeatureModel;
import be.vibes.ts.*;
import be.vibes.ts.exception.CoverageComputationException;
import be.vibes.ts.exception.TransitionSystenExecutionException;
import be.vibes.ts.execution.Execution;
import be.vibes.ts.execution.FeaturedTransitionSystemExecutor;
import be.vibes.ts.execution.TransitionSystemExecutor;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Structural coverage criteria for transition and featured transition systems. The criteria executes the tests on
 * the transition or featured transition system to collected structural coverage information and return the
 * corresponding coverage. If more than one execution are possible for the given test case, the coverage criteria
 * will consider all the elements covered by the different executions.
 *
 * @param <T> The type of elements to cover.
 * @author Xavier Devroey - xavier.devroey@gmail.com
 */
public abstract class StructuralCoverage<T> implements CoverageCriteria {

    private static final Logger LOG = LoggerFactory.getLogger(StructuralCoverage.class);

    private final TransitionSystem ts;
    private final TransitionSystemExecutor executor;
    private final Set<T> elementsToCover;

    /**
     * Create a new structural coverage criteria for the given transition system.
     *
     * @param ts The transition system for which the coverage criteria will be reported.
     */
    public StructuralCoverage(TransitionSystem ts) {
        this.ts = ts;
        this.executor = new TransitionSystemExecutor(ts);
        this.elementsToCover = Sets.newHashSet(getElementsToBeCovered());
    }

    /**
     * Creates a new structural coverage criteria for the given featured transition system and feature model.
     *
     * @param fts The featured transition system for which the coverage criteria will be reported.
     * @param fm  The feature model used during the execution of the tests to compute the coverage criteria.
     */
    public StructuralCoverage(FeaturedTransitionSystem fts, FeatureModel<?> fm) {
        this.ts = fts;
        this.executor = new FeaturedTransitionSystemExecutor(fts, fm);
        this.elementsToCover = Sets.newHashSet(getElementsToBeCovered());
    }

    @Override
    public double coverage(TestCase test) throws CoverageComputationException {
        Set<T> covered = new HashSet<>();
        double total = this.elementsToCover.size();
        addCoveredElements(test, covered);
        return total == 0.0 ? 0.0 : (covered.size() / total);
    }

    @Override
    public double coverage(TestSet suite) throws CoverageComputationException {
        Set<T> covered = new HashSet<>();
        double total = this.elementsToCover.size();
        for (TestCase test : suite) {
            addCoveredElements(test, covered);
        }
        return total == 0.0 ? 0.0 : (covered.size() / total);
    }

    @Override
    public double coverage(Execution execution){
        double total = this.elementsToCover.size();
        Set<T> covered = new HashSet<>(getCoveredElements(execution));
        return total == 0.0 ? 0.0 : (covered.size() / total);
    }

    @Override
    public double coverage(Iterator<Execution> executions){
        Set<T> covered = new HashSet<>();
        double total = this.elementsToCover.size();
        executions.forEachRemaining((execution) -> {
            covered.addAll(getCoveredElements(execution));
        });
        return total == 0.0 ? 0.0 : (covered.size() / total);
    }

    private void addCoveredElements(TestCase test, Set<T> covered) throws CoverageComputationException {
        try {
            executor.reset();
            executor.execute(test);
            Iterator<Execution> executions = executor.getCurrentExecutions();
            while (executions.hasNext()) {
                Execution execution = executions.next();
                covered.addAll(getCoveredElements(execution));
            }
        } catch (TransitionSystenExecutionException e) {
            LOG.error("Exception happening during the execution of the test case for structural coverage computation", e);
            throw new CoverageComputationException("Exception happening during the execution of the test case for structural coverage computation", e);
        }
    }

    protected abstract Set<T> getCoveredElements(Execution execution);

    public abstract Iterator<T> getElementsToBeCovered();

    protected TransitionSystem getTs() {
        return ts;
    }
}
