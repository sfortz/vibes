package be.vibes.fexpression;

/*
 * #%L
 * VIBeS: featured expressions
 * %%
 * Copyright (C) 2014 PReCISE, University of Namur
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
public class Feature extends de.vill.model.Feature {

    /**
     * The constructor of the feature class. Needs a name that can not be changed.
     * The name is independent of the namespace. See
     * {@link Feature#getFeatureName()} for further explanation.
     *
     * @param featureName The name of the feature (without namespace information)
     */
    public Feature(String featureName) {
        super(featureName);
    }

    public static Feature feature(String name) {
        return new Feature(name);
    }

    @Override
    public String toString() {
        return super.getFeatureName();
    }

}
