package be.vibes.ts.io.xml;

/*-
 * #%L
 * VIBeS: core
 * %%
 * Copyright (C) 2014 - 2018 University of Namur
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import be.vibes.fexpression.FExpression;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.Transition;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import be.vibes.ts.TransitionSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeaturedTransitionSystemPrinter extends TransitionSystemPrinter {

    private static final Logger LOG = LoggerFactory.getLogger(FeaturedTransitionSystemPrinter.class);

    public FeaturedTransitionSystemPrinter() {}

    @Override
    public void printElement(XMLStreamWriter xtw, TransitionSystem ts) throws XMLStreamException {
        LOG.trace("Printing FTS element");
        xtw.writeStartElement("fts");
        this.ts = ts;
        xtw.writeStartElement("start");
        xtw.writeCharacters(ts.getInitialState().getName());
        xtw.writeEndElement();
        xtw.writeStartElement("states");
        this.printElement(xtw, ts.states());
        xtw.writeEndElement();
        xtw.writeEndElement();
        this.ts = null;
    }

    @Override
    public void printElement(XMLStreamWriter xtw, Transition transition) throws XMLStreamException {
        LOG.trace("Printing transition element");
        xtw.writeStartElement("transition");
        xtw.writeAttribute("action", transition.getAction().getName());
        xtw.writeAttribute("target", transition.getTarget().getName());
        FExpression fexpr = this.getFTS().getFExpression(transition);
        xtw.writeAttribute("fexpression", fexpr.toString());
        xtw.writeEndElement();
    }

    private FeaturedTransitionSystem getFTS() {
        return (FeaturedTransitionSystem)this.ts;
    }
}