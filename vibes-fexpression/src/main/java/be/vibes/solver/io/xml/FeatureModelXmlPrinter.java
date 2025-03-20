package be.vibes.solver.io.xml;

import be.vibes.solver.FeatureModel;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class FeatureModelXmlPrinter {
    protected OutputStream output;
    private final FeatureModelElementPrinter fmPrinter;

    public FeatureModelXmlPrinter(OutputStream output, FeatureModelElementPrinter fmPrinter) {
        this.output = output;
        this.fmPrinter = fmPrinter;
    }

    public FeatureModelXmlPrinter(File outputFile, FeatureModelElementPrinter fmPrinter) throws FileNotFoundException {
        this(new FileOutputStream(outputFile), fmPrinter);
    }

    public void setOutput(OutputStream output) {
        this.output = output;
    }

    public void print(FeatureModel<?> fm) throws XMLStreamException {
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        IndentingXMLStreamWriter xtw = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(this.output));
        xtw.setIndentStep("    ");
        xtw.writeStartDocument("UTF-8","1.0");
        this.fmPrinter.printElement(xtw, fm);
        xtw.writeEndDocument();
        xtw.flush();
        xtw.close();
    }
}
