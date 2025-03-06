package be.vibes.solver.io.xml;

import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.FeatureModelDefinitionException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class XmlSavers {
    
    public static void save(FeatureModel fm, OutputStream out) throws FeatureModelDefinitionException {
        FeatureModelPrinter printer = new FeatureModelPrinter();
        FeatureModelXmlPrinter xmlOut = new FeatureModelXmlPrinter(out, printer);
        try {
            xmlOut.print(fm);
        } catch (XMLStreamException e) {
            throw new FeatureModelDefinitionException("Exception while printing XML!", e);
        }
    }

    public static void save(FeatureModel fm, File out) throws FeatureModelDefinitionException {
        try {
            save(fm, new FileOutputStream(out));
        } catch (FileNotFoundException e) {
            throw new FeatureModelDefinitionException("Output file not found!", e);
        }
    }
    
    public static void save(FeatureModel fm, String outputFileName) throws FeatureModelDefinitionException {
        save(fm, new File(outputFileName));
    }
}
