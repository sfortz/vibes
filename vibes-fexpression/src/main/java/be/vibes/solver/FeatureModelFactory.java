package be.vibes.solver;

import be.vibes.fexpression.Feature;

public class FeatureModelFactory extends XMLModelFactory<Feature, FeatureModel<Feature>>{

    public FeatureModelFactory() {
        super(new FeatureModel<>());
    }

    public FeatureModelFactory(SolverType type) {
        super(new FeatureModel<>(), type);
    }

    @Override
    public Feature setRootFeature(String name){
        Feature feature = new Feature(name);
        feature.setParentGroup(null);
        super.getFeatureModel().getFeatureMap().put(name, feature);
        super.getFeatureModel().setRootFeature(feature);
        return feature;
        /*
        Feature feature = new Feature(name);
        super.addFeature(feature, null, name);
        return feature;*/
    }

    @Override
    public Feature addFeature(Group group, String name){
        Feature feature = new Feature(name);
        group.getFeatures().add(feature);
        feature.setParentGroup(group);
        super.getFeatureModel().getFeatureMap().put(name, feature);
        //super.addFeature(feature, group, name);
        return feature;
    }
}
