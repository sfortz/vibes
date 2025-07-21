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
import be.vibes.fexpression.Feature;
import be.vibes.fexpression.ParserUtil;
import be.vibes.fexpression.exception.ParserException;
import be.vibes.solver.FeatureModelFactory;
import be.vibes.solver.Group;
import be.vibes.solver.FeatureModel;
import be.vibes.solver.SolverType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import java.util.Stack;

public class FeatureModelHandler <F extends Feature<F>> implements XmlEventHandler {
    public static final String FM_TAG = "fm";
    public static final String FEATURE_TAG = "feature";
    public static final String OPTIONAL_TAG = "optional";
    public static final String MANDATORY_TAG = "mandatory";
    public static final String ALTERNATIVE_TAG = "alternative";
    public static final String OR_TAG = "or";

    public static final String FEATURE_CONSTRAINTS_TAG = "feature_constraints";
    public static final String FEATURE_CONSTRAINT_TAG = "feature_constraint";
    /*
    public static final String EXCLUSIONS_TAG = "exclusions";
    public static final String EXCLUDE_TAG = "exclude";
    public static final String REQUIREMENTS_TAG = "requirements";
    public static final String REQUIRES_TAG = "requires";*/

    public static final String NAMESPACE_ATTR = "namespace";
    public static final String NAME_ATTR = "name";
    public static final String FEXPRESSION_ATTR = "fexpression";
    /*
    public static final String CONFLICT1_ATTR = "conflict1"; //TODO: For more expressiveness, we could add al type of FExpressions
    public static final String CONFLICT2_ATTR = "conflict2";
    public static final String FEATURE_ATTR = "feature";
    public static final String DEPENDENCY_ATTR = "dependency";*/

    private static final Logger LOG = LoggerFactory.getLogger(FeatureModelHandler.class);

    private final FeatureModelFactory<F>  factory;
    protected String charValue;

    // Stack to track FM depth
    protected Stack<Group<F>> groupStack = new Stack<>();
    protected Stack<F> featureStack = new Stack<>();

    public FeatureModelHandler() {
        this.factory = new FeatureModelFactory<>(SolverType.BDD);
    }

    public FeatureModel<F> getFeatureModel() {
        return this.factory.build();
    }

    public void handleStartDocument() {
        LOG.trace("Starting document");
    }

    public void handleEndDocument() {
        LOG.trace("Ending document");
    }

    public void handleStartElement(StartElement element) throws XMLStreamException {
        String tag = element.getName().getLocalPart();
        switch (tag) {
            case FM_TAG:
                handleStartFMTag(element);
                break;
            case FEATURE_TAG:
                handleStartFeatureTag(element);
                break;
            case OPTIONAL_TAG:
                handleStartOptionalTag(element);
                break;
            case MANDATORY_TAG:
                handleStartMandatoryTag(element);
                break;
            case OR_TAG:
                handleStartOrTag(element);
                break;
            case ALTERNATIVE_TAG:
                handleStartAlternativeTag(element);
                break;
            case FEATURE_CONSTRAINTS_TAG:
                handleStartFConstraintsTag();
                break;
            case FEATURE_CONSTRAINT_TAG:
                handleStartFConstraintTag(element);
                break;
                /*
            case EXCLUSIONS_TAG:
                handleStartExclusionsTag();
                break;
            case EXCLUDE_TAG:
                handleStartExcludeTag(element);
                break;
            case REQUIREMENTS_TAG:
                handleStartRequirementsTag();
                break;
            case REQUIRES_TAG:
                handleStartRequiresTag(element);
                break;*/
            default:
                LOG.debug("Unknown element: {}", tag);
        }
    }

    protected void handleStartFMTag(StartElement element) throws XMLStreamException {
        LOG.trace("Starting FM");
        LOG.trace("Processing namespace");
        String namespace = element.getAttributeByName(QName.valueOf(NAMESPACE_ATTR)).getValue();
        factory.setNamespace(namespace);
    }

    protected void handleStartFConstraintsTag() throws XMLStreamException {
        LOG.trace("Starting Feature Constraints");
    }

    protected void handleStartFConstraintTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing Feature Expression");
        String expr = element.getAttributeByName(QName.valueOf(FEXPRESSION_ATTR)).getValue();
        FExpression fexpr;
        try {
            fexpr = ParserUtil.getInstance().parse(expr);
        } catch (ParserException e) {
            LOG.error("Exception while parsing fexpression {}!", expr, e);
            throw new XMLStreamException("Exception while parsing fexpression " + expr, e);
        }
        factory.addConstraint(featureStack.peek(), fexpr);
    }

        /*
    protected void handleStartExclusionsTag() throws XMLStreamException {
        LOG.trace("Starting Exclusions");
    }

    protected void handleStartRequirementsTag() throws XMLStreamException {
        LOG.trace("Starting Requirements");
    }

    protected void handleStartExcludeTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing Exclusion");
        String c1 = element.getAttributeByName(QName.valueOf(CONFLICT1_ATTR)).getValue();
        String c2 = element.getAttributeByName(QName.valueOf(CONFLICT2_ATTR)).getValue();
        factory.addExclusionConstraint(featureStack.peek(), c1, c2);
    }

    protected void handleStartRequiresTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing Requirements");
        String feature = element.getAttributeByName(QName.valueOf(FEATURE_ATTR)).getValue();
        String dependency = element.getAttributeByName(QName.valueOf(DEPENDENCY_ATTR)).getValue();
        factory.addRequirementConstraint(featureStack.peek(), feature, dependency);
    }*/

    protected void handleStartFeatureTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing feature");
        String featureName = element.getAttributeByName(QName.valueOf(NAME_ATTR)).getValue();

        F currentFeature;

        if (groupStack.isEmpty()){
            currentFeature = factory.setRootFeature(featureName);
        } else {
            currentFeature = factory.addFeature(groupStack.peek(), featureName);
        }
        featureStack.push(currentFeature);
    }

    protected void handleStartOptionalTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing optional group");
        Group<F> currentGroup = factory.addChild(featureStack.peek(), Group.GroupType.OPTIONAL);
        groupStack.push(currentGroup);
    }
    protected void handleStartMandatoryTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing mandatory group");
        Group<F> currentGroup = factory.addChild(featureStack.peek(), Group.GroupType.MANDATORY);
        groupStack.push(currentGroup);
    }

    protected void handleStartOrTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing or group");
        Group<F> currentGroup = factory.addChild(featureStack.peek(), Group.GroupType.OR);
        groupStack.push(currentGroup);
    }

    protected void handleStartAlternativeTag(StartElement element) throws XMLStreamException {
        LOG.trace("Processing alternative group");
        Group<F> currentGroup = factory.addChild(featureStack.peek(), Group.GroupType.ALTERNATIVE);
        groupStack.push(currentGroup);
    }

    public void handleEndElement(EndElement element) throws XMLStreamException {
        String tag = element.getName().getLocalPart();
        switch (tag) {
            case FEATURE_TAG:
                LOG.trace("Ending feature");
                featureStack.pop();
                break;
            case MANDATORY_TAG, OPTIONAL_TAG, OR_TAG, ALTERNATIVE_TAG:
                LOG.trace("Ending group");
                groupStack.pop();
                break;/*
            case EXCLUDE_TAG:
                LOG.trace("Ending Exclusion");
                break;
            case REQUIRES_TAG:
                LOG.trace("Ending Requirement");
                break;*/
            case FM_TAG:
                LOG.trace("Ending feature model");
                break;
        }
    }

    public void handleCharacters(Characters element) throws XMLStreamException {
        this.charValue = element.asCharacters().getData().trim();
    }
}