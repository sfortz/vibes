package be.vibes.solver;

import be.vibes.fexpression.FExpression;
import be.vibes.fexpression.configuration.Configuration;
import be.vibes.solver.exception.ConstraintNotFoundException;
import be.vibes.solver.exception.ConstraintSolvingException;
import be.vibes.solver.exception.SolverFatalErrorException;
import be.vibes.solver.exception.SolverInitializationException;

import java.util.Iterator;

public interface SolverFacade {

    /**
     *
     * @param constraint
     * @return
     * @throws SolverInitializationException If the constraint could not be
     * added to the solver. Exact reason depends on implementation.
     * @throws SolverFatalErrorException If the solver encounter an error he
     * could not recover from. Solver should be reset when this exception is
     * launched
     */
    public ConstraintIdentifier addConstraint(FExpression constraint)
            throws SolverInitializationException, SolverFatalErrorException;

    /**
     *
     * @param id
     * @throws SolverFatalErrorException If the solver encounter an error it
     * could not recover from. Solver should be reset when this exception is
     * launched.
     * @throws ConstraintNotFoundException
     */
    public void removeConstraint(ConstraintIdentifier id)
            throws SolverFatalErrorException, ConstraintNotFoundException;

    /**
     *
     * @return @throws ConstraintSolvingException
     */
    public boolean isSatisfiable() throws ConstraintSolvingException;

    /**
     *
     * @return @throws ConstraintSolvingException
     */
    public Iterator<Configuration> getSolutions() throws ConstraintSolvingException;

    /**
     * Reset the state of the solver. This method should be used only if the
     * solver is in an inconsistant state.
     *
     * @throws SolverInitializationException If an error occurs during the
     * reseting of the solver.
     */
    public void reset() throws SolverInitializationException;

    /**
     * Returns the number of solutions.
     *
     * @return
     * @throws ConstraintSolvingException
     */
    public double getNumberOfSolutions() throws ConstraintSolvingException;

}
