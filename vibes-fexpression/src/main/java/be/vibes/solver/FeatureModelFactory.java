package be.vibes.solver;

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

import be.vibes.fexpression.Feature;

public class FeatureModelFactory<F extends Feature<F>> extends XMLModelFactory<F, FeatureModel<F>> {
//public class FeatureModelFactory extends XMLModelFactory<Feature<?>, FeatureModel<Feature<?>>>{

    public FeatureModelFactory() {
        super(FeatureModel::new);
    }

    public FeatureModelFactory(FeatureModel<F> otherFM) {
        super(() -> new FeatureModel<>(otherFM), otherFM.getSolver().getType());
    }

    public FeatureModelFactory(SolverType type) {
        super(FeatureModel::new, type);
    }

    public F setRootFeature(String name){
        Feature<F> feature = new Feature<>(name);
        return setRootFeature((F) feature, name);
    }

    public F addFeature(Group<F> group, String name){
        F feature = (F) new Feature<>(name);
        return addFeature(feature, group, name);
    }
}
