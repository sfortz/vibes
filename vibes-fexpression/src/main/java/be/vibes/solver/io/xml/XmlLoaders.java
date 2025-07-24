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

    public static FeatureModel<?> loadFeatureModel(InputStream in) throws FeatureModelDefinitionException {
        FeatureModelHandler<?> handler = new FeatureModelHandler<>();
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading TS", e);
            throw new FeatureModelDefinitionException("Error while reading TS!", e);
        }
        return handler.getFeatureModel();
    }

    public static FeatureModel<?> loadFeatureModel(File xmlFile) throws FeatureModelDefinitionException {
        try {
            return XmlLoaders.loadFeatureModel(new FileInputStream(xmlFile));
        } catch (FileNotFoundException e) {
            LOG.error("Error while loading TS input ={}!", xmlFile, e);
            throw new FeatureModelDefinitionException("Error while loading TS!", e);
        }
    }

    public static FeatureModel<?> loadFeatureModel(String xmlFile) throws FeatureModelDefinitionException {
        return XmlLoaders.loadFeatureModel(new File(xmlFile));
    }

}
