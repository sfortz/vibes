package be.vibes.solver.io.xml;

import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.exception.FeatureModelDefinitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class XmlLoaders {

    private static final Logger LOG = LoggerFactory.getLogger(XmlLoaders.class);

    public static FeatureModel<Feature> loadFeatureModel(InputStream in) throws FeatureModelDefinitionException {
        FeatureModelHandler handler = new FeatureModelHandler();
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading TS", e);
            throw new FeatureModelDefinitionException("Error while reading TS!", e);
        }
        return handler.getFeatureModel();
    }

    public static FeatureModel<Feature> loadFeatureModel(File xmlFile) throws FeatureModelDefinitionException {
        try {
            return XmlLoaders.loadFeatureModel(new FileInputStream(xmlFile));
        } catch (FileNotFoundException e) {
            LOG.error("Error while loading TS input ={}!", xmlFile, e);
            throw new FeatureModelDefinitionException("Error while loading TS!", e);
        }
    }

    public static FeatureModel<Feature> loadFeatureModel(String xmlFile) throws FeatureModelDefinitionException {
        return XmlLoaders.loadFeatureModel(new File(xmlFile));
    }

}
