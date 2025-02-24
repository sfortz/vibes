package be.vibes.solver;

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
import java.util.Iterator;
import java.util.Map;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.exception.ConstraintNotFoundException;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.exception.SolverFatalErrorException;
import be.vibes.solver.exception.SolverInitializationException;
import be.vibes.fexpression.Feature;

public class FeatureModel extends de.vill.model.FeatureModel {

    private SolverFacade solver;

    protected FeatureModel(de.vill.model.FeatureModel featureModel, SolverFacade solver) {
        super();
        this.getUsedLanguageLevels().addAll(featureModel.getUsedLanguageLevels());
        this.setNamespace(featureModel.getNamespace());
        this.getImports().addAll(featureModel.getImports());
        this.setRootFeature(featureModel.getRootFeature());
        this.getFeatureMap().putAll(featureModel.getFeatureMap());
        this.getImports().addAll(featureModel.getImports());
        this.getOwnConstraints().addAll(featureModel.getOwnConstraints());
        this.setExplicitLanguageLevels(featureModel.isExplicitLanguageLevels());
        this.getLiteralConstraints().addAll(featureModel.getLiteralConstraints());
        this.getLiteralExpressions().addAll(featureModel.getLiteralExpressions());
        this.getAggregateFunctionsWithRootFeature().addAll(featureModel.getAggregateFunctionsWithRootFeature());
        this.solver = solver;
    }


    public SolverFacade getSolver() {
        return solver;
    }

    /**
     * Set the root feature of the feature model
     * @param: rootFeature – the root feature
     */
    @Override
    public void setRootFeature(de.vill.model.Feature rootFeature) {
        super.setRootFeature(rootFeature);
        // TODO: Change the solver by getting the root feature Feature Diagram.
    }

    @Override
    public Feature getRootFeature() {
        return Feature.clone(super.getRootFeature());
    }

    public Feature getFeature(String name) {
        if (name == null) {
            return null;
        }

        // Normalize input name to lowercase
        //String lowerCaseName = name.toLowerCase();

        // Find matching feature in a case-insensitive way
        for (Map.Entry<String, de.vill.model.Feature> entry : this.getFeatureMap().entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return Feature.clone(entry.getValue());
            }
        }

        return null; // Return null if no match is found
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
     * @throws be.vibes.solver.exception.ConstraintNotFoundException
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
    public Iterator<Configuration> getSolutions() throws ConstraintSolvingException {
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
     * @return
     * @throws be.vibes.solver.exception.ConstraintSolvingException
     */
    public double getNumberOfSolutions() throws ConstraintSolvingException {
        return this.solver.getNumberOfSolutions();
    }

}
