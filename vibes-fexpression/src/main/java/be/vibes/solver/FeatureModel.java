package be.vibes.solver;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.Feature;
import be.vibes.solver.exception.*;
import de.vill.config.Configuration;
import de.vill.model.Attribute;
import de.vill.model.Import;
import de.vill.model.LanguageLevel;
import de.vill.model.constraint.Constraint;
import de.vill.model.constraint.LiteralConstraint;
import de.vill.model.expression.AggregateFunctionExpression;
import de.vill.model.expression.LiteralExpression;
import de.vill.util.Util;

import java.util.*;
import java.util.stream.Collectors;

import static de.vill.util.Util.addNecessaryQuotes;

/**
 * This class represents a feature model and all its sub featuremodels if the
 * model is composed.
 */
public class FeatureModel<T extends Feature> {
    private final Set<LanguageLevel> usedLanguageLevels = new HashSet<LanguageLevel>() {
        {
            add(LanguageLevel.BOOLEAN_LEVEL);
        }
    };
    private String namespace;
    private final List<Import> imports = new LinkedList<>();
    private T rootFeature;
    private final Map<String, T> featureMap = new HashMap<>();
    private final List<Constraint> ownConstraints = new LinkedList<>();
    private boolean explicitLanguageLevels = false;
    private SolverFacade solver;


    /*
     * These three lists are just for performance. They contain all points where
     * features are referenced e.g. in constraints or aggregate functions. The
     * corresponding feature can not be set during parsing, because the feature may
     * be in a sub-model which is not parsed then. Therefore. we store all this
     * objects to reference the features after everything is parsed without
     * searching for them.
     */
    private final List<LiteralConstraint> literalConstraints = new LinkedList<>();
    private final List<LiteralExpression> literalExpressions = new LinkedList<>();
    private final List<AggregateFunctionExpression> aggregateFunctionsWithRootFeature = new LinkedList<>();

    /**
     * Be very careful when creating your own featuremodel to set all information in
     * all objects. Especially when working with decomposed models it is important
     * to set all namespaces etc. right. If you are not sure look in the
     * {@link de.vill.main.UVLModelFactory} class how the feature model is assembled
     * there.
     */
    public FeatureModel() {
    }

    public FeatureModel(SolverFacade solver) {
        super();
        this.solver = solver;
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

    public T getFeature(String name) {
        if (name == null) {
            return null;
        }

        // Find matching feature in a case-insensitive way
        for (Map.Entry<String, T> entry : this.featureMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }

        return null; // Return null if no match is found
    }

    public Collection<T> getFeatures() {
        return this.featureMap.values();
    }

    /**
     * Get a set with all in this featuremodel used language levels (major and
     * minor). This list contains the levels used in this feature model and all its
     * sub feature models (that are actually used) The returned set is a copy,
     * therefore changing it will NOT change the featuremodel.
     *
     * @return the used language levels as set
     */
    public Set<LanguageLevel> getUsedLanguageLevelsRecursively() {
        Set<LanguageLevel> languageLevels = new HashSet<>(getUsedLanguageLevels());
        for (Import importLine : imports) {
            if (importLine.isReferenced()) {
                languageLevels.addAll(importLine.getFeatureModel().getUsedLanguageLevelsRecursively());
            }
        }
        return languageLevels;
    }

    /**
     * Get a set with all in this featuremodel used language levels (major and
     * minor). This list contains the levels used in this feature model but not the
     * ones used in submodels. The returned set is no copy, therefore changing it
     * will change the featuremodel.
     *
     * @return the used language levels as set
     */
    public Set<LanguageLevel> getUsedLanguageLevels() {
        return usedLanguageLevels;
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
     * This method returns a list of the imports of the feature model. The list does
     * not contain recursivly all imports (including imports of imported feature
     * models), just the directly imported ones in this feature model. This list is
     * no clone. Changing it will actually change the feature model.
     *
     * @return A list containing all imports of this feature model.
     */
    public List<Import> getImports() {
        return imports;
    }

    /**
     * Get the root feature of the feature model
     *
     * @return root feature
     */
    public T getRootFeature() {
        return rootFeature;
    }

    /**
     * Set the root feature of the feature model
     *
     * @param rootFeature the root feature
     */
    public void setRootFeature(T rootFeature) {
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
    public Map<String, T> getFeatureMap() {
        return featureMap;
    }

    /**
     * A list with all the constraints of this featuremodel. This does not contain
     * the constraints of the imported sub feature models or constraints in feature
     * attribtues.
     *
     * @return A list of the constraints of this featuremodel.
     */
    public List<Constraint> getOwnConstraints() {
        return ownConstraints;
    }

    /**
     * A list with all the constraints of that are part of feature attributes of
     * features in this featuremodel. This does not contain the constraints of the
     * imported features of sub feature models.
     *
     * @return A list of the constraints of featureattributes in this featuremodel.
     */
    public List<Constraint> getFeatureConstraints() {
        return getFeatureConstraints(getRootFeature());
    }

    private List<Constraint> getFeatureConstraints(T feature) {
        List<Constraint> featureConstraints = new LinkedList<>();
        Attribute<Constraint> featureConstraint = feature.getAttributes().get("constraint");
        Attribute<List<Constraint>> featureConstraintList = feature.getAttributes().get("constraints");
        if (featureConstraint != null) {
            featureConstraints.add(featureConstraint.getValue());
        }
        if (featureConstraintList != null) {
            featureConstraints.addAll(featureConstraintList.getValue());
        }
        for (Group childGroup : feature.getChildren()) {
            for (Feature childFeature : childGroup.getFeatures()) {
                if (!childFeature.isSubmodelRoot()) {
                    featureConstraints.addAll(getFeatureConstraints((T) childFeature));
                }
            }
        }
        return featureConstraints;
    }

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
    public List<Constraint> getConstraints() {
        List<Constraint> constraints = new LinkedList<Constraint>();
        constraints.addAll(ownConstraints);
        constraints.addAll(getFeatureConstraints());
        for (Import importLine : imports) {
            if (importLine.isReferenced()) {
                constraints.addAll(importLine.getFeatureModel().getConstraints());
            }
        }
        return constraints;
    }

    /**
     * Boolean whether the used language levels are explicitly imported or not
     *
     * @return true if all levels must be explicitly imported, fales if not
     */
    public boolean isExplicitLanguageLevels() {
        return explicitLanguageLevels;
    }

    /**
     * Boolean whether the used language levels are explicitly imported or not. This
     * determines if the used levels are printed in the toSring method
     *
     * @param explicitLanguageLevels true if all levels must be explicitly imported,
     *                               fales if not
     */
    public void setExplicitLanguageLevels(boolean explicitLanguageLevels) {
        this.explicitLanguageLevels = explicitLanguageLevels;
    }

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
        if (explicitLanguageLevels && !usedLanguageLevels.isEmpty()) {
            result.append("include");
            result.append(Configuration.getNewlineSymbol());
            Set<LanguageLevel> levelsToPrint;
            if (withSubmodels) {
                levelsToPrint = getUsedLanguageLevelsRecursively();
            } else {
                levelsToPrint = getUsedLanguageLevels();
            }
            for (LanguageLevel languageLevel : getUsedLanguageLevels()) {
                result.append(Configuration.getTabulatorSymbol());
                if (LanguageLevel.isMajorLevel(languageLevel)) {
                } else {
                    result.append(LanguageLevel.valueOf(languageLevel.getValue() - 1).getFirst().getName());
                    result.append(".");
                }
                result.append(languageLevel.getName());
                result.append(Configuration.getNewlineSymbol());
            }
            result.append(Configuration.getNewlineSymbol());
        }
        if (!imports.isEmpty() && !withSubmodels) {
            result.append("imports");
            result.append(Configuration.getNewlineSymbol());
            for (Import importLine : imports) {
                result.append(Configuration.getTabulatorSymbol());
                result.append(addNecessaryQuotes(importLine.getNamespace()));
                if (!importLine.getAlias().equals(importLine.getNamespace())) {
                    result.append(" as ");
                    result.append(addNecessaryQuotes(importLine.getAlias()));
                }
                result.append(Configuration.getNewlineSymbol());
            }
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

        List<Constraint> constraintList;
        if (withSubmodels) {
            constraintList = new LinkedList<>(ownConstraints);
            for (Import importLine : imports) {
                // only print the constraints of a submodel if the import is actually used
                if (importLine.isReferenced()) {
                    constraintList.addAll(importLine.getFeatureModel().getOwnConstraints());
                }
            }
        } else {
            constraintList = getOwnConstraints();
        }
        if (!constraintList.isEmpty()) {
            result.append("constraints");
            result.append(Configuration.getNewlineSymbol());
            for (Constraint constraint : constraintList) {
                result.append(Configuration.getTabulatorSymbol());
                result.append(constraint.toString(withSubmodels, currentAlias));
                result.append(Configuration.getNewlineSymbol());
            }
        }

        return result.toString();
    }

    /**
     * This list exists just for performance reasons when building a decomposed
     * feature model from several uvl files. This list does not have to be set, when
     * building your own feature model. It is also not necessary to update this list
     * when adding for example constraints.
     *
     * @return a list with all {@link LiteralConstraint} objects in the constraints
     *         of this feature model.
     */
    public List<LiteralConstraint> getLiteralConstraints() {
        return literalConstraints;
    }

    /**
     * This list exists just for performance reasons when building a decomposed
     * feature model from several uvl files. This list does not have to be set, when
     * building your own feature model. It is also not necessary to update this list
     * when adding for example constraints.
     *
     * @return a list with all {@link LiteralExpression} objects in the constraints
     *         of this feature model.
     */
    public List<LiteralExpression> getLiteralExpressions() {
        return literalExpressions;
    }

    /**
     * This list exists just for performance reasons when building a decomposed
     * feature model from several uvl files. This list does not have to be set, when
     * building your own feature model. It is also not necessary to update this list
     * when adding for example constraints.
     *
     * @return a list with all {@link AggregateFunctionExpression} objects in the
     *         constraints of this feature model.
     */
    public List<AggregateFunctionExpression> getAggregateFunctionsWithRootFeature() {
        return aggregateFunctionsWithRootFeature;
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


        if (this.getImports().size() != ((FeatureModel<?>) obj).getImports().size()) {
            return false;
        }

        if (this.getUsedLanguageLevels().size() != ((FeatureModel<?>) obj).getUsedLanguageLevels().size()) {
            return false;
        }

        for (LanguageLevel languageLevel: this.getUsedLanguageLevels()) {
            if (!(((FeatureModel<?>) obj).getUsedLanguageLevels().contains(languageLevel))) {
                return false;
            }
        }

        List<Import> objImports = ((FeatureModel<?>) obj).getImports();
        for (Import thisImport: this.getImports()) {
            final Optional<Import> identicalImport = objImports.stream()
                    .filter(imp -> imp.getFeatureModel().equals(thisImport.getFeatureModel()))
                    .findFirst();

            if (identicalImport.isEmpty()) {
                return false;
            }
        }

        if (this.getOwnConstraints().size() != ((FeatureModel<?>) obj).getOwnConstraints().size()) {
            return false;
        }

        List<Constraint> objConstraints = ((FeatureModel<?>) obj).getOwnConstraints();
        for (final Constraint constraint : this.getOwnConstraints()) {
            if (!objConstraints.contains(constraint)) {
                return false;
            }
        }

        return true;
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

    private List<T> getAncestors(T feature) {
        List<T> ancestors = new ArrayList<>();
        while (feature != null) {
            ancestors.add(feature);
            feature = (T) feature.getParentFeature();
        }
        return ancestors;
    }

    private T leastCommonAncestor(T f1, T f2) {
        List<T> ancestorsF1 = getAncestors(f1);
        List<T> ancestorsF2 = getAncestors(f2);

        // Find the lowest common ancestor
        for (T ancestor : ancestorsF1) {
            if (ancestorsF2.contains(ancestor)) {
                return ancestor;
            }
        }

        return null;
    }

    public T leastCommonAncestor (List<FExpression> fExpressions){

        FExpression disjunction = FExpression.falseValue();

        for (FExpression fexp: fExpressions){
            disjunction.orWith(fexp);
        }

        disjunction = disjunction.toCnf().applySimplification();

        if (disjunction.isTrue()) {
            return this.getRootFeature();
        }

        Set<T> features = disjunction.getFeatures().stream().map(f -> this.getFeature(f.getFeatureName())).collect(Collectors.toSet());

        Iterator<T> iterator = features.iterator();
        T lca = iterator.next();

        while (iterator.hasNext()) {
            lca = leastCommonAncestor(lca, iterator.next());
        }
        return lca;
    }

}
