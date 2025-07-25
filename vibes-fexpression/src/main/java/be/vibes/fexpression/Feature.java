package be.vibes.fexpression;

/*-
 * #%L
 * VIBeS: featured expressions
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

import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import java.util.Objects;
import de.vill.config.Configuration;
import de.vill.model.Attribute;
import de.vill.model.FeatureType;
import de.vill.model.Import;
import de.vill.util.Util;
import be.vibes.solver.Group;

import java.io.Serial;
import java.util.*;

import static de.vill.util.Util.addNecessaryQuotes;

/**
 * This class represents a feature of any kind (normal, numeric, abstract, ...).
 */
public class Feature<F extends Feature<F>>{

    private final String featureName;
    private String nameSpace = "";
    private Import relatedImport;
    private String lowerBound;
    private String upperBound;
    private final List<Group<F>> children;
    private final Map<String, Attribute> attributes;
    private FeatureType featureType;
    private boolean isSubmodelRoot = false;

    private Group<F> parent;
    private final List<FExpression> constraints = new LinkedList<>();
    //private final List<ExclusionConstraint> exclusions = new LinkedList<>();
    //private final List<RequirementConstraint> requirements = new LinkedList<>();


    /**
     * The constructor of the feature class. Needs a name that can not be changed.
     * The name is independent of the namespace. See
     * {@link Feature#getFeatureName()} for further explanation.
     *
     * @param name The name of the feature (without namespace information)
     */
    public Feature(String name) {
        if (name != null && name.length() > 1 && name.charAt(0) == '\'') {
            this.featureName = name.substring(1, name.length() - 1);
        } else {
            this.featureName = name;
        }

        children = new LinkedList<>() {

            /**
             * This custom class with the List-Interface is used to force the update of the
             * parent reference of children.
             **/
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            public boolean add(Group<F> e) {
                if (super.add(e)) {
                    e.setParentFeature((F) Feature.this);
                    return true;
                }
                return false;
            }

            @Override
            public void add(int index, Group<F> element) {
                super.set(index, element);
                element.setParentFeature((F) Feature.this);
            }

            @Override
            public Group<F> remove(int index) {
                Group<F> g = super.remove(index);
                g.setParentFeature(null);
                return g;
            }

            @Override
            public boolean remove(Object o) {
                if (super.remove(o)) {
                    ((Group<F>) o).setParentFeature(null);
                    return true;
                }
                return false;
            }

            @Override
            public boolean addAll(int index, Collection<? extends Group<F>> c) {
                if (super.addAll(index, c)) {
                    c.forEach(e -> e.setParentFeature((F) Feature.this));
                    return true;
                }
                return false;
            }

            @Override
            public void clear() {
                for (Group<F> featureGroup : this) {
                    featureGroup.setParentFeature(null);
                }
                super.clear();
            }

            @Override
            public Group<F> set(int index, Group<F> element) {
                Group<F> g;
                if ((g = super.set(index, element)) != null) {
                    g.setParentFeature((F) Feature.this);
                    return g;
                }
                return null;
            }

            class GroupIterator implements ListIterator<Group<F>> {
                private final ListIterator<Group<F>> itr;
                Group<F> lastReturned;

                public GroupIterator(ListIterator<Group<F>> itr) {
                    this.itr = itr;
                }

                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public Group<F> next() {
                    lastReturned = itr.next();
                    return lastReturned;
                }

                @Override
                public boolean hasPrevious() {
                    return itr.hasPrevious();
                }

                @Override
                public Group<F> previous() {
                    lastReturned = itr.previous();
                    return lastReturned;
                }

                @Override
                public int nextIndex() {
                    return itr.nextIndex();
                }

                @Override
                public int previousIndex() {
                    return itr.previousIndex();
                }

                @Override
                public void remove() {
                    itr.remove();
                    lastReturned.setParentFeature(null);
                }

                @Override
                public void set(Group<F> e) {
                    itr.set(e);
                    lastReturned.setParentFeature(null);
                    e.setParentFeature((F) Feature.this);
                }

                @Override
                public void add(Group<F> e) {
                    itr.add(e);
                    e.setParentFeature((F) Feature.this);
                }

            }

            @Override
            public ListIterator<Group<F>> listIterator(int index) {
                return new GroupIterator(super.listIterator(index));
            }

            ;

        };
        attributes = new HashMap<>();
    }

    public static Feature<?> feature(String name) {
        return new Feature<>(name);
    }

    /**
     * Returns just the name of the feature as string without any namespace
     * information. This means there are no dots in the string (because uvl does not
     * allow such) and two different features may return the same name.
     *
     * @return The name of the feature.
     */
    public String getFeatureName() {
        return featureName;
    }

    /**
     * This method returns a list with all children of the feature. Children can
     * only be groups and not other features. Only groups can have features as
     * children. If there are no children an empty list is returned (not null). This
     * list is no clone meaning that adding / removing items will actually change
     * the children of the feature!
     *
     * @return A list of all children of the feature.
     */
    public List<Group<F>> getChildren() {
        return children;
    }

    /**
     * Adds a group to the children of the feature. This could also be done by
     * getting the child list and adding it there.
     *
     * @param group The group that should be added as child. The group and all its
     *              children recursively must not contain this feature, since this
     *              would break the tree structure which is necessary. This is not
     *              checked by this method.
     */
    public void addChildren(Group<F> group) {
        children.add(group);
    }

    /**
     * This method only returns a value if the feature is a numeric feature / has a
     * feature cardinality. If not, this method returns null.
     *
     * @return null or the lower bound of a feature cardinality as string
     */
    public String getLowerBound() {
        return lowerBound;
    }

    /**
     * This method only returns a value if the feature is a numeric feature / has a
     * feature cardinality. If not, this method returns null. If there is no upper
     * bound in the cardinality, this method returns the lower bound. The returned
     * value might also be the * symbol, if the upper bound is unlimited.
     *
     * @return the upper bound as string
     */
    public String getUpperBound() {
        return upperBound;
    }

    /**
     * Sets the lower bound of a feature cardinality. Setting this value results in
     * this feature becoming a numeric feature.
     *
     * @param lowerBound lower bound as string (must be a positive integer or zero)
     */
    public void setLowerBound(String lowerBound) {
        this.lowerBound = lowerBound;
    }

    /**
     * Sets the upper bound of a feature cardinality. Setting this value results in
     * this feature becoming a numeric feature.
     *
     * @param upperBound upper bound as string (must be a positive integer or zero
     *                   or the * symbol)
     */
    public void setUpperBound(String upperBound) {
        this.upperBound = upperBound;
    }

    /**
     * This method returns the namespace of a feature. The namespace represents the
     * path from the root feature model over all imports to the feature. Each import
     * alias is separated by dots. If an import does not use an alias the namespace
     * of the import is used in this namespace. If the feature is in the root
     * feature model the namespace is an empty string but not null.
     *
     * @return the namespace of the feature (from root model to feature). May be
     * empty but not null!
     */
    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * Set the namespace for the feature. When creating a decomposed featuremodel by
     * yourself and not with the {@link de.vill.main.UVLModelFactory} becarefull to
     * the the namespaces and set them correct. See {@link Feature#getNameSpace()}
     * for explanation what the namespace of a feature represents.
     *
     * @param nameSpace Namespace of the feature must not be null.
     */
    public void setNameSpace(String nameSpace) {
        if (nameSpace == null) {
            throw new IllegalArgumentException("Namespace must not be null!");
        }
        this.nameSpace = nameSpace;
    }

    /**
     * Adds the feature type of the feature (if supported by the language level)
     *
     */
    public void setFeatureType(final FeatureType featureType) {
        this.featureType = featureType;
    }

    /**
     * Returns the type of the feature if supported by the language level
     *
     * @return the type of the feature
     */
    public FeatureType getFeatureType() {
        return this.featureType;
    }

    /**
     * This method returns a uvl valid name for the feature. This name is unique
     * even if the feature model is composed with other submodels. It does that by
     * using the namespace (from root to feature over all imports) and a unique ID
     * of the same length to prevent naming collisions. This is necessary to allow
     * printing the composed model as one uvl model.
     *
     * @return A uvl valid unique name for the feature
     */
    public String getFullReference() {
        String fullReference;
        // .hashCode() does vary in length, this might lead to naming conflicts,
        // therefore make ids all of the same length by adding zeros
        int idLength = String.valueOf(Integer.MAX_VALUE).length();
        String id = String.format("%0" + idLength + "d", this.hashCode());
        if ("".equals(nameSpace)) {
            // this case means, the feature is in the root feature model (meaning it is no
            // imported submodel) and therefore needs no namespace
            fullReference = getFeatureName() + "." + id;
        } else {
            // this case means, the feature is in a imported sub feature model and therefore
            // needs a namespace to be unique
            fullReference = nameSpace + "." + getFeatureName() + "." + id;
        }
        return fullReference.replace('.', '_');
    }

    /**
     * If the feature is the root feature of an imported submodel this method
     * returns the {@link Import} that imports the corresponding feature model, if
     * not this method returns null.
     *
     * @return The realted import if the feature is the root feature of an imported
     * feature model or null otherwise
     */
    public Import getRelatedImport() {
        return relatedImport;
    }

    /**
     * Set the {@link Import} that imports the feature model this feature is the
     * root of.
     *
     * @param relatedImport The related {@link Import}
     */
    public void setRelatedImport(Import relatedImport) {
        this.relatedImport = relatedImport;
    }

    /**
     * Returns a map with the attribute name (string) as key and the
     * {@link Attribute} as value. Returns an empty map the feature has no
     * attributes. This is not a clone, which means editing the map will actually
     * change the feature attributes.
     *
     * @return The attribute map.
     */
    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    /**
     * True if this feature is the root feature of an imported sub feature model,
     * false if not.
     *
     * @return isSubmodelRoot
     */
    public boolean isSubmodelRoot() {
        return isSubmodelRoot;
    }

    /**
     * Sets submodelRoot variable.
     *
     * @param submodelRoot true if this feature is the root feature of an imported
     *                     sub feature model, false if not.
     */
    public void setSubmodelRoot(boolean submodelRoot) {
        isSubmodelRoot = submodelRoot;
    }

    /**
     * This method is just for simplicity. It is used when printing the first
     * feature of a composed feauturemodel.
     *
     * @return feature in uvl representation with its subtree
     */
    @Override
    public String toString() {
        return toString(true, "");
    }

    /**
     * This method is necessary because the uvl string representation differs
     * between the feature as imported feature another feature model or as the root
     * feature as the submodel. This method is only relevant when the decomposed
     * model is printed as String, not for the composed model. Concret this method
     * always prints its children and attributes (if there are any) even if it is an
     * imported feature.
     *
     * @param currentAlias The namespace under this feature should be referenced.
     *                     This is necessary, because features can be reference from
     *                     e.g. constraints in other sub models, therefore the
     *                     namespace can vary.
     * @return The feature and ist subtree as String
     */
    public String toStringAsRoot(String currentAlias) {
        StringBuilder result = new StringBuilder();

        if (featureType != null) {
            result.append(featureType.getName()).append(" ");
        }
        result.append(addNecessaryQuotes(getFeatureName()));
        result.append(cardinalityToString());
        result.append(attributesToString(false, currentAlias));
        result.append(Configuration.getNewlineSymbol());

        for (Group<F> group : children) {
            result.append(Util.indentEachLine(group.toString(false, currentAlias)));
        }

        return result.toString();
    }

    /**
     * Returns the feature and all its children as uvl valid string.
     *
     * @param withSubmodels true if the featuremodel is printed as composed
     *                      featuremodel with all its submodels as one model, false
     *                      if the model is printed with separated sub models
     * @param currentAlias  the namspace from the one referencing the feature to the
     *                      feature
     * @return uvl representaiton of the feature
     */
    public String toString(boolean withSubmodels, String currentAlias) {
        StringBuilder result = new StringBuilder();
        if (featureType != null) {
            result.append(featureType.getName()).append(" ");
        }
        if (withSubmodels) {
            result.append(addNecessaryQuotes(getFullReference()));
        } else {
            result.append(addNecessaryQuotes(getReferenceFromSpecificSubmodel(currentAlias)));
        }
        result.append(cardinalityToString());

        /*
         * attributes and groups should not be printed if this is the root feature of
         * another submodel and we want to print each submodel separately if the feature
         * should be printed with is children eather call this method with withSubmodels
         * parameter true for a composed model or the toStringAsRoot method if the
         * featuremodels should be printed separately
         */
        if (!isSubmodelRoot() || withSubmodels) {
            result.append(attributesToString(withSubmodels, currentAlias));
            result.append(Configuration.getNewlineSymbol());

            for (Group<F> group : children) {
                result.append(Util.indentEachLine(group.toString(withSubmodels, currentAlias)));
            }
        }

        return result.toString();
    }

    /**
     * This method generates the namespace from which the feature is reference under
     * the assumption that the referencing is done from a feature model with the
     * namespace currentAlias.
     *
     * @param currentAlias The complete namespace from root model to the submodel
     *                     that wants to reference this feature
     * @return the reference with name
     */
    public String getReferenceFromSpecificSubmodel(String currentAlias) {
        // get the complete namespace (from root model to feature) and remove the
        // currentAlias from its beginning -> this must not be referenced because the
        // one referencing is already in this scope
        String currentNamespace = getNameSpace().substring(currentAlias.length());
        if (currentNamespace.isEmpty()) {
            return getFeatureName();
        }
        if (currentNamespace.charAt(0) == '.') {
            currentNamespace = currentNamespace.substring(1);
        }

        return currentNamespace + "." + getFeatureName();
    }

    /**
     * Returns the parent group of the feature.
     *
     * @return Parent group of the feature. Null if it is the root feature.
     */
    public Group<F> getParentGroup() {
        return parent;
    }

    /**
     * Sets the parent of the group.
     *
     * @param parent the parent of the group
     */
    public void setParentGroup(Group<F> parent) {
        this.parent = parent;
    }

    /**
     * Returns the parent feature of the feature.
     *
     * @return parent feature of the feature. Null if it is the root feature.
     */
    public Feature<?> getParentFeature() {
        if (parent != null) {
            return parent.getParentFeature();
        }
        return null;
    }

    public List<FExpression> getConstraints() {
        return constraints;
    }

    public int getNumberOfConstraints(){
        return constraints.size();
    }

    /*
    public List<ExclusionConstraint> getExclusions() {
        return exclusions;
    }

    public List<RequirementConstraint> getRequirements() {
        return requirements;
    }

    public int getTotalNumberOfConstraints(){
        return exclusions.size() + requirements.size();
    }

    public int getNumberOfExclusionConstraints(){
        return exclusions.size();
    }

    public int getNumberOfRequirementConstraints(){
        return requirements.size();
    }*/

    private String cardinalityToString() {
        StringBuilder result = new StringBuilder();
        if (!(upperBound == null & lowerBound == null)) {
            result.append(" cardinality [");
            if (getLowerBound().equals(getUpperBound())) {
                result.append(getLowerBound());
            } else {
                result.append(getLowerBound());
                result.append("..");
                result.append(getUpperBound());
            }
            result.append("] ");
        }
        return result.toString();
    }

    private String attributesToString(boolean withSubmodels, String currentAlias) {
        StringBuilder result = new StringBuilder();
        if (!attributes.isEmpty()) {
            result.append(" {");
            attributes.forEach((k, v) -> {
                result.append(addNecessaryQuotes(k));

                result.append(' ');
                result.append(v.toString(withSubmodels, currentAlias));
                result.append(", ");
            });
            result.deleteCharAt(result.length() - 1);
            result.deleteCharAt(result.length() - 1);
            result.append("}");
        }
        return result.toString();
    }

    @Override
    public Feature<F> clone() {
        Feature<F> feature = new Feature<>(getFeatureName());
        feature.setNameSpace(getNameSpace());
        feature.setLowerBound(getLowerBound());
        feature.setUpperBound(getUpperBound());
        feature.setSubmodelRoot(isSubmodelRoot);
        feature.setRelatedImport(getRelatedImport());
        feature.setFeatureType(this.getFeatureType());
        feature.getAttributes().putAll(getAttributes());
        for (Group<F> group : getChildren()) {
            feature.getChildren().add(group.clone());
        }
        for (Group<F> group : feature.getChildren()) {
            group.setParentFeature((F) feature);
        }
        return feature;
    }

    /*
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Feature)) {
            return false;
        }

        if (!(this.getFeatureName().equals(((Feature<F>) obj).getFeatureName()))) {
            return false;
        }

        if (this.getFeatureType() != null && !(this.getFeatureType().equals(((Feature<?>) obj).getFeatureType()))) {
            return false;
        }

        if (this.getUpperBound() != null && !(this.getUpperBound().equals(((Feature<F>) obj).getUpperBound()))) {
            return false;
        }

        if (this.getLowerBound() != null && !(this.getLowerBound().equals(((Feature<F>) obj).getLowerBound()))) {
            return false;
        }

        // check attributes
        if (this.getAttributes().size() != ((Feature<F>) obj).getAttributes().size()) {
            return false;
        }
        Map<String, Attribute> objAttributes = ((Feature<F>) obj).getAttributes();
        for (String key: this.getAttributes().keySet()) {
            if (!objAttributes.containsKey(key)) {
                return false;
            }

            if (!this.getAttributes().get(key).equals(((Feature<F>) obj).getAttributes().get(key))) {
                return false;
            }
        }

        if (this.getChildren().size() != ((Feature<F>) obj).getChildren().size()) {
            return false;
        }

        final List<Group<F>> objGroups = ((Feature<F>) obj).getChildren();
        for (final Group<F> currentGroup : this.getChildren()) {
            if (!objGroups.contains(currentGroup)) {
                return false;
            }
        }

        return true;
    }*/

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Feature<?> feature = (Feature<?>) o;
        return Objects.equals(getFeatureName(), feature.getFeatureName()) && Objects.equals(getChildren(), feature.getChildren()) && getFeatureType() == feature.getFeatureType() && Objects.equals(getConstraints(), feature.getConstraints()); //Objects.equal(getExclusions(), feature.getExclusions()) && Objects.equal(getRequirements(), feature.getRequirements());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFeatureName(), getChildren(), getFeatureType(), getConstraints()); //getExclusions(), getRequirements());
    }

/*
    @Override
    public int hashCode() {
        return Objects.hashCode(getFeatureName(), getChildren(), parent, getExclusions(), getRequirements());
        //return Objects.hashCode(getFeatureName(), getNameSpace(), getRelatedImport(), getLowerBound(), getUpperBound(), getChildren(), getAttributes(), getFeatureType(), isSubmodelRoot(), getExclusions(), getRequirements());

        //return Objects.hashCode(getFeatureName(), getNameSpace(), getRelatedImport(), getLowerBound(), getUpperBound(), getChildren(), getAttributes(), getFeatureType(), isSubmodelRoot(), parent, getExclusions(), getRequirements());
    }*/
}
