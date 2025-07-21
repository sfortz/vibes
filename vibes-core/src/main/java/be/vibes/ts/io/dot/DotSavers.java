package be.vibes.ts.io.dot;

/*
 * Copyright 2025 Sophie Fortz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
