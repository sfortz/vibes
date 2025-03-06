package be.vibes.solver.io.xml;

import be.vibes.solver.FeatureModel;
import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import de.vill.model.Group;
import de.vill.model.constraint.Constraint;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;

public interface FeatureModelElementPrinter {
    void printElement(XMLStreamWriter writer, FeatureModel fm) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, List<Constraint> constraints) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, de.vill.model.Feature feature) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, Group group) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, ExclusionConstraint constraint) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, RequirementConstraint constraint) throws XMLStreamException;
}
