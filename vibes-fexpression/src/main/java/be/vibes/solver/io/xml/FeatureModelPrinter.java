package be.vibes.solver.io.xml;

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

import be.vibes.fexpression.FExpression;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.constraints.ExclusionConstraint;
import be.vibes.solver.constraints.RequirementConstraint;
import be.vibes.fexpression.Feature;
import be.vibes.solver.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.List;


import static be.vibes.solver.io.xml.FeatureModelHandler.*;

public class FeatureModelPrinter implements FeatureModelElementPrinter{

    private static final Logger LOG = LoggerFactory.getLogger(FeatureModelPrinter.class);

    private FeatureModel<?>  fm;

    @Override
    public void printElement(XMLStreamWriter xtw, FeatureModel<?> fm) throws XMLStreamException {

        this.fm = fm;
        LOG.trace("Printing FM element");
        xtw.writeStartElement(FM_TAG);
        xtw.writeAttribute(NAMESPACE_ATTR, fm.getNamespace());
        printElement(xtw, fm.getRootFeature());
        xtw.writeEndElement();
        this.fm = null;
    }

    @Override
    public void printElement(XMLStreamWriter xtw, Feature<?> feature) throws XMLStreamException {
        LOG.trace("Printing feature element");
        xtw.writeStartElement(FEATURE_TAG);
        xtw.writeAttribute(NAME_ATTR, feature.getFeatureName());
        for(Group<?>  group: feature.getChildren()){
            printElement(xtw, group);
        }

        if(feature.getNumberOfConstraints() > 0){
            xtw.writeStartElement(FEATURE_CONSTRAINTS_TAG);
            for (FExpression fexpr : feature.getConstraints()) {
                printElement(xtw, fexpr);
            }
            /*
            if(feature.getNumberOfExclusionConstraints() > 0){
                printExclusions(xtw, feature.getExclusions());
            }
            if(feature.getNumberOfRequirementConstraints() > 0){
                printRequirements(xtw, feature.getRequirements());
            }*/
            xtw.writeEndElement();
        }
        xtw.writeEndElement();
    }

    @Override
    public void printElement(XMLStreamWriter xtw, Group<?>  group) throws XMLStreamException {
        LOG.trace("Printing group element");
        switch (group.GROUPTYPE){
            case OR -> xtw.writeStartElement(OR_TAG);
            case ALTERNATIVE -> xtw.writeStartElement(ALTERNATIVE_TAG);
            case MANDATORY -> xtw.writeStartElement(MANDATORY_TAG);
            case OPTIONAL -> xtw.writeStartElement(OPTIONAL_TAG);
        }

        for (Feature<?> feature : group.getFeatures()) {
            Feature<?> f = fm.getFeature(feature.getFeatureName());
            printElement(xtw, f);
        }

        xtw.writeEndElement();
    }

    @Override
    public void printElement(XMLStreamWriter xtw, FExpression fexpr) throws XMLStreamException {
        LOG.trace("Printing constraint element");
        xtw.writeStartElement(FEATURE_CONSTRAINT_TAG);
        xtw.writeAttribute(FEXPRESSION_ATTR, fexpr.toString());
        xtw.writeEndElement();
    }

    /*
    @Override
    public void printExclusions(XMLStreamWriter xtw, List<ExclusionConstraint> exclusions) throws XMLStreamException {
        xtw.writeStartElement(EXCLUSIONS_TAG);
        for (ExclusionConstraint constraint : exclusions) {
            printElement(xtw, constraint);
        }
        xtw.writeEndElement();
    }

    @Override
    public void printRequirements(XMLStreamWriter xtw, List<RequirementConstraint> requirements) throws XMLStreamException {
        xtw.writeStartElement(REQUIREMENTS_TAG);
        for (RequirementConstraint constraint : requirements) {
            printElement(xtw, constraint);
        }
        xtw.writeEndElement();
    }

    @Override
    public void printElement(XMLStreamWriter xtw, ExclusionConstraint constraint) throws XMLStreamException {
        LOG.trace("Printing exclusion element");
        xtw.writeStartElement(EXCLUDE_TAG);
        xtw.writeAttribute(CONFLICT1_ATTR, constraint.getLeft().getLiteral());
        xtw.writeAttribute(CONFLICT2_ATTR, constraint.getRight().getLiteral());
        xtw.writeEndElement();
    }

    @Override
    public void printElement(XMLStreamWriter xtw, RequirementConstraint constraint) throws XMLStreamException {
        LOG.trace("Printing requirement element");
        xtw.writeStartElement(REQUIRES_TAG);
        xtw.writeAttribute(FEATURE_ATTR, constraint.getRight().getLiteral());
        xtw.writeAttribute(DEPENDENCY_ATTR, constraint.getLeft().getLiteral());
        xtw.writeEndElement();
    }*/
}
