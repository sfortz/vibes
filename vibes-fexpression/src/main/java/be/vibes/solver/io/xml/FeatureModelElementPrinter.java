package be.vibes.solver.io.xml;

import be.vibes.fexpression.Feature;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import be.vibes.solver.Group;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

public interface FeatureModelElementPrinter {
    void printElement(XMLStreamWriter writer, FeatureModel<?> fm) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, Feature<?> feature) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, Group<?> group) throws XMLStreamException;

    void printExclusions(XMLStreamWriter xtw, List<ExclusionConstraint> exclusions) throws XMLStreamException;

    void printRequirements(XMLStreamWriter xtw, List<RequirementConstraint> requirements) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, ExclusionConstraint constraint) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, RequirementConstraint constraint) throws XMLStreamException;
}
