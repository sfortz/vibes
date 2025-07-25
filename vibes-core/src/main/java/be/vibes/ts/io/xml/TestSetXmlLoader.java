package be.vibes.ts.io.xml;

/*-
 * #%L
 * VIBeS: core
 * %%
 * Copyright (C) 2014 - 2018 University of Namur
 * Copyright 2025 Sophie Fortz
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

import be.vibes.solver.FeatureModel;
import be.vibes.ts.FeaturedTransitionSystem;
import be.vibes.ts.TestSet;
import be.vibes.ts.TransitionSystem;
import be.vibes.ts.exception.TransitionSystemDefinitionException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Xavier Devroey - xavier.devroey@gmail.com
 */
public class TestSetXmlLoader {
    
    private static final Logger LOG = LoggerFactory.getLogger(TestSetXmlLoader.class);
    
    public static TestSet loadTestSet(InputStream in, TransitionSystem ts) throws TransitionSystemDefinitionException {
        TestCaseHandler handler = new TestCaseHandler(ts);
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading TS input!", e);
            throw new TransitionSystemDefinitionException("Error while reading TS!", e);
        }
        return handler.getTestSet();
    }

    public static TestSet loadTestSet(InputStream in, FeaturedTransitionSystem fts, FeatureModel<?> fm) throws TransitionSystemDefinitionException {
        FtsTestCaseHandler handler = new FtsTestCaseHandler(fts, fm);
        try {
            XmlReader reader = new XmlReader(handler, in);
            reader.readDocument();
        } catch (XMLStreamException e) {
            LOG.error("Error while reading TS input!", e);
            throw new TransitionSystemDefinitionException("Error while reading TS!", e);
        }
        return handler.getTestSet();
    }

}
