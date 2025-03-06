package be.vibes.solver.exception;

public class FeatureModelDefinitionException extends RuntimeException {
    public FeatureModelDefinitionException(String message) {
        super(message);
    }

    public FeatureModelDefinitionException(String message, Exception cause) {
        super(message, cause);
    }
}
