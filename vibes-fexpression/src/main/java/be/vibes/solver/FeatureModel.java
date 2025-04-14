package be.vibes.solver;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.exception.*;
import de.vill.config.Configuration;
import de.vill.model.Attribute;
import de.vill.model.constraint.Constraint;
import de.vill.util.Util;

import java.util.*;
import static de.vill.util.Util.addNecessaryQuotes;

/**
 * This class represents a feature model and all its sub featuremodels if the
 * model is composed.
 */
public class FeatureModel<F extends Feature<F>> {

    private String namespace;
    private F rootFeature;
    private final Map<String, F> featureMap = new HashMap<>();
    private final List<FExpression> ownConstraints = new LinkedList<>();
    //private final List<Constraint> ownConstraints = new LinkedList<>();
    private SolverFacade solver;

    /**
     * Be very careful when creating your own featuremodel to set all information in
     * all objects. Especially when working with decomposed models it is important
     * to set all namespaces etc. right. If you are not sure look in the
     * {@link de.vill.main.UVLModelFactory} class how the feature model is assembled
     * there.
     */
    protected FeatureModel() {
    }

    protected FeatureModel(SolverFacade solver) {
        super();
        this.solver = solver;
    }

    protected <G extends Feature<G>> FeatureModel(FeatureModel<G> otherFM) {

        this(otherFM.getSolver());

        F root;

        // Ensure G extends F
        try{
            root = (F) otherFM.getRootFeature();
        } catch (ClassCastException e){
            throw new FeatureModelDefinitionException(e.getMessage(), e);
        }

        this.setNamespace(otherFM.getNamespace());
        this.setRootFeature(root);

        Queue<F> queue = new LinkedList<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            F f = queue.poll();
            this.getFeatureMap().put(f.getFeatureName(), f);  // Safe cast

            for (Group<F> group : f.getChildren()) {
                queue.addAll(group.getFeatures());
            }
        }

        this.getOwnConstraints().addAll(otherFM.getOwnConstraints());
    }

    public void setSolver(SolverFacade solver) {
        if(this.solver == null){
            this.solver = solver;
        } else {
            throw new FeatureModelDefinitionException("This Feature Model solver was already set.");
        }
    }

    public SolverFacade getSolver() {
        return solver;
    }

    public F getFeature(String name) {
        if (name == null) {
            return null;
        }

        // Find matching feature in a case-insensitive way
        for (Map.Entry<String, F> entry : this.featureMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }

        return null; // Return null if no match is found
    }

    public Collection<F> getFeatures() {
        return this.featureMap.values();
    }

    /**
     * Returns the namespace of the featuremodel. If no namespace is set, the
     * featuremodel returns the name of the root feature.
     *
     * @return the namespace of the feature model
     */
    public String getNamespace() {
        if (namespace == null) {
            if(rootFeature != null) {
                return rootFeature.getFeatureName();
            }
        }
        return namespace;
    }

    /**
     * Setter for the namespace of the featuremodel.
     *
     * @param namespace Namespace of the featuremodel.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Get the root feature of the feature model
     *
     * @return root feature
     */
    public F getRootFeature() {
        return rootFeature;
    }

    /**
     * Set the root feature of the feature model
     *
     * @param rootFeature the root feature
     */
    public void setRootFeature(F rootFeature) {
        this.rootFeature = rootFeature;
        // TODO: Change the solver by getting the root feature Feature Diagram.
    }

    /**
     * This map contains all features of this featuremodel and recursivly all
     * features of its imported submodels. This means in a decomposed feature model
     * only the feature model object of the root feature model contains all features
     * over all sub feature models in this map. The key is the reference with
     * namespace and feature name viewed from this model (this means the key is how
     * the feature is referenced from this feature model). Therefore in the root
     * feature model the key for each feature is its complete reference from the
     * root feature model. When adding or deleting features from this map the
     * featuremodel gets inconsistent when not also adding / removing them from the
     * feature tree.
     *
     * @return A map with all features of the feature model
     */
    public Map<String, F> getFeatureMap() {
        return featureMap;
    }

    /**
     * A list with all the constraints of this featuremodel. This does not contain
     * the constraints of the imported sub feature models or constraints in feature
     * attribtues.
     *
     * @return A list of the constraints of this featuremodel.
     */
    public List<FExpression> getOwnConstraints() {
        return ownConstraints;
    }
    /*
        public List<Constraint> getOwnConstraints() {
            return ownConstraints;
        }
    */

    /**
     * A list with all the constraints of that are part of feature attributes of
     * features in this featuremodel. This does not contain the constraints of the
     * imported features of sub feature models.
     *
     * @return A list of the constraints of featureattributes in this featuremodel.
     */
    /*
    public List<Constraint> getFeatureConstraints() {
        return getFeatureConstraints(getRootFeature());
    }

    private List<Constraint> getFeatureConstraints(F feature) {
        List<Constraint> featureConstraints = new LinkedList<>();
        Attribute<Constraint> featureConstraint = feature.getAttributes().get("constraint");
        Attribute<List<Constraint>> featureConstraintList = feature.getAttributes().get("constraints");
        if (featureConstraint != null) {
            featureConstraints.add(featureConstraint.getValue());
        }
        if (featureConstraintList != null) {
            featureConstraints.addAll(featureConstraintList.getValue());
        }
        for (Group<F> childGroup : feature.getChildren()) {
            for (F childFeature : childGroup.getFeatures()) {
                if (!childFeature.isSubmodelRoot()) {
                    featureConstraints.addAll(getFeatureConstraints(childFeature));
                }
            }
        }
        return featureConstraints;
    }*/

    /**
     * A list will all constraints of this featuremodel and recursively of all its
     * imported sub feature models (that are used). This inclues constraints in
     * feature attributes. This list is not stored but gets calculated with every
     * call. This means changing a constraint will have an effect to the feature
     * model, but adding deleting constraints will have no effect. This must be done
     * in the correspoding ownConstraint lists of the feature models. This means
     * when calling this method on the root feature model it returns with all
     * constraints of the decomposed feature model.
     *
     * @return a list will all constraints of this feature model.
     */
    public List<FExpression> getConstraints() {
        List<FExpression> constraints = new LinkedList<>();
        constraints.addAll(ownConstraints);
        return constraints;
    }
    /*
    public List<Constraint> getConstraints() {
        List<Constraint> constraints = new LinkedList<Constraint>();
        constraints.addAll(ownConstraints);
        constraints.addAll(getFeatureConstraints());
        return constraints;
    }*/

    /**
     * Prints the current featuremodel without composing it with the other models.
     *
     * @return the uvl representation of the current model without submodels
     */
    @Override
    public String toString() {
        return toString(false, "");
    }

    private String toString(boolean withSubmodels, String currentAlias) {
        StringBuilder result = new StringBuilder();
        if (namespace != null) {
            result.append("namespace ");
            result.append(addNecessaryQuotes(namespace));
            result.append(Configuration.getNewlineSymbol());
            result.append(Configuration.getNewlineSymbol());
        }

        if (rootFeature != null) {
            result.append("features");
            result.append(Configuration.getNewlineSymbol());
            if (withSubmodels) {
                result.append(Util.indentEachLine(getRootFeature().toString(withSubmodels, currentAlias)));
            } else {
                result.append(Util.indentEachLine(getRootFeature().toStringAsRoot(currentAlias)));
            }
            result.append(Configuration.getNewlineSymbol());
        }

        //List<Constraint> constraintList;
        List<FExpression> constraintList;
        if (withSubmodels) {
            constraintList = new LinkedList<>(ownConstraints);
        } else {
            constraintList = getOwnConstraints();
        }
        if (!constraintList.isEmpty()) {
            result.append("constraints");
            result.append(Configuration.getNewlineSymbol());
            //for (Constraint constraint : constraintList) {
                //result.append(Configuration.getTabulatorSymbol());
                //result.append(constraint.toString(withSubmodels, currentAlias));
                //result.append(Configuration.getNewlineSymbol());
            //}
            for (FExpression constraint : constraintList) {
                result.append(constraint.toString());
            }
        }

        return result.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FeatureModel)) {
            return false;
        }

        if (!this.namespace.equals(((FeatureModel<?>) obj).namespace)) {
            return false;
        }

        if (!this.getRootFeature().equals(((FeatureModel<?>) obj).getRootFeature())) {
            return false;
        }

        if (this.getOwnConstraints().size() != ((FeatureModel<?>) obj).getOwnConstraints().size()) {
            return false;
        }

        return Objects.equals(this.getOwnConstraints(), ((FeatureModel<?>) obj).getOwnConstraints());

        /*
        List<Constraint> objConstraints = ((FeatureModel<?>) obj).getOwnConstraints();
        for (final Constraint constraint : this.getOwnConstraints()) {
            if (!objConstraints.contains(constraint)) {
                return false;
            }
        }*/

        //return true;
    }


    /**
     * @param constraint
     * @return
     * @throws SolverInitializationException If the constraint could not be
     *                                       added to the solver. Exact reason depends on implementation.
     * @throws SolverFatalErrorException     If the solver encounter an error he
     *                                       could not recover from. Solver should be reset when this exception is
     *                                       launched
     */
    public ConstraintIdentifier addSolverConstraint(FExpression constraint)
            throws SolverInitializationException, SolverFatalErrorException {
        return this.solver.addConstraint(constraint);
    }

    /**
     * @param id
     * @throws SolverFatalErrorException                             If the solver encounter an error it
     *                                                               could not recover from. Solver should be reset when this exception is
     *                                                               launched.
     */
    public void removeSolverConstraint(ConstraintIdentifier id)
            throws SolverFatalErrorException, ConstraintNotFoundException {
        this.solver.removeConstraint(id);
    }

    /**
     * @return @throws ConstraintSolvingException
     */
    public boolean isSatisfiable() throws ConstraintSolvingException {
        return this.solver.isSatisfiable();
    }

    /**
     * @return @throws ConstraintSolvingException
     */
    public Iterator<be.vibes.fexpression.configuration.Configuration> getSolutions() throws ConstraintSolvingException {
        return this.solver.getSolutions();
    }

    /**
     * Reset the state of the solver. This method should be used only if the
     * solver is in an inconsistant state.
     *
     * @throws SolverInitializationException If an error occurs during the
     *                                       reseting of the solver.
     */
    public void resetSolver() throws SolverInitializationException {
        this.solver.reset();
    }

    /**
     * Returns the number of solutions.
     *
     */
    public double getNumberOfSolutions() throws ConstraintSolvingException {
        return this.solver.getNumberOfSolutions();
    }

    private List<F> getAncestors(F feature) {
        List<F> ancestors = new ArrayList<>();
        while (feature != null) {
            ancestors.add(feature);
            feature = (F) feature.getParentFeature();
        }
        return ancestors;
    }

    private F leastCommonAncestor(F f1, F f2) {
        List<F> ancestorsF1 = getAncestors(f1);
        List<F> ancestorsF2 = getAncestors(f2);

        // Find the lowest common ancestor
        for (F ancestor : ancestorsF1) {
            if (ancestorsF2.contains(ancestor)) {
                return ancestor;
            }
        }

        return null;
    }

    public F getLeastCommonAncestor(List<FExpression> fExpressions) {
        // Shortcut: if any expression is true, the root feature is the LCA
        if(fExpressions.contains(FExpression.trueValue())){
            return this.getRootFeature();
        }

        // Build the disjunction of all simplified expressions
        FExpression disjunction = FExpression.falseValue();
        for (FExpression fexp : fExpressions) {
            disjunction.orWith(fexp.applySimplification());
        }

        // Simplify the final disjunction
        disjunction = disjunction.toCnf().applySimplification();

        if (disjunction.isTrue()) {
            return this.getRootFeature();
        } else if (disjunction.isFalse()) {
            return null;
        }

        Set<Feature<?>> negativeFeaturesSet = new HashSet<>(disjunction.getNegatedFeatures());
        Set<F> featureSet = new HashSet<>();

        for (Feature<?> f : disjunction.getFeatures()) {

            F baseFeature = this.getFeature(f.getFeatureName());
            if (negativeFeaturesSet.contains(f)) {
                baseFeature = (F) baseFeature.getParentFeature();
            }

            featureSet.add(baseFeature);
        }

        if (featureSet.isEmpty()) {
            return null;
        }

        // Compute least common ancestor from the feature set
        Iterator<F> iterator = featureSet.iterator();
        F lca = iterator.next();

        while (iterator.hasNext()) {
            lca = leastCommonAncestor(lca, iterator.next());
        }

        return lca;
    }

}
