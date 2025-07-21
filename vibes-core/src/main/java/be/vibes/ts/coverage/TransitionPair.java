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

import be.vibes.ts.Transition;

import java.util.Objects;

/**
 * Utility class representing a pair of transitions. Used to compute transition pair coverage.
 *
 * @author Xavier Devroey - xavier.devroey@gmail.com
 * @see TransitionPairCoverage
 */
public class TransitionPair {

    private final Transition first;
    private final Transition second;

    public TransitionPair(Transition first, Transition second) {
        this.first = first;
        this.second = second;
    }

    public Transition getFirst() {
        return first;
    }

    public Transition getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransitionPair that = (TransitionPair) o;
        return Objects.equals(first, that.first) &&
                Objects.equals(second, that.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
