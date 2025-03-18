package be.vibes.solver;

import be.vibes.fexpression.Feature;

public class FeatureModelFactory extends XMLModelFactory<Feature, FeatureModel<Feature>>{

    public FeatureModelFactory() {
        super(new FeatureModel<>());
    }

    public FeatureModelFactory(SolverType type) {
        super(new FeatureModel<>(), type);
    }

    public Feature setRootFeature(String name){
        Feature feature = new Feature(name);
        return setRootFeature(feature, name);
    }

    public Feature addFeature(Group<Feature> group, String name){
        Feature feature = new Feature(name);
        return addFeature(feature, group, name);
    }
}
