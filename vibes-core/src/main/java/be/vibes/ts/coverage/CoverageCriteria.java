package be.vibes.ts.coverage;

/*-
 * #%L
 * VIBeS: core
 * %%
 * Copyright (C) 2014 - 2018 University of Namur
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

import be.vibes.ts.TestCase;
import be.vibes.ts.TestSet;
import be.vibes.ts.exception.CoverageComputationException;
import be.vibes.ts.execution.Execution;

import java.util.Iterator;

/**
 * A coverage criteria gives, for a test or a set of tests, a value between 0 (denoting no coverage)
 * and 1 (denoting full coverage) denoting the coverage percentage.
 *
 * @author Xavier Devroey - xavier.devroey@gmail.com
 */
public interface CoverageCriteria {

    /**
     * Returns the coverage for the given test case as a value between 0 (denoting no coverage)
     * and 1 (denoting full coverage).
     *
     * @param test The test to evaluate.
     * @return A value between 0 and 1.
     * @throws CoverageComputationException If an error occurs during the computation.
     */
    public double coverage(TestCase test) throws CoverageComputationException;

    /**
     * Returns the coverage for the given set of test cases as a value between 0 (denoting no coverage)
     * and 1 (denoting full coverage).
     *
     * @param suite The set of tests to evaluate.
     * @return A value between 0 and 1.
     * @throws CoverageComputationException If an error occurs during the computation.
     */
    public double coverage(TestSet suite) throws CoverageComputationException;

    /**
     * Returns the coverage for the given execution as a value between 0 (denoting no coverage)
     * and 1 (denoting full coverage). The execution must have been performed on the transition system for which
     * the coverage criteria is defined.
     *
     * @param execution The execution to evaluate.
     * @return A value between 0 and 1.
     * @throws CoverageComputationException If an error occurs during the computation.
     */
    public double coverage(Execution execution) throws CoverageComputationException;

    /**
     * Returns the coverage for the given executions as a value between 0 (denoting no coverage)
     * and 1 (denoting full coverage). The executions must have been performed on the transition system for which
     * the coverage criteria is defined.
     *
     * @param execution The execution to evaluate.
     * @return A value between 0 and 1.
     * @throws CoverageComputationException If an error occurs during the computation.
     */
    public double coverage(Iterator<Execution> execution) throws CoverageComputationException;

}
