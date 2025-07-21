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

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;


public class XmlSavers {
    
    public static void save(FeatureModel fm, OutputStream out) throws FeatureModelDefinitionException {
        FeatureModelPrinter printer = new FeatureModelPrinter();
        FeatureModelXmlPrinter xmlOut = new FeatureModelXmlPrinter(out, printer);
        try {
            xmlOut.print(fm);
        } catch (XMLStreamException e) {
            throw new FeatureModelDefinitionException("Exception while printing XML!", e);
        }
    }

    public static void save(FeatureModel fm, File out) throws FeatureModelDefinitionException {
        try {
            save(fm, new FileOutputStream(out));
        } catch (FileNotFoundException e) {
            throw new FeatureModelDefinitionException("Output file not found!", e);
        }
    }
    
    public static void save(FeatureModel fm, String outputFileName) throws FeatureModelDefinitionException {
        save(fm, new File(outputFileName));
    }
}
