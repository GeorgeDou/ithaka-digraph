/*
 * Copyright 2012 Odysseus Software GmbH
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
package de.odysseus.ithaka.digraph.io.graphml;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Assert;
import org.junit.Test;

import de.odysseus.ithaka.digraph.Digraph;
import de.odysseus.ithaka.digraph.MapDigraph;
import de.odysseus.ithaka.digraph.io.graphml.AbstractGraphMLProperty;
import de.odysseus.ithaka.digraph.io.graphml.GraphMLAttribute;
import de.odysseus.ithaka.digraph.io.graphml.GraphMLExporter;
import de.odysseus.ithaka.digraph.io.graphml.GraphMLPropertyDomain;
import de.odysseus.ithaka.digraph.io.graphml.GraphMLPropertyType;
import de.odysseus.ithaka.digraph.io.graphml.SimpleGraphMLProvider;

public class GraphMLExporterTest {
	AbstractGraphMLProperty<String> name = new GraphMLAttribute<String>(GraphMLPropertyDomain.Node, "name", GraphMLPropertyType.String) {
		@Override
		protected String getData(String value) {
			return value;
		}
	};
	AbstractGraphMLProperty<Integer> weight = new GraphMLAttribute<Integer>(GraphMLPropertyDomain.Edge, "weight", GraphMLPropertyType.String) {
		@Override
		protected String getData(Integer value) {
			return value == null ? null : value.toString();
		}
	};
	AbstractGraphMLProperty<Digraph<? extends String, ? extends Integer>> foo =
			new AbstractGraphMLProperty<Digraph<? extends String, ? extends Integer>>(GraphMLPropertyDomain.Graph, "urn:foo:bar", "ext") {
		@Override
		protected void writeKeyExtraAttributes(XMLStreamWriter writer) throws XMLStreamException {
		}
		@Override
		protected boolean hasDefault() {
			return false;
		}
		@Override
		protected void writeDefaultContent(XMLStreamWriter writer) throws XMLStreamException {			
		}
		@Override
		protected boolean hasData(Digraph<? extends String,? extends Integer> data) {
			return true;
		}
		@Override
		protected void writeDataContent(XMLStreamWriter writer, Digraph<? extends String, ? extends Integer> data) throws XMLStreamException {
			writer.writeEmptyElement(getPrefix(), "bar", getNamespaceURI());
		}
	};

	@Test
	public void test() throws XMLStreamException {
		Digraph<String, Integer> digraph = new MapDigraph<String, Integer>();
		digraph.put("a", "b", 1);
		digraph.put("b", "a", 2);
		digraph.put("a", "c", 3);
		digraph.add("d");

		SimpleGraphMLProvider<String, Integer, Digraph<? extends String,? extends Integer>> provider =
				new SimpleGraphMLProvider<String, Integer, Digraph<? extends String,? extends Integer>>();
		provider.addGraphProperty(foo);
		provider.addNodeProperty(name);
		provider.addEdgeProperty(weight);
		
		StringWriter result = new StringWriter();
		XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(result);
		new GraphMLExporter().export(provider, digraph, null, writer);

//		System.out.println(result);

		try {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(getClass().getResource("graphml-foo.xsd"));
			Validator validator = schema.newValidator();
			validator.validate(new StreamSource(new StringReader(result.toString())));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}
}
