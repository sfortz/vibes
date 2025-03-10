package be.vibes.fexpression;

import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import de.vill.model.Group;

import java.util.*;

public class Feature extends de.vill.model.Feature {

    private final List<ExclusionConstraint> exclusions = new LinkedList<>();
    private final List<RequirementConstraint> requirements = new LinkedList<>();

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

    @Override
    public Feature getParentFeature() {
        de.vill.model.Feature parent = super.getParentFeature();
        if (parent == null){
            return null;
        } else {
            return clone(parent);
        }
    }

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
    }

    public static Feature clone(de.vill.model.Feature old) {
        Feature feature = new Feature(old.getFeatureName());
        feature.setNameSpace(old.getNameSpace());
        feature.setLowerBound(old.getLowerBound());
        feature.setUpperBound(old.getUpperBound());
        feature.setSubmodelRoot(old.isSubmodelRoot());
        feature.setRelatedImport(old.getRelatedImport());
        feature.setFeatureType(old.getFeatureType());
        feature.getAttributes().putAll(old.getAttributes());
        for (Group group : old.getChildren()) {
            feature.getChildren().add(group.clone());
        }
        for (Group group : feature.getChildren()) {
            group.setParentFeature(feature);
        }
        feature.setParentGroup(old.getParentGroup());
        return feature;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getFeatureName(), this.getFeatureType(), this.getUpperBound(), this.getLowerBound(), this.getAttributes());
        //return Objects.hash(this.getFeatureName(), this.getFeatureType(), this.getUpperBound(), this.getLowerBound(), this.getAttributes(), this.getChildren());
    }

}
