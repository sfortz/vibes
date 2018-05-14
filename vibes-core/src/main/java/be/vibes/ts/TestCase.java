package be.vibes.ts;

import be.vibes.ts.exception.TransitionSystenExecutionException;

/**
 *
 * @author Xavier Devroey - xavier.devroey@gmail.com
 */
public class TestCase extends Execution{
    
    private final String id;

    public TestCase(String id) {
        super();
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public Execution copy() {
        TestCase copy = new TestCase(getId());
        try {
            copy.enqueueAll(this);
        } catch (TransitionSystenExecutionException ex) {
            throw new IllegalStateException("Copy of an inconsistent TestCase!", ex);
        }
        return copy;
    }
    
}
