import be.vibes.fexpression.exception.DimacsFormatException;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import be.vibes.ts.io.dot.*;
import be.vibes.ts.io.xml.XmlSavers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Main {
    public static void main(String[] args) throws IOException, DimacsFormatException, TransitionSystemDefinitionException {
        test2();
    }

    public static void test() throws IOException, TransitionSystemDefinitionException {

        String dirPath = "vibes-core/src/main/resources/";
        String file = dirPath + "aerouc5.dot";

        TransitionSystem ts = TransitionSystemDotHandler.parseDotFile(file);
        System.out.println("Loaded Transition System: " + ts);
        XmlSavers.save(ts, dirPath + "new.ts");

        PrintStream output = new PrintStream(new FileOutputStream(dirPath + "new.dot"));
        TransitionSystemDotPrinter printer = new TransitionSystemDotPrinter(ts, output);
        printer.printDot();
        printer.flush();
    }

    public static void test2() throws IOException, TransitionSystemDefinitionException {

        String dirPath = "vibes-core/src/main/resources/";
        String file = dirPath + "robot.dot";

        FeaturedTransitionSystem fts = FeaturedTransitionSystemDotHandler.parseDotFile(file);
        System.out.println("Loaded Transition System: " + fts);
        XmlSavers.save(fts, dirPath + "new.ts");
        DotSavers.save(fts,dirPath + "new.dot");
    }

}
