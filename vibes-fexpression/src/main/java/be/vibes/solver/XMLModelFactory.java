package be.vibes.solver;

import be.vibes.fexpression.DimacsModel;
import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import be.vibes.solver.exception.FeatureModelDefinitionException;
import be.vibes.solver.exception.SolverInitializationException;
import de.vill.exception.ParseError;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;

import java.util.*;

public class XMLModelFactory {

    private final SolverType solverType;
    private final FeatureModel<Feature> featureModel;

    public XMLModelFactory() {
        this(SolverType.SAT4J);
    }

    public XMLModelFactory(SolverType type) {
        switch (type) {
            case SAT4J -> this.solverType = SolverType.SAT4J;
            case BDD -> this.solverType = SolverType.BDD;
            default -> throw new UnsupportedOperationException("Only SAT4J and BDD solvers are currently supported. Default is SAT4J.");
        }
        this.featureModel = new FeatureModel();
    }

    public void setNamespace(String namespace) {
        featureModel.setNamespace(namespace);
    }

    public Feature setRootFeature(String name) {
        Feature feature = new Feature(name);
        feature.setParentGroup(null);
        featureModel.getFeatureMap().put(name, feature);
        featureModel.setRootFeature(feature);
        return feature;
    }

    public Group addChild(Feature parent, Group.GroupType type) {
        Group group = new Group(type);
        parent.addChildren(group);
        group.setParentFeature(parent);
        return group;
    }

    public Feature addFeature(Group group, String name) {
        Feature feature = new Feature(name);
        feature.setParentGroup(group);
        group.getFeatures().add(feature);
        featureModel.getFeatureMap().put(name, feature);


        return feature;
    }

    public List<Feature> addAllFeatures(Group group, Collection<String> names) {
        List<Feature> features = new LinkedList<>();
        for (String name: names){
            Feature feature = addFeature(group, name);
            features.add(feature);
        }
        return features;
    }

    private Set<String> getRecursiveChildren(Feature f) {
        Set<String> children = new HashSet<>();
        children.add(f.getFeatureName());

        for(Group g: f.getChildren()){
            for(Feature child: g.getFeatures()){
                children.addAll(getRecursiveChildren(child));
            }
        }
        return children;
    }

    public ExclusionConstraint addExclusionConstraint(Feature lca, String f1, String f2) {
        return addConstraint(lca, f1, f2, "Exclusion");
    }

    public RequirementConstraint addRequirementConstraint(Feature lca, String feature, String dependency) {
        return addConstraint(lca, dependency, feature, "Requirement");
    }

    private <T extends Constraint> T addConstraint(Feature lca, String f1, String f2, String type) {

        if(featureModel.getFeature(lca.getFeatureName()) == null){
            throw new FeatureModelDefinitionException( "Impossible to add the new " + type
                    + " constraint. Feature " + lca.getFeatureName() + " is not part of the FM."
            );
        }

        Set<String> children = getRecursiveChildren(lca);

        if(featureModel.getFeature(f1) == null || featureModel.getFeature(f2) == null){
            throw new FeatureModelDefinitionException("Constraints should only refers to features belonging to the FM.");
        }

        if (!children.contains(f1) && !children.contains(f2)) {
            throw new FeatureModelDefinitionException(
                    type + " constraint should only refer to sub-features of " + lca.getFeatureName() + "."
            );
        }

        LiteralConstraint c1 = new LiteralConstraint(f1);
        LiteralConstraint c2 = new LiteralConstraint(f2);

        switch (type) {
            case "Exclusion": {
                ExclusionConstraint constraint = new ExclusionConstraint(c1, c2);
                lca.getExclusions().add(constraint);
                featureModel.getOwnConstraints().add(constraint);
                return (T) constraint;
            }
            case "Requirement":
                RequirementConstraint constraint = new RequirementConstraint(c1, c2);
                lca.getRequirements().add(constraint);
                featureModel.getOwnConstraints().add(constraint);
                return (T) constraint;
            default: throw new FeatureModelDefinitionException("Unknown type of constraints!");
        }
    }

    /*
    public Constraint addExclusionConstraint(Feature f1, Feature f2) {
        return addExclusionConstraint(f1.getFeatureName(), f2.getFeatureName());
    }

    public Constraint addRequirementConstraint(Feature feature, Feature dependency){
        return addRequirementConstraint(feature.getFeatureName(), dependency.getFeatureName());
    }

    public Constraint addExclusionConstraint(String f1, String f2) {

        if(featureModel.getFeature(f1) == null || featureModel.getFeature(f2) == null){
            throw new FeatureModelDefinitionException("Constraints should only refers to features belonging to the FM.");
        }

        LiteralConstraint c1 = new LiteralConstraint(f1);
        LiteralConstraint c2 = new LiteralConstraint(f2);
        Constraint constraint = new ExclusionConstraint(c1, c2);
        featureModel.getOwnConstraints().add(constraint);
        return constraint;
    }

    public Constraint addRequirementConstraint(String feature, String dependency) {

        if(featureModel.getFeature(feature) == null || featureModel.getFeature(dependency) == null){
            throw new FeatureModelDefinitionException("Constraints should only refers to features belonging to the FM.");
        }

        LiteralConstraint f = new LiteralConstraint(feature);
        LiteralConstraint d = new LiteralConstraint(dependency);
        Constraint constraint = new RequirementConstraint(d, f);
        featureModel.getOwnConstraints().add(constraint);
        return constraint;
    }*/

    private FExpression buildFExpression(Feature feature) {

        FExpression featureExpression = FExpression.featureExpr(feature.getFeatureName());

        for(Group group : feature.getChildren()){

            FExpression groupExpression = null;
            List<FExpression> childrenExpressions = buildFExpression(group);

            switch (group.GROUPTYPE){
                case OR -> {
                    groupExpression = FExpression.falseValue();
                    for(FExpression child: childrenExpressions){
                        groupExpression.orWith(child);
                    }
                }
                case ALTERNATIVE -> {
                    groupExpression = FExpression.falseValue();
                    for(FExpression child: childrenExpressions){
                        FExpression c1 = groupExpression.or(child);
                        FExpression c2 = (groupExpression.not()).or(child.not());
                        groupExpression = c1.and(c2);
                    }
                }
                case MANDATORY -> {
                    if(childrenExpressions.size() == 1){
                        groupExpression = childrenExpressions.getFirst();
                    } else {
                        throw new FeatureModelDefinitionException("A mandatory group can only contain one feature!");
                    }
                }
                case OPTIONAL -> {
                    if(childrenExpressions.size() == 1){
                        FExpression child = childrenExpressions.getFirst();
                        groupExpression = child.or(child.not());
                    } else {
                        throw new FeatureModelDefinitionException("An optional group can only contain one feature!");
                    }
                }
            }

            assert groupExpression != null;
            featureExpression.andWith(groupExpression);
        }

        return featureExpression;
    }

    private List<FExpression> buildFExpression(Group group) {
        List<FExpression> childrenExpressions = new ArrayList<>();

        for (Feature child : group.getFeatures()) {
            FExpression childExp = buildFExpression(child);
            childrenExpressions.add(childExp);
        }

        return childrenExpressions;
    }


    private FExpression buildFExpression(Constraint constraint) {

        FExpression featureExpression;

        switch (constraint) {
            case RequirementConstraint c -> {
                FExpression left = buildFExpression(c.getLeft());
                FExpression right = buildFExpression(c.getRight());
                featureExpression = left.not().or(right);
            }
            case ExclusionConstraint c -> {
                FExpression left = buildFExpression(c.getLeft());
                FExpression right = buildFExpression(c.getRight());
                featureExpression = (left.or(right)).and(left.not().or(right.not()));
            }
            case LiteralConstraint c -> {
                Feature f = featureModel.getFeature(c.getLiteral());
                featureExpression = FExpression.featureExpr(f);
            }/*
            case ImplicationConstraint c ->  {
                FExpression left = buildFExpression(c.getLeft());
                FExpression right = buildFExpression(c.getRight());
                featureExpression = left.not().or(right);

            }
            case AndConstraint c ->  {
                featureExpression = buildFExpression(c.getLeft()).and(buildFExpression(c.getRight()));
            }
            case NotConstraint c ->  {
                featureExpression = buildFExpression(c.getContent()).not();
            }*/
            default -> throw new FeatureModelDefinitionException("This type of FM constraint is not yet defined!");
        }

        return featureExpression;
    }

    private SolverFacade getSolverFacade(FExpression featureDiagram) {

        DimacsModel model;
        try {
            model = DimacsModel.createFromFeatureList(featureDiagram);
        } catch (DimacsFormatException e) {
            throw new ParseError("Unable to initialise the DimacsModel.");
        }

        SolverFacade solver;

        switch (this.solverType) {
            case SAT4J -> {
                try {
                    solver = new Sat4JSolverFacade(model);
                } catch (SolverInitializationException e) {
                    throw new ParseError("Unable to initialise the SAT4J solver.");
                }
            }
            case BDD -> solver = new BDDSolverFacade(featureDiagram);
            default -> throw new UnsupportedOperationException("Only SAT4J and BDD solvers are currently supported. Default is SAT4J.");
        }
        return solver;
    }

    public FeatureModel<?> build() {
        FExpression fexp = buildFExpression(featureModel.getRootFeature());

        for(Constraint constr: featureModel.getOwnConstraints()){
            fexp.andWith(buildFExpression(constr));
        }

        featureModel.setSolver(getSolverFacade(fexp));
        return featureModel;
    }

}