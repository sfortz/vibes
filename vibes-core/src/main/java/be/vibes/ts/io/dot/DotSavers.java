package be.vibes.ts.io.dot;

import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TransitionSystem;

import java.io.*;

public class DotSavers {

    /**
     * Prints the given TS on the given output in Graphviz Dot format.
     *
     * @param ts The TS to print.
     * @param out The output on which to print the TS.
     */
    public static void save(TransitionSystem ts, PrintStream out) {
        TransitionSystemDotPrinter dotOut = new TransitionSystemDotPrinter(ts,out);
        dotOut.printDot();
        dotOut.flush();
    }

    public static void save(TransitionSystem ts, FileOutputStream out) {
        save(ts, new PrintStream(out));
    }

    public static void save(TransitionSystem ts, File outputFile) throws FileNotFoundException {
        save(ts, new FileOutputStream(outputFile));
    }

    public static void save(TransitionSystem ts, String outputFileName) throws FileNotFoundException {
        save(ts, new FileOutputStream(outputFileName));
    }

    public static void save(FeaturedTransitionSystem fts, PrintStream out) {
        FeaturedTransitionSystemDotPrinter dotOut = new FeaturedTransitionSystemDotPrinter(fts,out);
        dotOut.printDot();
        dotOut.flush();
    }

    public static void save(FeaturedTransitionSystem fts, FileOutputStream out) {
        save(fts, new PrintStream(out));
    }

    public static void save(FeaturedTransitionSystem fts, File outputFile) throws FileNotFoundException {
        save(fts, new FileOutputStream(outputFile));
    }

    public static void save(FeaturedTransitionSystem fts, String outputFileName) throws FileNotFoundException {
        save(fts, new FileOutputStream(outputFileName));
    }

}
