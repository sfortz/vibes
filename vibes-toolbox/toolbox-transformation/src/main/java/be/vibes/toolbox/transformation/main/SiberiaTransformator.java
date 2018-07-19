package be.vibes.toolbox.transformation.main;

import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.UsageModel;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.commons.cli.Option;

/**
 * Transforms the input TS to Siberia automaton.
 *
 * @author Xavier Devroey - xavier.devroey@unamur.be
 */
public class SiberiaTransformator implements Transformator {

    private static final String OPTION_NAME = "siberia";

    public static final SiberiaTransformator SIBERIA = new SiberiaTransformator();

    private SiberiaTransformator() {
    }

    @Override
    public Option getCommandLineOption() {
        return Option.builder(OPTION_NAME)
                .desc("Transforms input to Siberia format automaton.")
                .build();
    }

    @Override
    public void transform(TransitionSystem lts, OutputStream out, String... cmdArgs) throws IOException {
        throw new UnsupportedOperationException("Siberia automaton not supported yet");
        /*
        PrintStream output = new PrintStream(out);
        new LtsSiberiaPrinter(output, lts).printLTSiberia();
        output.flush();
        */
    }

    @Override
    public void transform(FeaturedTransitionSystem fts, OutputStream out, String... cmdArgs) throws IOException {
        throw new UnsupportedOperationException("Siberia automaton does not support featured transition systems!");
    }

    @Override
    public void transform(UsageModel um, OutputStream out, String... cmdArgs) throws IOException {
        throw new UnsupportedOperationException("Siberia automaton does not support featured transition systems!");
    }

}
