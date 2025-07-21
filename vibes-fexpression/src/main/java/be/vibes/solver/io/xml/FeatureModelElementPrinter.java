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

    //void printExclusions(XMLStreamWriter xtw, List<ExclusionConstraint> exclusions) throws XMLStreamException;

    //void printRequirements(XMLStreamWriter xtw, List<RequirementConstraint> requirements) throws XMLStreamException;

    void printElement(XMLStreamWriter xtw, FExpression fexpr) throws XMLStreamException;

   //void printElement(XMLStreamWriter xtw, ExclusionConstraint constraint) throws XMLStreamException;

    //void printElement(XMLStreamWriter xtw, RequirementConstraint constraint) throws XMLStreamException;
}
