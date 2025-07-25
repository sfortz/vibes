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
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.State;
import be.vibes.ts.Transition;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.execution.Execution;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * State coverage criteria for transition systems. When a transition is executed, both source and destination states
 * are covered.
 *
 * @author Xavier Devroey - xavier.devroey@gmail.com
 */
public class StateCoverage extends StructuralCoverage<State> {

    /**
     * Create a new state coverage criteria for the given transition system.
     *
     * @param ts The transition system for which the coverage criteria will be reported.
     */
    public StateCoverage(TransitionSystem ts) {
        super(ts);
    }

    /**
     * Creates a new state coverage criteria for the given featured transition system and feature model.
     *
     * @param fts The featured transition system for which the coverage criteria will be reported.
     * @param fm  The feature model used during the execution of the tests to compute the coverage criteria.
     */
    public StateCoverage(FeaturedTransitionSystem fts, FeatureModel<?> fm) {
        super(fts, fm);
    }

    @Override
    protected Set<State> getCoveredElements(Execution execution) {
        Set<State> covered = new HashSet<>();
        for (Transition transition : execution) {
            covered.add(transition.getSource());
            covered.add(transition.getTarget());
        }
        return covered;
    }

    @Override
    public Iterator<State> getElementsToBeCovered() {
        return getTs().states();
    }

}
