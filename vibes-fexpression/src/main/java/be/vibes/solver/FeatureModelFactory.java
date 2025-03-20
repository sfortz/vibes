package be.vibes.solver;

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
