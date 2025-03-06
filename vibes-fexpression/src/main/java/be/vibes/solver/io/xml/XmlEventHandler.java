package be.vibes.solver.io.xml;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

public interface XmlEventHandler {

    void handleStartDocument();

    void handleEndDocument();

    void handleStartElement(StartElement asStartElement) throws XMLStreamException;

    void handleEndElement(EndElement asEndElement) throws XMLStreamException;

    void handleCharacters(Characters asCharacters) throws XMLStreamException;

}
